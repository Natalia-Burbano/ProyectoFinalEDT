package co.edu.uniquindio.proyectofinal.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Coleccion sin elementos repetidos (se compara con equals). */
public class Conjunto<T> implements Iterable<T> {

    private static class Nodo<T> {
        T valor;
        Nodo<T> siguiente;

        Nodo(T valor) {
            this.valor = valor;
        }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamanio;

    /** Agrega el elemento. Devuelve false si ya estaba. null da error. */
    public boolean agregar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("El elemento no puede ser nulo");
        }
        if (contiene(elemento)) {
            return false;
        }
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            cola.siguiente = nuevo;
        }
        cola = nuevo;
        tamanio++;
        return true;
    }

    /** Quita el elemento. Devuelve false si no estaba. */
    public boolean eliminar(T elemento) {
        Nodo<T> anterior = null;
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.valor.equals(elemento)) {
                if (anterior == null) {
                    cabeza = actual.siguiente;      // era la cabeza
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                if (actual == cola) {
                    cola = anterior;                // era la cola
                }
                tamanio--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    /** Dice si el elemento esta en el conjunto. */
    public boolean contiene(T elemento) {
        if (elemento == null) {
            return false;
        }
        Nodo<T> actual = cabeza;
        while (actual != null) {
            if (actual.valor.equals(elemento)) {
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacio() {
        return tamanio == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException("No hay mas elementos");
                }
                T valor = actual.valor;
                actual = actual.siguiente;
                return valor;
            }
        };
    }

    /** Formato: {A, B, C} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        Nodo<T> actual = cabeza;
        while (actual != null) {
            sb.append(actual.valor);
            if (actual.siguiente != null) {
                sb.append(", ");
            }
            actual = actual.siguiente;
        }
        return sb.append("}").toString();
    }
}