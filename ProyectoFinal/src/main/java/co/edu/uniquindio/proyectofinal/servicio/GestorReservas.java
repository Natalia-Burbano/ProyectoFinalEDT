package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;
import co.edu.uniquindio.proyectofinal.excepciones.ReservaSolapadaException;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.Canal;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/** Crea y maneja el ciclo de vida de las reservas de todos los canales. */
public class GestorReservas {

    /** Con estos dias o mas de anticipacion se devuelve todo lo pagado. */
    public static final int DIAS_DEVOLUCION_TOTAL = 7;
    /** Con estos dias o mas (y menos de 7) se devuelve la mitad. */
    public static final int DIAS_DEVOLUCION_MITAD = 2;

    private final ListaDoble<Reserva> reservas = new ListaDoble<>();
    private int consecutivo = 0;

    /**
     * Crea una reserva. Valida en este orden: fecha de entrada no pasada, apartamento en servicio,
     * capacidad y solapamiento. Portal queda PENDIENTE; Directa y Externa, CONFIRMADA.
     * La lista de ocupantes debe incluir al titular.
     */
    public Reserva crear(Huesped titular, Apartamento apartamento, Estancia estancia,
                         ListaDoble<Ocupante> ocupantes, Canal canal, LocalTime horaLlegada,
                         long valorTotal, LocalDateTime ahora) {
        if (titular == null || apartamento == null || estancia == null
                || ocupantes == null || canal == null || ahora == null) {
            throw new IllegalArgumentException("Faltan datos para crear la reserva");
        }
        if (ocupantes.estaVacia()) {
            throw new IllegalArgumentException("La reserva necesita al menos un ocupante");
        }
        if (valorTotal < 0) {
            throw new IllegalArgumentException("El valor total no puede ser negativo");
        }
        // 1. No se reserva una fecha que ya paso
        if (estancia.getEntrada().isBefore(ahora.toLocalDate())) {
            throw new FechasInvalidasException("No se puede reservar una fecha que ya paso: " + estancia.getEntrada());
        }
        // 2. El apartamento debe estar en servicio
        if (apartamento.getEstado() == EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new IllegalStateException("El apartamento " + apartamento.getCodigo() + " esta fuera de servicio");
        }
        // 3. Capacidad
        if (ocupantes.tamanio() > apartamento.getCapacidad()) {
            throw new CapacidadExcedidaException("El apartamento " + apartamento.getCodigo() + " admite "
                    + apartamento.getCapacidad() + " personas y se pidieron " + ocupantes.tamanio());
        }
        // 4. Solapamiento (se revisa solo en el arbol de ese apartamento)
        Reserva choque = apartamento.getReservas().reservaQueChoca(estancia);
        if (choque != null) {
            throw new ReservaSolapadaException("El apartamento " + apartamento.getCodigo()
                    + " ya tiene la reserva " + choque.getCodigo() + " (" + choque.getEstancia() + ")");
        }
        consecutivo++;
        String codigo = String.format("R%04d", consecutivo);
        Reserva reserva = new Reserva(codigo, titular, apartamento, estancia, ocupantes,
                canal, horaLlegada, valorTotal, ahora);
        if (canal != Canal.PORTAL) {
            reserva.cambiarEstado(EstadoReserva.CONFIRMADA);
        }
        apartamento.getReservas().insertar(reserva);
        reservas.agregarAlFinal(reserva);
        apartamento.sumarReserva();
        return reserva;
    }

