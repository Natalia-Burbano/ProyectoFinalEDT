package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;

import java.time.LocalDate;

/**
 * Guarda los periodos de cada temporada y dice a que temporada pertenece una fecha.
 * Una fecha que no cae en ningun periodo es temporada BAJA.
 */
public class CalendarioTemporadas {

    private final ListaDoble<PeriodoTemporada> periodos;

    /**
     * Crea el calendario sin periodos (todo seria temporada BAJA).
     */
    public CalendarioTemporadas() {
        periodos = new ListaDoble<>();
    }

    /**
     * Agrega un periodo. Si se cruza con otro que ya estaba, da FechasInvalidasException.
     */
    public PeriodoTemporada definirPeriodo(Temporada temporada, LocalDate desde, LocalDate hasta) {
        PeriodoTemporada nuevo = new PeriodoTemporada(temporada, desde, hasta);

        for (PeriodoTemporada p : periodos) {
            if (p.seCruzaCon(nuevo)) {
                throw new FechasInvalidasException("El periodo " + nuevo + " se cruza con " + p);
            }
        }

        periodos.agregarAlFinal(nuevo);
        return nuevo;
    }

    /**
     * Devuelve la temporada de una fecha: la del periodo que la contiene, o BAJA si no cae en ninguno.
     */
    public Temporada temporadaDe(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }

        for (PeriodoTemporada p : periodos) {
            if (p.contiene(fecha)) {
                return p.getTemporada();
            }
        }
        return Temporada.BAJA;
    }

    /**
     * Quita un periodo. Devuelve true si estaba.
     */
    public boolean quitarPeriodo(PeriodoTemporada periodo) {
        return periodos.eliminar(periodo);
    }

    /**
     * Devuelve una copia de los periodos, para mostrarlos sin que se pueda cambiar el calendario desde afuera.
     */
    public ListaDoble<PeriodoTemporada> periodos() {
        return periodos.copia();
    }
}