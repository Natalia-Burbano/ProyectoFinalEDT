package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Una estancia: fecha de entrada y fecha de salida. La noche de salida no se cobra ni se ocupa
 * (del 10 al 12 de marzo son dos noches). Se puede recorrer con for-each noche por noche.
 */
public class Estancia implements Iterable<LocalDate> {

    private final LocalDate entrada;
    private final LocalDate salida;

    public Estancia(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son obligatorias");
        }
        if (!salida.isAfter(entrada)) {
            throw new FechasInvalidasException("La salida (" + salida + ") debe ser despues de la entrada (" + entrada + ")");
        }
        this.entrada = entrada;
        this.salida = salida;
    }

    /** Cantidad de noches de la estancia. */
    public int noches() {
        return (int) ChronoUnit.DAYS.between(entrada, salida);
    }

    /** Dice si esa noche hace parte de la estancia (la noche de salida no cuenta). */
    public boolean contiene(LocalDate noche) {
        return !noche.isBefore(entrada) && noche.isBefore(salida);
    }

    /** Dos estancias se cruzan si comparten al menos una noche. */
    public boolean seCruzaCon(Estancia otra) {
        return entrada.isBefore(otra.salida) && otra.entrada.isBefore(salida);
    }

    public LocalDate getEntrada() { return entrada; }
    public LocalDate getSalida() { return salida; }

    /** Da cada noche desde la entrada hasta la noche antes de la salida. */
    @Override
    public Iterator<LocalDate> iterator() {
        return new Iterator<LocalDate>() {
            private LocalDate siguiente = entrada;

            @Override
            public boolean hasNext() {
                return siguiente.isBefore(salida);
            }

            @Override
            public LocalDate next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No quedan mas noches");
                }
                LocalDate actual = siguiente;
                siguiente = siguiente.plusDays(1);
                return actual;
            }
        };
    }

    @Override
    public String toString() {
        return entrada + " -> " + salida + " (" + noches() + " noches)";
    }
}
