package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.ReservaSolapadaException;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.CalendarioTemporadas;
import co.edu.uniquindio.proyectofinal.modelo.Canal;
import co.edu.uniquindio.proyectofinal.modelo.Cotizacion;
import co.edu.uniquindio.proyectofinal.modelo.Empleado;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.Folio;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.Movimiento;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.modelo.Rol;
import co.edu.uniquindio.proyectofinal.modelo.Ruta;
import co.edu.uniquindio.proyectofinal.modelo.SolicitudExterna;
import co.edu.uniquindio.proyectofinal.modelo.TareaPreparacion;
import co.edu.uniquindio.proyectofinal.modelo.Temporada;
import co.edu.uniquindio.proyectofinal.util.Reloj;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;

/**
 * La clase que une todo el sistema. No repite logica: solo llama a las otras clases de servicio
 * en el orden correcto. Las pantallas y el cargador del escenario hablan solo con esta clase.
 */
public class SistemaAlojamiento {

    /** Porcentaje del total que se paga como anticipo en el portal. */
    public static final int PORCENTAJE_ANTICIPO = 30;
    /** Hora que se manda a la cola de preparacion cuando el apartamento no tiene entrada hoy. */
    public static final LocalTime SIN_HORA = LocalTime.of(23, 59);

    private final CatalogoApartamentos catalogo = new CatalogoApartamentos();
    private final RegistroHuespedes huespedes = new RegistroHuespedes();
    private final GestorReservas gestorReservas = new GestorReservas();
    private final GestorFolio folios = new GestorFolio();
    private final CalendarioTemporadas calendario = new CalendarioTemporadas();
    private final Cotizador cotizador = new Cotizador();
    private final GestorPreparacion preparacion = new GestorPreparacion();
    private final CanalExterno canalExterno = new CanalExterno();
    private final VencimientoReservas vencimientos = new VencimientoReservas();
    private final MapaEdificio mapa = new MapaEdificio();
    private final TurnosRecepcion turnos = new TurnosRecepcion();
    private final RegistroEmpleados empleados = new RegistroEmpleados();
    private final AsignadorGrupos asignador = new AsignadorGrupos();

    // ------------------------------------------------------------------ registros

    /** Registra un apartamento en el catalogo. */
    public void registrarApartamento(Apartamento apartamento) {
        catalogo.agregar(apartamento);
    }

    /** Registra un huesped. */
    public void registrarHuesped(Huesped huesped) {
        huespedes.registrar(huesped);
    }

    /** Registra un empleado; si es recepcionista entra tambien a la rotacion de turnos. */
    public void registrarEmpleado(Empleado empleado) {
        empleados.registrar(empleado);
        if (empleado.getRol() == Rol.RECEPCIONISTA) {
            turnos.agregarRecepcionista(empleado);
        }
    }

    /** Define el periodo de una temporada (no se pueden cruzar). */
    public void definirTemporada(Temporada temporada, LocalDate desde, LocalDate hasta) {
        calendario.definirPeriodo(temporada, desde, hasta);
    }

    /** Cambia la tarifa por persona por noche de un apartamento en una temporada. */
    public void cambiarTarifa(String codigoApartamento, Temporada temporada, long valor) {
        apartamentoObligatorio(codigoApartamento).cambiarTarifa(temporada, valor);
    }

    /** Deja un apartamento fuera de servicio con su motivo. */
    public void ponerFueraDeServicio(String codigoApartamento, String motivo) {
        apartamentoObligatorio(codigoApartamento).ponerFueraDeServicio(motivo);
    }

    /** Saca un apartamento de fuera de servicio: queda pendiente de preparacion y entra a la cola. */
    public void reactivarApartamento(String codigoApartamento) {
        Apartamento apartamento = apartamentoObligatorio(codigoApartamento);
        apartamento.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        preparacion.agregar(apartamento, false, SIN_HORA, Reloj.ahora());
    }

    // ------------------------------------------------------------------ consultas

    public Apartamento buscarApartamento(String codigo) {
        return catalogo.buscar(codigo);
    }

    public Huesped buscarHuesped(String documento) {
        return huespedes.buscar(documento);
    }

    public Reserva buscarReserva(String codigo) {
        return gestorReservas.buscar(codigo);
    }

