package co.edu.uniquindio.proyectofinal.estructuras;

import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cola generica (el primero que entra es el primero que sale).
 * Se pone por el final y se saca por el frente.
 */
public class Cola<T> implements Iterable<T> {

    /**
     * Nodo de la cola. Guarda el valor y el nodo que va detras.
     */
    private static class Nodo<T> {
        private T valor;
        private Nodo<T> siguiente;

        public Nodo(T valor) {
            this.valor = valor;
            this.siguiente = null;
        }
    }

    private Nodo<T> frente;   // el primero, el que sale
    private Nodo<T> fin;      // el ultimo, donde se pone el nuevo
    private int tamanio;      // cantidad de elementos

    /**
     * Crea la cola vacia.
     */
    public Cola() {
        frente = null;
        fin = null;
        tamanio = 0;
    }

    /**
     * Pone un elemento al final de la cola.
     */
    public void encolar(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("No se puede encolar un valor nulo");
        }

        Nodo<T> nuevo = new Nodo<>(valor);

        if (estaVacia()) {
            frente = nuevo;
            fin = nuevo;
        } else {
            fin.siguiente = nuevo;   // se engancha detras del ultimo
            fin = nuevo;
        }
        tamanio++;
    }

    /**
     * Saca el primer elemento de la cola y lo devuelve.
     */
    public T desencolar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("No se puede desencolar: la cola esta vacia");
        }

        T valor = frente.valor;
        frente = frente.siguiente;   // el segundo pasa a ser el primero

        if (frente == null) {
            // si quedo vacia, el fin tambien queda en null
            fin = null;
        }
        tamanio--;
        return valor;
    }

    /**
     * Devuelve el primer elemento sin sacarlo.
     */
    public T frente() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola esta vacia");
        }
        return frente.valor;
    }

    /**
     * Devuelve cuantos elementos tiene la cola.
     */
    public int tamanio() {
        return tamanio;
    }

    /**
     * Dice si la cola no tiene elementos.
     */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /**
     * Recorre la cola del frente al final, sin sacar nada.
     */
    @Override
    public Iterator<T> iterator() {
        return new IteradorCola();
    }

    /**
     * Iterador que va del frente al final.
     */
    private class IteradorCola implements Iterator<T> {
        private Nodo<T> actual = frente;

        @Override
        public boolean hasNext() {
            return actual != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No hay mas elementos");
            }
            T valor = actual.valor;
            actual = actual.siguiente;
            return valor;
        }
    }
}