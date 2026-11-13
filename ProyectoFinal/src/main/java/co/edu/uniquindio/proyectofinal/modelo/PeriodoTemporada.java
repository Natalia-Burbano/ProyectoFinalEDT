package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;

import java.time.LocalDate;

/**
 * Un rango de fechas que pertenece a una temporada, por ejemplo ALTA del 15 de diciembre al 15 de enero.
 * Las dos fechas estan incluidas.
 */
public class PeriodoTemporada {

    private final Temporada temporada;
    private final LocalDate desde;
    private final LocalDate hasta;

    /**
     * Crea el periodo. La fecha "desde" no puede ser despues de "hasta".
     */
    public PeriodoTemporada(Temporada temporada, LocalDate desde, LocalDate hasta) {
        if (temporada == null || desde == null || hasta == null) {
            throw new IllegalArgumentException("La temporada y las fechas son obligatorias");
        }
        if (desde.isAfter(hasta)) {
            throw new FechasInvalidasException("La fecha de inicio (" + desde
                    + ") no puede ser despues de la fecha final (" + hasta + ")");
        }
        this.temporada = temporada;
        this.desde = desde;
        this.hasta = hasta;
    }

    /**
     * Dice si la fecha cae dentro del periodo (las dos puntas cuentan).
     */
    public boolean contiene(LocalDate fecha) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    /**
     * Dice si este periodo comparte al menos un dia con otro.
     */
    public boolean seCruzaCon(PeriodoTemporada otro) {
        return !this.desde.isAfter(otro.hasta) && !otro.desde.isAfter(this.hasta);
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    /**
     * Dos periodos son iguales si tienen la misma temporada y las mismas fechas.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PeriodoTemporada)) {
            return false;
        }
        PeriodoTemporada otro = (PeriodoTemporada) o;
        return temporada == otro.temporada && desde.equals(otro.desde) && hasta.equals(otro.hasta);
    }

    @Override
    public int hashCode() {
        return desde.hashCode() + hasta.hashCode() + temporada.hashCode();
    }

    @Override
    public String toString() {
        return temporada + " del " + desde + " al " + hasta;
    }
}