    public ListaDoble<Apartamento> apartamentos() {
        return catalogo.todos();
    }

    public ListaDoble<Huesped> huespedes() {
        return huespedes.todos();
    }

    public ListaDoble<Empleado> empleados() {
        return empleados.todos();
    }

    /** Todas las reservas (para los reportes y las pantallas). */
    public ListaDoble<Reserva> todasLasReservas() {
        return gestorReservas.todas();
    }

    /** Apartamentos libres para el rango y las personas, ordenados con el criterio escogido. */
    public ListaDoble<Apartamento> buscarDisponibles(LocalDate entrada, LocalDate salida,
                                                     int personas, Comparator<Apartamento> orden) {
        return catalogo.disponiblesOrdenados(new Estancia(entrada, salida), personas, orden);
    }

    /**
     * Arma la lista completa de ocupantes: el titular (ocupa cupo y paga si tiene 6 o mas)
     * mas los que se pasen. Si el titular ya venia en la lista, no se repite.
     */
    public ListaDoble<Ocupante> ocupantesConTitular(String documento, ListaDoble<Ocupante> otros) {
        Huesped titular = huespedObligatorio(documento);
        Ocupante yo = new Ocupante(titular.getNombre(), titular.getFechaNacimiento());
        ListaDoble<Ocupante> todos = new ListaDoble<>();
        todos.agregarAlFinal(yo);
        if (otros != null) {
            for (Ocupante o : otros) {
                if (!o.equals(yo)) {
                    todos.agregarAlFinal(o);
                }
            }
        }
        return todos;
    }

    /** Cotiza una estancia. Los ocupantes son todos los que se alojan. */
    public Cotizacion cotizar(String codigoApto, LocalDate entrada, LocalDate salida,
                              ListaDoble<Ocupante> ocupantes) {
        return cotizador.cotizar(apartamentoObligatorio(codigoApto), new Estancia(entrada, salida),
                ocupantes, calendario);
    }

    // ------------------------------------------------------------------ reservas

    /** Reserva por el portal: queda Pendiente, con folio y cargo, y entra a vencimientos. */
    public Reserva reservarPortal(String documento, String codigoApto, LocalDate entrada,
                                  LocalDate salida, ListaDoble<Ocupante> ocupantes,
                                  LocalTime horaLlegada) {
        return reservar(Canal.PORTAL, documento, codigoApto, entrada, salida, ocupantes, horaLlegada);
    }

    /** Reserva directa (recepcion): queda Confirmada con su folio. */
    public Reserva reservarDirecta(String documento, String codigoApto, LocalDate entrada,
                                   LocalDate salida, ListaDoble<Ocupante> ocupantes,
                                   LocalTime horaLlegada) {
        return reservar(Canal.DIRECTA, documento, codigoApto, entrada, salida, ocupantes, horaLlegada);
    }

    /** Lo comun de los tres canales: cotiza, crea la reserva, abre el folio y hace lo propio del canal. */
    private Reserva reservar(Canal canal, String documento, String codigoApto, LocalDate entrada,
                             LocalDate salida, ListaDoble<Ocupante> ocupantes, LocalTime horaLlegada) {
        Huesped titular = huespedObligatorio(documento);
        Apartamento apartamento = apartamentoObligatorio(codigoApto);
        Estancia estancia = new Estancia(entrada, salida);
        ListaDoble<Ocupante> todos = ocupantesConTitular(documento, ocupantes);
        Cotizacion cotizacion = cotizador.cotizar(apartamento, estancia, todos, calendario);
        LocalDateTime ahora = Reloj.ahora();

        Reserva reserva = gestorReservas.crear(titular, apartamento, estancia, todos, canal,
                horaLlegada, cotizacion.getTotal(), ahora);

        folios.abrirFolio(reserva.getCodigo());
        if (cotizacion.getTotal() > 0) {
            folios.registrarCargo(reserva.getCodigo(), "Estancia " + apartamento.getCodigo() + " "
                    + estancia.noches() + " noches", cotizacion.getTotal(), ahora);
        }
        if (canal == Canal.PORTAL) {
            vencimientos.agregar(reserva);
        } else if (entrada.equals(ahora.toLocalDate())
                && apartamento.getEstado() == EstadoApartamento.PENDIENTE_PREPARACION) {
            // el apartamento esta esperando preparacion y hoy llega un grupo: pasa a prioridad 1
            preparacion.subirAPrioridad1(apartamento.getCodigo(), reserva.getHoraLlegada());
        }
        return reserva;
    }

