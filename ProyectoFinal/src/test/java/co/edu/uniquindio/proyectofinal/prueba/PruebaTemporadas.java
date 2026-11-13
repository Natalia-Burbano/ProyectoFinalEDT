package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;
import co.edu.uniquindio.proyectofinal.modelo.CalendarioTemporadas;
import co.edu.uniquindio.proyectofinal.modelo.NocheLiquidada;
import co.edu.uniquindio.proyectofinal.modelo.PeriodoTemporada;
import co.edu.uniquindio.proyectofinal.modelo.Temporada;
import co.edu.uniquindio.proyectofinal.util.Prueba;

import java.time.LocalDate;

/**
 * Pruebas de las temporadas y de la NocheLiquidada.
 */
public class PruebaTemporadas {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /**
     * Casos de prueba de las temporadas.
     */
    public static void ejecutar() {
        Prueba.titulo("Temporadas");

        CalendarioTemporadas calendario = new CalendarioTemporadas();
        Prueba.verificar(calendario.temporadaDe(LocalDate.of(2026, 12, 31)) == Temporada.BAJA,
                "sin periodos, cualquier fecha es BAJA");

        calendario.definirPeriodo(Temporada.ALTA, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15));
        PeriodoTemporada media = calendario.definirPeriodo(Temporada.MEDIA,
                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 7, 15));

        Prueba.verificar(calendario.temporadaDe(LocalDate.of(2026, 12, 31)) == Temporada.ALTA, "el 2026-12-31 es ALTA");
        Prueba.verificar(calendario.temporadaDe(LocalDate.of(2026, 11, 4)) == Temporada.BAJA, "el 2026-11-04 es BAJA");
        Prueba.verificar(calendario.temporadaDe(LocalDate.of(2026, 12, 15)) == Temporada.ALTA, "el primer dia del periodo cuenta (15 dic ALTA)");
        Prueba.verificar(calendario.temporadaDe(LocalDate.of(2027, 1, 15)) == Temporada.ALTA, "el ultimo dia del periodo cuenta (15 ene ALTA)");
        Prueba.verificar(calendario.temporadaDe(LocalDate.of(2027, 1, 16)) == Temporada.BAJA, "el dia siguiente ya es BAJA");
        Prueba.verificar(calendario.temporadaDe(LocalDate.of(2026, 7, 1)) == Temporada.MEDIA, "el 2026-07-01 es MEDIA");

        Prueba.esperarExcepcion(FechasInvalidasException.class,
                () -> calendario.definirPeriodo(Temporada.MEDIA, LocalDate.of(2027, 1, 10), LocalDate.of(2027, 2, 1)),
                "un periodo que se cruza con otro da error");
        Prueba.esperarExcepcion(FechasInvalidasException.class,
                () -> new PeriodoTemporada(Temporada.ALTA, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 1)),
                "un periodo con desde despues de hasta da error");
        Prueba.verificar(calendario.periodos().tamanio() == 2, "el calendario tiene 2 periodos");

        Prueba.verificar(calendario.quitarPeriodo(media), "quitar el periodo MEDIA devuelve true");
        Prueba.verificar(calendario.temporadaDe(LocalDate.of(2026, 7, 1)) == Temporada.BAJA,
                "despues de quitarlo, el 2026-07-01 vuelve a ser BAJA");

        calendario.periodos().agregarAlFinal(media);
        Prueba.verificar(calendario.periodos().tamanio() == 1, "cambiar la lista que devuelve periodos() no cambia el calendario");

        Prueba.titulo("NocheLiquidada");
        NocheLiquidada noche = new NocheLiquidada(LocalDate.of(2026, 3, 10), Temporada.BAJA, 80000, 2);
        Prueba.verificar(noche.getValor() == 160000, "80.000 por persona con 2 que pagan da 160.000");
        Prueba.verificar(new NocheLiquidada(LocalDate.of(2026, 3, 10), Temporada.BAJA, 80000, 0).getValor() == 0,
                "si nadie paga esa noche el valor es 0");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new NocheLiquidada(null, Temporada.BAJA, 80000, 2), "una noche sin fecha da error");
    }
}