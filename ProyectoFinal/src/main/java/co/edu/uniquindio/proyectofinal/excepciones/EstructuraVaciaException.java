package co.edu.uniquindio.proyectofinal.excepciones;

/** Se intento sacar o ver algo de una estructura vacia. */
public class EstructuraVaciaException extends RuntimeException {
    public EstructuraVaciaException(String mensaje) { super(mensaje); }
}