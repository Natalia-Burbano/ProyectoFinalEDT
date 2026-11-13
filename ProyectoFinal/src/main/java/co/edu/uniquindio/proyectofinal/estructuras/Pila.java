package co.edu.uniquindio.proyectofinal.estructuras;

import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Pila generica (el ultimo que entra es el primero que sale).
 * Solo se puede poner y sacar por arriba, en la cima.
 */
public class Pila<T> implements Iterable<T> {

    /**
     * Nodo de la pila. Guarda el valor y el nodo que esta debajo.
     */
    private static class Nodo<T> {
        private T valor;
        private Nodo<T> siguiente;

        public Nodo(T valor) {
            this.valor = valor;
            this.siguiente = null;
        }
    }

    private Nodo<T> cima;     // el de arriba
    private int tamanio;      // cantidad de elementos

    /**
     * Crea la pila vacia.
     */
    public Pila() {
        cima = null;
        tamanio = 0;
    }

    /**
     * Pone un elemento encima de la pila.
     */
    public void apilar(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("No se puede apilar un valor nulo");
        }

        Nodo<T> nuevo = new Nodo<>(valor);
        nuevo.siguiente = cima;   // el nuevo queda encima del que era la cima
        cima = nuevo;
        tamanio++;
    }

    /**
     * Saca el elemento de arriba y lo devuelve.
     */
    public T desapilar() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("No se puede desapilar: la pila esta vacia");
        }

        T valor = cima.valor;
        cima = cima.siguiente;    // el de abajo pasa a ser la cima
        tamanio--;
        return valor;
    }

    /**
     * Devuelve el elemento de arriba sin sacarlo.
     */
    public T cima() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La pila esta vacia");
        }
        return cima.valor;
    }

    /**
     * Devuelve cuantos elementos tiene la pila.
     */
    public int tamanio() {
        return tamanio;
    }

    /**
     * Dice si la pila no tiene elementos.
     */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /**
     * Recorre la pila de la cima hacia abajo, sin sacar nada.
     */
    @Override
    public Iterator<T> iterator() {
        return new IteradorPila();
    }

    /**
     * Iterador que va de la cima hacia abajo.
     */
    private class IteradorPila implements Iterator<T> {
        private Nodo<T> actual = cima;

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