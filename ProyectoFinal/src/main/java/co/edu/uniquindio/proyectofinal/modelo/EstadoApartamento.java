package co.edu.uniquindio.proyectofinal.modelo;

/** Estados operativos de un apartamento y los cambios permitidos entre ellos. */
public enum EstadoApartamento {
    PREPARADO("Preparado"),
    OCUPADO("Ocupado"),
    PENDIENTE_PREPARACION("Pendiente de preparacion"),
    EN_PREPARACION("En preparacion"),
    FUERA_DE_SERVICIO("Fuera de servicio");

    private final String texto;

    EstadoApartamento(String texto) {
        this.texto = texto;
    }

    /** Texto para mostrar en pantalla. */
    public String getTexto() {
        return texto;
    }

    /**
     * Dice si se puede pasar de este estado al nuevo.
     * Preparado -> Ocupado; Ocupado -> Pendiente; Pendiente -> En preparacion;
     * En preparacion -> Preparado; cualquiera menos Ocupado -> Fuera de servicio;
     * Fuera de servicio -> Pendiente.
     */
    public boolean puedeCambiarA(EstadoApartamento nuevo) {
        if (nuevo == null) {
            return false;
        }
        if (nuevo == FUERA_DE_SERVICIO) {
            return this != OCUPADO && this != FUERA_DE_SERVICIO;
        }
        return switch (this) {
            case PREPARADO -> nuevo == OCUPADO;
            case OCUPADO -> nuevo == PENDIENTE_PREPARACION;
            case PENDIENTE_PREPARACION -> nuevo == EN_PREPARACION;
            case EN_PREPARACION -> nuevo == PREPARADO;
            case FUERA_DE_SERVICIO -> nuevo == PENDIENTE_PREPARACION;
        };
    }

    @Override
    public String toString() {
        return texto;
    }
}
