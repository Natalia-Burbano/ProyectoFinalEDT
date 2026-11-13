package co.edu.uniquindio.proyectofinal.modelo;

/** Ciclo de vida de una reserva. */
public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    EN_CURSO("En curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada"),
    NO_SHOW("No-show");

    private final String texto;

    EstadoReserva(String texto) {
        this.texto = texto;
    }

    /** Texto para mostrar en pantalla. */
    public String getTexto() {
        return texto;
    }

    /**
     * Pendiente -> Confirmada o Cancelada; Confirmada -> En curso, Cancelada o No-show;
     * En curso -> Finalizada. Los demas estados no cambian mas.
     */
    public boolean puedeCambiarA(EstadoReserva nuevo) {
        if (nuevo == null) {
            return false;
        }
        return switch (this) {
            case PENDIENTE -> nuevo == CONFIRMADA || nuevo == CANCELADA;
            case CONFIRMADA -> nuevo == EN_CURSO || nuevo == CANCELADA || nuevo == NO_SHOW;
            case EN_CURSO -> nuevo == FINALIZADA;
            case FINALIZADA, CANCELADA, NO_SHOW -> false;
        };
    }

    /** Una reserva activa es la que todavia ocupa noches: Pendiente, Confirmada o En curso. */
    public boolean esActiva() {
        return this == PENDIENTE || this == CONFIRMADA || this == EN_CURSO;
    }

    @Override
    public String toString() {
        return texto;
    }
}
