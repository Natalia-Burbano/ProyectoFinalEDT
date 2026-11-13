package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.Canal;
import co.edu.uniquindio.proyectofinal.modelo.ConflictoCanal;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.FilaReporte;
import co.edu.uniquindio.proyectofinal.modelo.Folio;
import co.edu.uniquindio.proyectofinal.modelo.Movimiento;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.modelo.Temporada;
import co.edu.uniquindio.proyectofinal.modelo.TipoMovimiento;

import java.time.LocalDate;

/** Reportes del administrador: cada metodo recorre las estructuras y devuelve filas (etiqueta, valor). */
public class Reportes {

    private final SistemaAlojamiento sistema;

    public Reportes(SistemaAlojamiento sistema) {
        if (sistema == null) {
            throw new IllegalArgumentException("El sistema no puede ser nulo");
        }
        this.sistema = sistema;
    }

    /** Porcentaje de noches ocupadas de cada apartamento en el periodo (la noche de salida no cuenta). */
    public ListaDoble<FilaReporte> ocupacionPorApartamento(LocalDate desde, LocalDate hasta) {
        Estancia periodo = new Estancia(desde, hasta);   // da FechasInvalidasException si hasta <= desde
        ListaDoble<FilaReporte> filas = new ListaDoble<>();
        for (Apartamento apto : sistema.apartamentos()) {
            int ocupadas = 0;
            for (Reserva reserva : apto.getReservas().inorden()) {
                if (ocupaNoches(reserva)) {
                    ocupadas += nochesEnPeriodo(reserva, periodo);
                }
            }
            filas.agregarAlFinal(new FilaReporte(apto.getCodigo(), ocupadas * 100.0 / periodo.noches()));
        }
        return filas;
    }

    /** Noches ocupadas en cada temporada (de todas las reservas que ocupan noches). */
    public ListaDoble<FilaReporte> ocupacionPorTemporada() {
        int[] noches = new int[Temporada.values().length];
        for (Reserva reserva : todasLasReservas()) {
            if (ocupaNoches(reserva)) {
                for (LocalDate noche : reserva.getEstancia()) {
                    noches[sistema.temporadas().temporadaDe(noche).ordinal()]++;
                }
            }
        }
        ListaDoble<FilaReporte> filas = new ListaDoble<>();
        for (Temporada temporada : Temporada.values()) {
            filas.agregarAlFinal(new FilaReporte(legible(temporada.name()), noches[temporada.ordinal()]));
        }
        return filas;
    }

    /** Pagos registrados en los folios entre dos fechas (las dos incluidas). */
    public ListaDoble<FilaReporte> ingresosPorPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new FechasInvalidasException("El periodo no es valido");
        }
        long pagos = 0;
        long devoluciones = 0;
        for (Folio folio : sistema.folios().todos()) {
            for (Movimiento movimiento : folio) {
                if (movimiento.getTipo() != TipoMovimiento.PAGO) {
                    continue;
                }
                LocalDate fecha = movimiento.getFechaHora().toLocalDate();
                if (fecha.isBefore(desde) || fecha.isAfter(hasta)) {
                    continue;
                }
                if (movimiento.getValor() >= 0) {
                    pagos += movimiento.getValor();
                } else {
                    devoluciones += movimiento.getValor();      // devoluciones y pagos deshechos (negativos)
                }
            }
        }
        ListaDoble<FilaReporte> filas = new ListaDoble<>();
        filas.agregarAlFinal(new FilaReporte("Pagos recibidos", pagos));
        filas.agregarAlFinal(new FilaReporte("Devoluciones y correcciones", devoluciones));
        filas.agregarAlFinal(new FilaReporte("Ingresos netos", pagos + devoluciones));
        return filas;
    }

    /** Reservas por canal, contando tambien las canceladas y los no-show. */
    public ListaDoble<FilaReporte> reservasPorCanal() {
        int[] porCanal = new int[Canal.values().length];
        int canceladas = 0;
        int noShow = 0;
        for (Reserva reserva : todasLasReservas()) {
            porCanal[reserva.getCanal().ordinal()]++;
            if (reserva.getEstado() == EstadoReserva.CANCELADA) {
                canceladas++;
            }
            if (reserva.getEstado() == EstadoReserva.NO_SHOW) {
                noShow++;
            }
        }
        ListaDoble<FilaReporte> filas = new ListaDoble<>();
        for (Canal canal : Canal.values()) {
            filas.agregarAlFinal(new FilaReporte(legible(canal.name()), porCanal[canal.ordinal()]));
        }
        filas.agregarAlFinal(new FilaReporte("Canceladas", canceladas));
        filas.agregarAlFinal(new FilaReporte("No-show", noShow));
        return filas;
    }

    /** Total de conflictos del canal externo y una fila por cada uno. */
    public ListaDoble<FilaReporte> conflictosDeCanal() {
        CanalExterno canal = sistema.canalExterno();
        ListaDoble<FilaReporte> filas = new ListaDoble<>();
        filas.agregarAlFinal(new FilaReporte("Total de conflictos", canal.cantidadConflictos()));
        for (ConflictoCanal conflicto : canal.conflictos()) {
            String etiqueta = conflicto.getFecha() + " " + conflicto.getSolicitud().getCodigoApartamento()
                    + ": " + conflicto.getMotivo();
            filas.agregarAlFinal(new FilaReporte(etiqueta, 1));
        }
        return filas;
    }

    /** Tiempo promedio que tarda la preparacion de un apartamento. */
    public ListaDoble<FilaReporte> tiempoPromedioPreparacion() {
        double promedio = sistema.preparacion().tiempoPromedioMinutos();
        ListaDoble<FilaReporte> filas = new ListaDoble<>();
        filas.agregarAlFinal(new FilaReporte("Tiempo promedio de preparacion (minutos)", promedio));
        return filas;
    }

    // ---------- metodos privados ----------

    /** Todas las reservas del alojamiento (las guarda el arbol de cada apartamento). */
    private ListaDoble<Reserva> todasLasReservas() {
        ListaDoble<Reserva> todas = new ListaDoble<>();
        for (Apartamento apto : sistema.apartamentos()) {
            for (Reserva reserva : apto.getReservas().inorden()) {
                todas.agregarAlFinal(reserva);
            }
        }
        return todas;
    }

    /** Las canceladas y los no-show no ocupan noches. */
    private boolean ocupaNoches(Reserva reserva) {
        return reserva.getEstado() != EstadoReserva.CANCELADA
                && reserva.getEstado() != EstadoReserva.NO_SHOW;
    }

    /** Cuantas noches de la reserva caen dentro del periodo. */
    private int nochesEnPeriodo(Reserva reserva, Estancia periodo) {
        int cuenta = 0;
        for (LocalDate noche : reserva.getEstancia()) {
            if (periodo.contiene(noche)) {
                cuenta++;
            }
        }
        return cuenta;
    }

    /** PORTAL -> Portal */
    private String legible(String nombre) {
        return nombre.charAt(0) + nombre.substring(1).toLowerCase();
    }
}