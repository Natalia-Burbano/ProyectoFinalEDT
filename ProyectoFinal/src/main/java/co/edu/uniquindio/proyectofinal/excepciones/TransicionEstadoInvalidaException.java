package co.edu.uniquindio.proyectofinal.excepciones;

/** Cambio de estado no permitido (apartamento o reserva). */
public class TransicionEstadoInvalidaException extends RuntimeException {
    public TransicionEstadoInvalidaException(String mensaje) { super(mensaje); }
}