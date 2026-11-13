package co.edu.uniquindio.proyectofinal.comparadores;

import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.modelo.TareaPreparacion;

import java.util.Comparator;

/**
 * Ordenes para las colas de prioridad. El que queda "menor" es el que sale primero.
 */
public final class ComparadoresPreparacion {

    private ComparadoresPreparacion() {
    }

    /**
     * Cola de preparacion:
     * 1. primero los que tienen entrada hoy (prioridad 1 antes que prioridad 2),
     * 2. entre ellos, la hora de llegada mas temprana,
     * 3. luego el que lleva mas tiempo pendiente,
     * 4. y al final el codigo del apartamento.
     */
    public static final Comparator<TareaPreparacion> PREPARACION =
            Comparator.comparingInt(TareaPreparacion::prioridad)
                    .thenComparing(ComparadoresPreparacion::compararHoraLlegada)
                    .thenComparing(TareaPreparacion::getPendienteDesde)
                    .thenComparing(TareaPreparacion::getCodigoApartamento);

    /**
     * Reservas pendientes: la que vence primero y, si empatan, por codigo.
     */
    public static final Comparator<Reserva> VENCIMIENTO =
            Comparator.comparing(Reserva::getVenceEn)
                    .thenComparing(Reserva::getCodigo);

    /**
     * Compara la hora de llegada de dos tareas. Las de prioridad 2 no tienen hora,
     * asi que entre ellas da empate y decide el siguiente criterio.
     */
    private static int compararHoraLlegada(TareaPreparacion a, TareaPreparacion b) {
        if (a.getHoraLlegada() == null || b.getHoraLlegada() == null) {
            return 0;
        }
        return a.getHoraLlegada().compareTo(b.getHoraLlegada());
    }
}
