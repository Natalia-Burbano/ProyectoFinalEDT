package co.edu.uniquindio.proyectofinal.excepciones;

/** Mas personas que la capacidad, o un grupo grande que no cabe. */
public class CapacidadExcedidaException extends RuntimeException {
    public CapacidadExcedidaException(String mensaje) { super(mensaje); }
}