    /** Busca una reserva por codigo recorriendo la lista. Devuelve null si no existe. */
    public Reserva buscar(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Reserva r : reservas) {
            if (r.getCodigo().equals(codigo.trim())) {
                return r;
            }
        }
        return null;
    }

    /** Las reservas de un huesped (donde es titular). */
    public ListaDoble<Reserva> reservasDe(Huesped huesped) {
        if (huesped == null) {
            throw new IllegalArgumentException("El huesped no puede ser nulo");
        }
        ListaDoble<Reserva> resultado = new ListaDoble<>();
        for (Reserva r : reservas) {
            if (r.getTitular().equals(huesped)) {
                resultado.agregarAlFinal(r);
            }
        }
        return resultado;
    }

    /** Una copia de la lista con todas las reservas. */
    public ListaDoble<Reserva> todas() {
        return reservas.copia();
    }

    /** Pendiente -> Confirmada. */
    public void confirmar(Reserva reserva) {
        obligatoria(reserva).cambiarEstado(EstadoReserva.CONFIRMADA);
    }

    /** Cancela la reserva. Ya no cuenta para los solapamientos: sus noches quedan libres. */
    public void cancelar(Reserva reserva) {
        obligatoria(reserva).cambiarEstado(EstadoReserva.CANCELADA);
    }

    /** Marca la reserva como No-show (el huesped no llego). */
    public void marcarNoShow(Reserva reserva) {
        obligatoria(reserva).cambiarEstado(EstadoReserva.NO_SHOW);
    }

    /**
     * Check-in: la reserva debe estar CONFIRMADA, hoy debe ser la entrada y el apartamento
     * debe estar PREPARADO. Queda EN_CURSO y el apartamento OCUPADO.
     */
    public void checkIn(Reserva reserva, LocalDate hoy) {
        obligatoria(reserva);
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha de hoy no puede ser nula");
        }
        Apartamento apartamento = reserva.getApartamento();
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new TransicionEstadoInvalidaException("La reserva " + reserva.getCodigo()
                    + " esta " + reserva.getEstado() + " y para el check-in debe estar Confirmada");
        }
        if (!hoy.equals(reserva.getEstancia().getEntrada())) {
            throw new FechasInvalidasException("Hoy (" + hoy + ") no es la fecha de entrada ("
                    + reserva.getEstancia().getEntrada() + ")");
        }
        if (apartamento.getEstado() != EstadoApartamento.PREPARADO) {
            throw new TransicionEstadoInvalidaException("El apartamento " + apartamento.getCodigo()
                    + " no esta preparado (estado: " + apartamento.getEstado() + ")");
        }
        reserva.cambiarEstado(EstadoReserva.EN_CURSO);
        apartamento.cambiarEstado(EstadoApartamento.OCUPADO);
    }

    /**
     * Check-out: la reserva pasa a FINALIZADA, el apartamento a PENDIENTE_PREPARACION y la
     * reserva se guarda en el historial del titular.
     */
    public void checkOut(Reserva reserva) {
        obligatoria(reserva);
        reserva.cambiarEstado(EstadoReserva.FINALIZADA);
        reserva.getApartamento().cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        reserva.getTitular().agregarAlHistorial(reserva);
    }

    /** La reserva CONFIRMADA del apartamento que entra hoy, o null. La usa la cola de preparacion. */
    public Reserva tieneEntradaHoy(Apartamento apartamento, LocalDate hoy) {
        if (apartamento == null || hoy == null) {
            throw new IllegalArgumentException("El apartamento y la fecha son obligatorios");
        }
        for (Reserva r : apartamento.getReservas().activas()) {
            if (r.getEstado() == EstadoReserva.CONFIRMADA && r.getEstancia().getEntrada().equals(hoy)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Cuanto se devuelve al cancelar: con 7 dias o mas antes de la entrada, todo lo pagado;
     * de 2 a 6 dias, la mitad; con menos de 2 dias, nada.
     */
    public long calcularDevolucion(Reserva reserva, long totalPagado, LocalDate hoy) {
        obligatoria(reserva);
        if (hoy == null) {
            throw new IllegalArgumentException("La fecha de hoy no puede ser nula");
        }
        if (totalPagado <= 0) {
            return 0;
        }
        long dias = ChronoUnit.DAYS.between(hoy, reserva.getEstancia().getEntrada());
        if (dias >= DIAS_DEVOLUCION_TOTAL) {
            return totalPagado;
        }
        if (dias >= DIAS_DEVOLUCION_MITAD) {
            return totalPagado / 2;
        }
        return 0;
    }

    private Reserva obligatoria(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        return reserva;
    }
}
