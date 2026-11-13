package co.edu.uniquindio.proyectofinal.excepciones;

/** Salida antes de la entrada, fecha pasada o temporadas cruzadas. */
public class FechasInvalidasException extends RuntimeException {
    public FechasInvalidasException(String mensaje) { super(mensaje); }
}
