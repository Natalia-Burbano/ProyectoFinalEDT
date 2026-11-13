package co.edu.uniquindio.proyectofinal.comparadores;

import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import java.util.Comparator;

/** Ordenes alternativos de apartamentos. Todos terminan con thenComparing por codigo. */
public final class ComparadoresApartamento {

    private ComparadoresApartamento() {
    }

    /** Tarifa de temporada baja, el mas barato primero. */
    public static final Comparator<Apartamento> POR_PRECIO =
            Comparator.comparingLong(Apartamento::getTarifaBaja)
                    .thenComparing(Apartamento::getCodigo);

    /** Mayor capacidad primero. */
    public static final Comparator<Apartamento> POR_CAPACIDAD =
            Comparator.comparingInt(Apartamento::getCapacidad).reversed()
                    .thenComparing(Apartamento::getCodigo);

    /** Mas dormitorios primero. */
    public static final Comparator<Apartamento> POR_DORMITORIOS =
            Comparator.comparingInt(Apartamento::getDormitorios).reversed()
                    .thenComparing(Apartamento::getCodigo);

    /** Mas reservas primero. */
    public static final Comparator<Apartamento> POR_NUMERO_RESERVAS =
            Comparator.comparingInt(Apartamento::getNumeroReservas).reversed()
                    .thenComparing(Apartamento::getCodigo);
}
