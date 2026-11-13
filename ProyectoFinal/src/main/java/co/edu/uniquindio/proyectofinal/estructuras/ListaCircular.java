package co.edu.uniquindio.proyectofinal.estructuras;

import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Lista circular con un elemento "actual" (sirve para rotar turnos). */
public class ListaCircular<T> implements Iterable<T> {

    private static class Nodo<T> {
        T valor;
        Nodo<T> siguiente;

        Nodo(T valor) {
            this.valor = valor;
        }
    }

    private Nodo<T> cabeza;
    private Nodo<T> ultimo;
    private Nodo<T> actual;
    private int tamanio;

    /** Agrega al final de la vuelta (antes del primero). */
    public void agregar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("El elemento no puede ser nulo");
        }
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
            ultimo = nuevo;
            actual = nuevo;
            nuevo.siguiente = nuevo;        // se apunta a si mismo
        } else {
            ultimo.siguiente = nuevo;
            nuevo.siguiente = cabeza;       // cierra el circulo
            ultimo = nuevo;
        }
        tamanio++;
    }

    /** El elemento donde va el turno. Vacia: EstructuraVaciaException. */
    public T actual() {
        if (actual == null) {
            throw new EstructuraVaciaException("La lista circular esta vacia");
        }
        return actual.valor;
    }

    /** Pasa al siguiente y lo devuelve. Despues del ultimo vuelve al primero. */
    public T avanzar() {
        if (actual == null) {
            throw new EstructuraVaciaException("La lista circular esta vacia");
        }
        actual = actual.siguiente;
        return actual.valor;
    }

    /** Quita el elemento. Si era el actual, el actual pasa al siguiente. */
    public boolean eliminar(T elemento) {
        if (tamanio == 0) {
            return false;
        }
        Nodo<T> anterior = ultimo;
        Nodo<T> nodo = cabeza;
        for (int i = 0; i < tamanio; i++) {
            if (nodo.valor.equals(elemento)) {
                if (tamanio == 1) {
                    cabeza = null;
                    ultimo = null;
                    actual = null;
                } else {
                    anterior.siguiente = nodo.siguiente;
                    if (nodo == cabeza) {
                        cabeza = nodo.siguiente;
                    }
                    if (nodo == ultimo) {
                        ultimo = anterior;
                    }
                    if (nodo == actual) {
                        actual = nodo.siguiente;
                    }
                }
                tamanio--;
                return true;
            }
            anterior = nodo;
            nodo = nodo.siguiente;
        }
        return false;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    /** Da una sola vuelta, empezando en el actual (no gira para siempre). */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> nodo = actual;
            private int vistos = 0;

            @Override
            public boolean hasNext() {
                return vistos < tamanio;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No hay mas elementos");
                }
                T valor = nodo.valor;
                nodo = nodo.siguiente;
                vistos++;
                return valor;
            }
        };
    }
}