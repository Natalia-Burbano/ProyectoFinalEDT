package co.edu.uniquindio.proyectofinal.modelo;

import java.time.LocalDate;

/**
 * Una fila del desglose de la cotizacion: lo que se cobra por una noche.
 * El valor es la tarifa por persona por la cantidad de personas que pagan esa noche.
 */
public class NocheLiquidada {

    private final LocalDate fecha;
    private final Temporada temporada;
    private final long tarifaPorPersona;
    private final int personasQuePagan;
    private final long valor;

    /**
     * Crea la noche y calcula su valor.
     */
    public NocheLiquidada(LocalDate fecha, Temporada temporada, long tarifaPorPersona, int personasQuePagan) {
        if (fecha == null || temporada == null) {
            throw new IllegalArgumentException("La fecha y la temporada son obligatorias");
        }
        if (tarifaPorPersona < 0 || personasQuePagan < 0) {
            throw new IllegalArgumentException("La tarifa y las personas no pueden ser negativas");
        }
        this.fecha = fecha;
        this.temporada = temporada;
        this.tarifaPorPersona = tarifaPorPersona;
        this.personasQuePagan = personasQuePagan;
        this.valor = tarifaPorPersona * personasQuePagan;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public long getTarifaPorPersona() {
        return tarifaPorPersona;
    }

    public int getPersonasQuePagan() {
        return personasQuePagan;
    }

    public long getValor() {
        return valor;
    }

    /**
     * Muestra la noche, por ejemplo: 2026-03-10 BAJA 80000 x 2 = 160000
     */
    @Override
    public String toString() {
        return fecha + " " + temporada + " " + tarifaPorPersona + " x " + personasQuePagan + " = " + valor;
    }
}