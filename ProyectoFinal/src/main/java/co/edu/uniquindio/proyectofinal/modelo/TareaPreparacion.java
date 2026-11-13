package co.edu.uniquindio.proyectofinal.modelo;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Lo que entra a la cola de preparacion: un apartamento que quedo pendiente de preparar.
 * Si tiene una entrada para hoy (prioridad 1) guarda la hora en que llega el siguiente grupo.
 */
public class TareaPreparacion {

    private final Apartamento apartamento;
    private final boolean entradaHoy;          // true = prioridad 1
    private final LocalTime horaLlegada;       // null si no tiene entrada hoy
    private final LocalDateTime pendienteDesde; // cuando quedo pendiente (para medir el tiempo)

    /**
     * Crea la tarea. Si tiene entrada hoy, la hora de llegada es obligatoria.
     */
    public TareaPreparacion(Apartamento apartamento, boolean entradaHoy, LocalTime horaLlegada,
                            LocalDateTime pendienteDesde) {
        if (apartamento == null || pendienteDesde == null) {
            throw new IllegalArgumentException("El apartamento y la hora en que quedo pendiente son obligatorios");
        }
        if (entradaHoy && horaLlegada == null) {
            throw new IllegalArgumentException("Si tiene entrada hoy hay que decir la hora de llegada");
        }
        this.apartamento = apartamento;
        this.entradaHoy = entradaHoy;
        this.horaLlegada = entradaHoy ? horaLlegada : null;
        this.pendienteDesde = pendienteDesde;
    }

    /**
     * 1 si tiene entrada hoy, 2 si no.
     */
    public int prioridad() {
        return entradaHoy ? 1 : 2;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    public String getCodigoApartamento() {
        return apartamento.getCodigo();
    }

    public boolean isEntradaHoy() {
        return entradaHoy;
    }

    public LocalTime getHoraLlegada() {
        return horaLlegada;
    }

    public LocalDateTime getPendienteDesde() {
        return pendienteDesde;
    }

    /**
     * Dos tareas son iguales si son del mismo apartamento.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TareaPreparacion)) {
            return false;
        }
        TareaPreparacion otra = (TareaPreparacion) o;
        return apartamento.equals(otra.apartamento);
    }

    @Override
    public int hashCode() {
        return apartamento.hashCode();
    }

    @Override
    public String toString() {
        if (entradaHoy) {
            return apartamento.getCodigo() + " (prioridad 1, llegan a las " + horaLlegada + ")";
        }
        return apartamento.getCodigo() + " (prioridad 2)";
    }
}
