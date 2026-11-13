package co.edu.uniquindio.proyectofinal.modelo;

import java.time.LocalDateTime;

/**
 * Una solicitud del canal externo que se rechazo, con el motivo y la fecha.
 */
public class ConflictoCanal {

    private final SolicitudExterna solicitud;
    private final String motivo;
    private final LocalDateTime fecha;

    /**
     * Crea el conflicto. Los tres datos son obligatorios.
     */
    public ConflictoCanal(SolicitudExterna solicitud, String motivo, LocalDateTime fecha) {
        if (solicitud == null || motivo == null || motivo.isBlank() || fecha == null) {
            throw new IllegalArgumentException("La solicitud, el motivo y la fecha son obligatorios");
        }
        this.solicitud = solicitud;
        this.motivo = motivo;
        this.fecha = fecha;
    }

    public SolicitudExterna getSolicitud() {
        return solicitud;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return solicitud + " -> " + motivo;
    }
}
