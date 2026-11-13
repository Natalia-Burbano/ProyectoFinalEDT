package co.edu.uniquindio.proyectofinal.gui;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Ayudas para mostrar datos en pantalla: dinero, fechas y horas.
 * La usan todas las pantallas para que todo se vea igual.
 */
public final class Formato {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private Formato() {
    }

    /**
     * Muestra un valor en pesos con puntos de miles, por ejemplo $ 60.000 o $ -90.000.
     */
    public static String pesos(long valor) {
        String signo = valor < 0 ? "-" : "";
        String cifras = String.valueOf(Math.abs(valor));
        String conPuntos = "";
        int contador = 0;
        // se recorre de derecha a izquierda poniendo un punto cada tres cifras
        for (int i = cifras.length() - 1; i >= 0; i--) {
            conPuntos = cifras.charAt(i) + conPuntos;
            contador++;
            if (contador % 3 == 0 && i > 0) {
                conPuntos = "." + conPuntos;
            }
        }
        return "$ " + signo + conPuntos;
    }

    /**
     * Fecha y hora, por ejemplo 20/11/2026 14:30.
     */
    public static String fechaHora(LocalDateTime momento) {
        return momento == null ? "-" : momento.format(FECHA_HORA);
    }

    /**
     * Solo la hora, por ejemplo 15:00. Si no hay hora devuelve un guion.
     */
    public static String hora(LocalTime hora) {
        return hora == null ? "-" : hora.format(HORA);
    }
}