    /** Cuanto hay que pagar como minimo de anticipo (30 % del total). */
    public long anticipoRequerido(String codigoReserva) {
        return reservaObligatoria(codigoReserva).getValorTotal() * PORCENTAJE_ANTICIPO / 100;
    }

    /** Paga el anticipo de una reserva Pendiente: registra el pago, la confirma y la saca de vencimientos. */
    public void pagarAnticipo(String codigoReserva, long valor) {
        Reserva reserva = reservaObligatoria(codigoReserva);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new TransicionEstadoInvalidaException("La reserva " + codigoReserva + " esta "
                    + reserva.getEstado() + " y solo se paga anticipo si esta Pendiente");
        }
        long minimo = anticipoRequerido(codigoReserva);
        if (reserva.getValorTotal() > 0) {
            if (valor < minimo || valor <= 0) {
                throw new IllegalArgumentException("El anticipo debe ser al menos el " + PORCENTAJE_ANTICIPO
                        + " % del total (" + minimo + " pesos)");
            }
            folios.registrarPago(codigoReserva, "Anticipo", valor, Reloj.ahora());
        }
        gestorReservas.confirmar(reserva);
        vencimientos.quitar(reserva);
    }

    /** Pone en la cola del canal externo una solicitud de la plataforma (simulada). */
    public void recibirSolicitudExterna(SolicitudExterna solicitud) {
        canalExterno.recibir(solicitud);
    }

    /**
     * Procesa la siguiente solicitud del canal externo, en orden de llegada. Si choca con una
     * reserva vigente registra el conflicto de canal. Devuelve un mensaje con lo que paso.
     * Cola vacia: EstructuraVaciaException.
     */
    public String procesarSiguienteSolicitud() {
        SolicitudExterna solicitud = canalExterno.siguiente();
        try {
            Reserva reserva = reservar(Canal.EXTERNA, solicitud.getDocumentoHuesped(),
                    solicitud.getCodigoApartamento(), solicitud.getEntrada(), solicitud.getSalida(),
                    solicitud.getOcupantes(), solicitud.getHoraLlegada());
            return "Reserva " + reserva.getCodigo() + " creada para " + solicitud.getCodigoApartamento();
        } catch (ReservaSolapadaException e) {
            canalExterno.registrarConflicto(solicitud, e.getMessage(), Reloj.ahora());
            return "Conflicto de canal: " + e.getMessage();
        } catch (RuntimeException e) {
            return "Solicitud rechazada: " + e.getMessage();
        }
    }

    /** Cuanto se devolveria hoy si se cancelara la reserva (100 %, 50 % o 0 % de lo pagado). */
    public long consultarDevolucion(String codigoReserva) {
        Reserva reserva = reservaObligatoria(codigoReserva);
        long pagado = folios.totalPagado(codigoReserva);
        return gestorReservas.calcularDevolucion(reserva, pagado, Reloj.hoy());
    }

    /** Cancela la reserva y registra la devolucion en el folio. Devuelve lo devuelto. */
    public long cancelarReserva(String codigoReserva) {
        Reserva reserva = reservaObligatoria(codigoReserva);
        long devolucion = consultarDevolucion(codigoReserva);
        boolean estabaPendiente = reserva.getEstado() == EstadoReserva.PENDIENTE;
        gestorReservas.cancelar(reserva); // si no se puede cancelar, lanza y no se toca el folio
        if (estabaPendiente) {
            vencimientos.quitar(reserva);
        }
        if (devolucion > 0) {
            folios.registrarDevolucion(codigoReserva, devolucion, Reloj.ahora());
        }
        return devolucion;
    }

    /**
     * Elimina un huesped. No se deja si tiene reservas activas (Pendiente, Confirmada o En curso),
     * porque esas reservas quedarian sin titular. Devuelve false si el huesped no existe.
     */
    public boolean eliminarHuesped(String documento) {
        Huesped huesped = huespedes.buscar(documento);
        if (huesped == null) {
            return false;
        }
        for (Reserva reserva : gestorReservas.reservasDe(huesped)) {
            if (reserva.estaActiva()) {
                throw new IllegalStateException("El huesped " + documento + " tiene la reserva "
                        + reserva.getCodigo() + " activa. Primero hay que cancelarla o terminarla.");
            }
        }
        return huespedes.eliminar(documento);
    }

    /**
     * Devuelve la reserva solo si el titular es el huesped con ese documento.
     * Si la reserva es de otra persona da IllegalArgumentException.
     * Asi un huesped no puede tocar las reservas de los demas.
     */
    public Reserva reservaDelHuesped(String documento, String codigoReserva) {
        Huesped huesped = huespedObligatorio(documento);
        Reserva reserva = reservaObligatoria(codigoReserva);
        if (!reserva.getTitular().equals(huesped)) {
            throw new IllegalArgumentException("La reserva " + codigoReserva + " no es suya, no la puede cambiar");
        }
        return reserva;
    }

    /** Un huesped cancela una de sus reservas. Si la reserva es de otro, da error y no se cancela. */
    public long cancelarReservaDe(String documento, String codigoReserva) {
        reservaDelHuesped(documento, codigoReserva);
        return cancelarReserva(codigoReserva);
    }

    /** Un huesped paga el anticipo de una de sus reservas. Si la reserva es de otro, da error. */
    public void pagarAnticipoDe(String documento, String codigoReserva, long valor) {
        reservaDelHuesped(documento, codigoReserva);
        pagarAnticipo(codigoReserva, valor);
    }

    /** Marca la reserva como No-show. */
    public void marcarNoShow(String codigoReserva) {
        gestorReservas.marcarNoShow(reservaObligatoria(codigoReserva));
    }

    // ------------------------------------------------------------------ llegadas y salidas

    /** Check-in (solo si hoy es la entrada y el apartamento esta Preparado). */
    public void checkIn(String codigoReserva) {
        gestorReservas.checkIn(reservaObligatoria(codigoReserva), Reloj.hoy());
    }

    /** Check-out: el apartamento entra a la cola de preparacion (con entrada hoy y hora si tiene). */
    public void checkOut(String codigoReserva) {
        Reserva reserva = reservaObligatoria(codigoReserva);
        gestorReservas.checkOut(reserva);
        Apartamento apartamento = reserva.getApartamento();
        LocalDateTime ahora = Reloj.ahora();
        Reserva siguiente = gestorReservas.tieneEntradaHoy(apartamento, ahora.toLocalDate());
        if (siguiente != null) {
            preparacion.agregar(apartamento, true, siguiente.getHoraLlegada(), ahora);
        } else {
            preparacion.agregar(apartamento, false, SIN_HORA, ahora);
        }
    }

    // ------------------------------------------------------------------ preparacion

    /** Saca el primero de la cola y lo pasa a En preparacion. */
    public Apartamento iniciarPreparacion() {
        return preparacion.iniciarSiguiente(Reloj.ahora());
    }

    /** Termina la preparacion: el apartamento queda Preparado. */
    public void terminarPreparacion(String codigoApto) {
        preparacion.terminar(codigoApto, Reloj.ahora());
    }

    /** La cola de preparacion en orden de prioridad. */
    public ListaDoble<TareaPreparacion> colaPreparacion() {
        return preparacion.pendientesEnOrden();
    }

    /** La ruta mas corta que recorre los pendientes en el orden de la cola. Cola vacia: null. */
    public Ruta rutaPreparacion() {
        ListaDoble<TareaPreparacion> cola = colaPreparacion();
        if (cola.estaVacia()) {
            return null;
        }
        ListaDoble<String> codigos = new ListaDoble<>();
        for (TareaPreparacion tarea : cola) {
            codigos.agregarAlFinal(tarea.getApartamento().getCodigo());
        }
        return mapa.rutaPreparacion(codigos);
    }

    // ------------------------------------------------------------------ folio

    public Movimiento registrarCargo(String codigoReserva, String concepto, long valor) {
        return folios.registrarCargo(codigoReserva, concepto, valor, Reloj.ahora());
    }

    public Movimiento registrarPago(String codigoReserva, String concepto, long valor) {
        return folios.registrarPago(codigoReserva, concepto, valor, Reloj.ahora());
    }

    /** Deshace la ultima operacion del folio registrando el movimiento contrario. */
    public Movimiento deshacerUltima(String codigoReserva) {
        return folios.deshacerUltima(codigoReserva, Reloj.ahora());
    }

    /** El folio de una reserva. Si no existe: IllegalArgumentException. */
    public Folio folio(String codigoReserva) {
        Folio folio = folios.buscar(codigoReserva);
        if (folio == null) {
            throw new IllegalArgumentException("No existe el folio de la reserva " + codigoReserva);
        }
        return folio;
    }

    // ------------------------------------------------------------------ vencimientos y grupos

    /** Cancela las reservas Pendientes cuyo plazo de 24 horas ya paso y devuelve cuales fueron. */
    public ListaDoble<Reserva> revisarVencimientos() {
        ListaDoble<Reserva> vencidas = vencimientos.vencidas(Reloj.ahora());
        for (Reserva reserva : vencidas) {
            gestorReservas.cancelar(reserva); // sus noches quedan libres
        }
        return vencidas;
    }

    /** Busca la combinacion con menos apartamentos libres que aloje a todo el grupo. */
    public ListaDoble<Apartamento> asignarGrupoGrande(LocalDate entrada, LocalDate salida, int personas) {
        ListaDoble<Apartamento> libres = catalogo.disponibles(new Estancia(entrada, salida), 1);
        return asignador.mejorCombinacion(libres, personas);
    }

    // ------------------------------------------------------------------ turnos y huespedes

    public Empleado turnoActual() {
        return turnos.turnoActual();
    }

    /** Pasa el turno al siguiente recepcionista y lo devuelve. */
    public Empleado pasarTurno() {
        turnos.pasarTurno();
        return turnos.turnoActual();
    }

    /** Agrega un favorito al huesped. Devuelve false si ya estaba. */
    public boolean agregarFavorito(String documento, String codigoApto) {
        Huesped huesped = huespedObligatorio(documento);
        apartamentoObligatorio(codigoApto);
        return huesped.agregarFavorito(codigoApto.trim());
    }

    /** Quita un favorito del huesped. Devuelve false si no estaba. */
    public boolean quitarFavorito(String documento, String codigoApto) {
        return huespedObligatorio(documento).quitarFavorito(codigoApto == null ? null : codigoApto.trim());
    }

    public ListaDoble<Reserva> historial(String documento) {
        return huespedObligatorio(documento).getHistorial();
    }

    public ListaDoble<Reserva> reservasDe(String documento) {
        return gestorReservas.reservasDe(huespedObligatorio(documento));
    }

    // ------------------------------------------------------------------ getters para pantallas y reportes

    public MapaEdificio mapa() { return mapa; }
    /** La fecha y hora que usa todo el sistema (la del Reloj). */
    public LocalDateTime reloj() { return Reloj.ahora(); }
    public CanalExterno canalExterno() { return canalExterno; }
    public GestorPreparacion preparacion() { return preparacion; }
    public GestorFolio folios() { return folios; }
    public CalendarioTemporadas temporadas() { return calendario; }
    public CatalogoApartamentos catalogo() { return catalogo; }
    public RegistroHuespedes registroHuespedes() { return huespedes; }
    public GestorReservas gestorReservas() { return gestorReservas; }
    public VencimientoReservas vencimientos() { return vencimientos; }
    public TurnosRecepcion turnos() { return turnos; }
    public RegistroEmpleados registroEmpleados() { return empleados; }

    // ------------------------------------------------------------------ privados

    private Apartamento apartamentoObligatorio(String codigo) {
        Apartamento apartamento = catalogo.buscar(codigo);
        if (apartamento == null) {
            throw new IllegalArgumentException("No existe el apartamento " + codigo);
        }
        return apartamento;
    }

    private Huesped huespedObligatorio(String documento) {
        Huesped huesped = huespedes.buscar(documento);
        if (huesped == null) {
            throw new IllegalArgumentException("No existe el huesped con documento " + documento);
        }
        return huesped;
    }

    private Reserva reservaObligatoria(String codigo) {
        Reserva reserva = gestorReservas.buscar(codigo);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe la reserva " + codigo);
        }
        return reserva;
    }
}
