package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.comparadores.ComparadoresPreparacion;
import co.edu.uniquindio.proyectofinal.estructuras.ColaPrioridad;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;

import java.time.LocalDateTime;

/**
 * Lleva las reservas Pendientes ordenadas por su hora de vencimiento (24 horas despues de creadas).
 * Solo dice cuales ya vencieron; cancelarlas lo hace SistemaAlojamiento.
 */
public class VencimientoReservas {

    private final ColaPrioridad<Reserva> pendientes;

    /** Crea la cola de vencimientos vacia. */
    public VencimientoReservas() {
        pendientes = new ColaPrioridad<>(ComparadoresPreparacion.VENCIMIENTO);
    }

    /**
     * Mete una reserva. Solo se aceptan reservas en estado PENDIENTE.
     */
    public void agregar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalArgumentException("Solo se vigilan reservas pendientes, y la " + reserva.getCodigo()
                    + " esta " + reserva.getEstado());
        }
        pendientes.insertar(reserva);
    }

    /**
     * Quita una reserva (cuando paga el anticipo o la cancelan). Devuelve true si estaba.
     */
    public boolean quitar(Reserva reserva) {
        return pendientes.eliminar(reserva);
    }

    /**
     * Saca y devuelve todas las reservas que ya vencieron a esta hora.
     * Como la cola esta ordenada por vencimiento, se van sacando del frente hasta encontrar una que no ha vencido.
     */
    public ListaDoble<Reserva> vencidas(LocalDateTime ahora) {
        ListaDoble<Reserva> lista = new ListaDoble<>();

        while (!pendientes.estaVacia() && !pendientes.verPrimero().getVenceEn().isAfter(ahora)) {
            lista.agregarAlFinal(pendientes.extraer());
        }
        return lista;
    }

    /**
     * La reserva que vence primero, o null si no hay ninguna.
     */
    public Reserva proximaAVencer() {
        if (pendientes.estaVacia()) {
            return null;
        }
        return pendientes.verPrimero();
    }

    /** Cuantas reservas pendientes se estan vigilando. */
    public int cantidad() {
        return pendientes.tamanio();
    }
}
