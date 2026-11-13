package co.edu.uniquindio.proyectofinal.modelo;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/** Persona que se aloja en una reserva. Los menores de 6 anios ocupan cupo pero no pagan. */
public class Ocupante {

    /** Edad desde la que se paga. */
    public static final int EDAD_MINIMA_PAGO = 6;

    private final String nombre;
    private final LocalDate fechaNacimiento;

    public Ocupante(String nombre, LocalDate fechaNacimiento) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del ocupante no puede estar vacio");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento del ocupante es obligatoria");
        }
        this.nombre = nombre.trim();
        this.fechaNacimiento = fechaNacimiento;
    }

    /** Anios cumplidos en la fecha dada. */
    public int edadEn(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    /** Dice si esa noche ya tiene 6 anios o mas (y por eso paga). */
    public boolean pagaEn(LocalDate fecha) {
        return edadEn(fecha) >= EDAD_MINIMA_PAGO;
    }

    public String getNombre() { return nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ocupante otro)) {
            return false;
        }
        return nombre.equals(otro.nombre) && fechaNacimiento.equals(otro.fechaNacimiento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, fechaNacimiento);
    }

    @Override
    public String toString() {
        return nombre + " (" + fechaNacimiento + ")";
    }
}
