package co.edu.uniquindio.proyectofinal.excepciones;

/** Dos reservas activas del mismo apartamento comparten al menos una noche. */
public class ReservaSolapadaException extends RuntimeException {

    public ReservaSolapadaException(String mensaje) {
        super(mensaje);
    }
}