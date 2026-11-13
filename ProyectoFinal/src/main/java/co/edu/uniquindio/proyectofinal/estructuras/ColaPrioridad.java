package co.edu.uniquindio.proyectofinal.estructuras;

import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cola de prioridad generica.
 * Por dentro es una lista enlazada que siempre esta ordenada: cada elemento nuevo
 * entra en el puesto que le toca segun el comparador. Asi el primero de la lista
 * siempre es el que debe salir.
 */
public class ColaPrioridad<T> implements Iterable<T> {

    /**
     * Nodo de la cola. Guarda el valor y el nodo que va despues.
     */
    private static class Nodo<T> {
        private T valor;
        private Nodo<T> siguiente;

        public Nodo(T valor) {
            this.valor = valor;
            this.siguiente = null;
        }
    }

    private Nodo<T> primero;                 // el que sale primero
    private int tamanio;                     // cantidad de elementos
    private final Comparator<T> comparador;  // decide quien va antes

    /**
     * Crea la cola vacia. El comparador decide el orden: el "menor" sale primero.
     */
    public ColaPrioridad(Comparator<T> comparador) {
        if (comparador == null) {
            throw new IllegalArgumentException("El comparador no puede ser nulo");
        }
        this.comparador = comparador;
        this.primero = null;
        this.tamanio = 0;
    }

    /**
     * Mete el elemento en el puesto que le toca.
     * Queda antes del primer elemento que sea mayor que el. Si empata con otros,
     * queda despues de ellos (entre iguales sale primero el que llego primero).
     */
    public void insertar(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("No se puede insertar un valor nulo");
        }

        Nodo<T> nuevo = new Nodo<>(valor);

        // va de primero si la cola esta vacia o si es menor que el primero
        if (primero == null || comparador.compare(valor, primero.valor) < 0) {
            nuevo.siguiente = primero;
            primero = nuevo;
        } else {
            // buscar el ultimo nodo que no sea mayor que el nuevo
            Nodo<T> aux = primero;
            while (aux.siguiente != null && comparador.compare(valor, aux.siguiente.valor) >= 0) {
                aux = aux.siguiente;
            }
            // el nuevo queda entre aux y el que seguia
            nuevo.siguiente = aux.siguiente;
            aux.siguiente = nuevo;
        }
        tamanio++;
    }

    /**
     * Saca el primero (el de mayor prioridad) y lo devuelve.
     */
    public T extraer() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("No se puede extraer: la cola de prioridad esta vacia");
        }

        T valor = primero.valor;
        primero = primero.siguiente;
        tamanio--;
        return valor;
    }

    /**
     * Devuelve el primero sin sacarlo.
     */
    public T verPrimero() {
        if (estaVacia()) {
            throw new EstructuraVaciaException("La cola de prioridad esta vacia");
        }
        return primero.valor;
    }

    /**
     * Quita la primera vez que aparece el elemento (compara con equals).
     * Devuelve true si lo quito y false si no estaba. La cola sigue ordenada.
     */
    public boolean eliminar(T valor) {
        if (estaVacia()) {
            return false;
        }

        // si es el primero
        if (primero.valor.equals(valor)) {
            primero = primero.siguiente;
            tamanio--;
            return true;
        }

        // si esta mas adelante: se busca con el nodo de antes para poder saltarlo
        Nodo<T> anterior = primero;
        while (anterior.siguiente != null) {
            if (anterior.siguiente.valor.equals(valor)) {
                anterior.siguiente = anterior.siguiente.siguiente;
                tamanio--;
                return true;
            }
            anterior = anterior.siguiente;
        }
        return false;
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
     * Devuelve una ListaDoble con los elementos en el orden en que van a salir.
     * No saca nada de la cola.
     */
    public ListaDoble<T> enOrden() {
        ListaDoble<T> lista = new ListaDoble<>();
        Nodo<T> aux = primero;

        while (aux != null) {
            lista.agregarAlFinal(aux.valor);
            aux = aux.siguiente;
        }
        return lista;
    }

    /**
     * Recorre la cola del primero al ultimo. Como la lista esta ordenada,
     * el for-each da los elementos en el orden en que van a salir.
     */
    @Override
    public Iterator<T> iterator() {
        return new IteradorColaPrioridad();
    }

    /**
     * Muestra la cola en el orden de salida, por ejemplo [1, 2, 3].
     */
    @Override
    public String toString() {
        String texto = "[";
        Nodo<T> aux = primero;

        while (aux != null) {
            texto += aux.valor;
            if (aux.siguiente != null) {
                texto += ", ";
            }
            aux = aux.siguiente;
        }

        texto += "]";
        return texto;
    }

    /**
     * Iterador que va del primero al ultimo.
     */
    private class IteradorColaPrioridad implements Iterator<T> {
        private Nodo<T> actual = primero;

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