package co.edu.uniquindio.proyectofinal.util;


import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fecha y hora que usa todo el sistema. Se puede fijar para pruebas y demo. */
public class Reloj {

    private static LocalDateTime fija = null;   // null = hora real

    private Reloj() { }

    /** Fecha y hora actuales (la fija si hay una, si no la real). */
    public static LocalDateTime ahora() {
        if (fija != null) {
            return fija;
        }
        return LocalDateTime.now();
    }

    /** Fecha de hoy segun el sistema. */
    public static LocalDate hoy() {
        return ahora().toLocalDate();
    }

    /** Deja una fecha y hora fija. */
    public static void fijar(LocalDateTime momento) {
        if (momento == null) {
            throw new IllegalArgumentException("La fecha y hora no puede ser nula");
        }
        fija = momento;
    }

    /** Vuelve a la hora del computador. */
    public static void usarHoraReal() {
        fija = null;
    }
}
