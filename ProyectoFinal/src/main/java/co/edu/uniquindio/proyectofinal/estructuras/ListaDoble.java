package co.edu.uniquindio.proyectofinal.estructuras;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Lista doblemente enlazada generica.
 * Cada nodo conoce al nodo de antes y al de despues, por eso se puede
 * recorrer en los dos sentidos.
 */
public class ListaDoble<T> implements Iterable<T> {

    /**
     * Nodo de la lista. Guarda el valor y las referencias al anterior y al siguiente.
     * Es privado para que nadie de afuera pueda tocar los enlaces.
     */
    private static class Nodo<T> {
        private T valor;
        private Nodo<T> anterior;
        private Nodo<T> siguiente;

        public Nodo(T valor) {
            this.valor = valor;
            this.anterior = null;
            this.siguiente = null;
        }
    }

    private Nodo<T> cabeza;   // primer nodo
    private Nodo<T> cola;     // ultimo nodo
    private int tamanio;      // cantidad de elementos

    /**
     * Crea la lista vacia.
     */
    public ListaDoble() {
        cabeza = null;
        cola = null;
        tamanio = 0;
    }

    /**
     * Agrega un elemento al final de la lista.
     */
    public void agregarAlFinal(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("No se puede agregar un valor nulo");
        }

        Nodo<T> nuevo = new Nodo<>(valor);

        if (estaVacia()) {
            // si no hay nada, el nuevo es el primero y el ultimo
            cabeza = nuevo;
            cola = nuevo;
        } else {
            // se engancha despues de la cola y queda como la nueva cola
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
            cola = nuevo;
        }
        tamanio++;
    }

    /**
     * Agrega un elemento al inicio de la lista.
     */
    public void agregarAlInicio(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("No se puede agregar un valor nulo");
        }

        Nodo<T> nuevo = new Nodo<>(valor);

        if (estaVacia()) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            // se engancha antes de la cabeza y queda como la nueva cabeza
            nuevo.siguiente = cabeza;
            cabeza.anterior = nuevo;
            cabeza = nuevo;
        }
        tamanio++;
    }

    /**
     * Devuelve el elemento que esta en la posicion dada (la primera es 0).
     */
    public T obtener(int posicion) {
        return nodoEn(posicion).valor;
    }

    /**
     * Quita la primera vez que aparece el elemento (compara con equals).
     * Devuelve true si lo quito y false si no estaba.
     */
    public boolean eliminar(T valor) {
        Nodo<T> aux = cabeza;

        while (aux != null) {
            if (aux.valor.equals(valor)) {
                quitarNodo(aux);
                return true;
            }
            aux = aux.siguiente;
        }
        return false;
    }

    /**
     * Quita el elemento que esta en la posicion dada y lo devuelve.
     */
    public T eliminarEn(int posicion) {
        Nodo<T> nodo = nodoEn(posicion);
        quitarNodo(nodo);
        return nodo.valor;
    }

    /**
     * Dice si el elemento esta en la lista (compara con equals).
     */
    public boolean contiene(T valor) {
        Nodo<T> aux = cabeza;

        while (aux != null) {
            if (aux.valor.equals(valor)) {
                return true;
            }
            aux = aux.siguiente;
        }
        return false;
    }

    /**
     * Devuelve cuantos elementos tiene la lista.
     */
    public int tamanio() {
        return tamanio;
    }

    /**
     * Dice si la lista no tiene elementos.
     */
    public boolean estaVacia() {
        return tamanio == 0;
    }

    /**
     * Ordena la misma lista segun el comparador (por insercion).
     * Se arma una lista nueva metiendo cada elemento en el puesto que le toca,
     * y despues esta lista se queda con los nodos de la nueva.
     */
    public void ordenar(Comparator<T> comparador) {
        if (comparador == null) {
            throw new IllegalArgumentException("El comparador no puede ser nulo");
        }

        ListaDoble<T> ordenada = new ListaDoble<>();
        Nodo<T> aux = cabeza;

        while (aux != null) {
            ordenada.insertarEnOrden(aux.valor, comparador);
            aux = aux.siguiente;
        }

        cabeza = ordenada.cabeza;
        cola = ordenada.cola;
    }

    /**
     * Devuelve una lista nueva con los mismos elementos y en el mismo orden.
     * Si se cambia la copia, esta lista no cambia.
     */
    public ListaDoble<T> copia() {
        ListaDoble<T> nueva = new ListaDoble<>();
        Nodo<T> aux = cabeza;

        while (aux != null) {
            nueva.agregarAlFinal(aux.valor);
            aux = aux.siguiente;
        }
        return nueva;
    }

    /**
     * Recorre la lista de la cabeza a la cola. Es lo que usa el for-each.
     */
    @Override
    public Iterator<T> iterator() {
        return new IteradorLista();
    }

    /**
     * Recorre la lista al reves, de la cola a la cabeza.
     */
    public Iterator<T> iteradorInverso() {
        return new IteradorInverso();
    }

    /**
     * Muestra la lista asi: [A <-> B <-> C]. Si esta vacia muestra [].
     */
    @Override
    public String toString() {
        String texto = "[";
        Nodo<T> aux = cabeza;

        while (aux != null) {
            texto += aux.valor;
            if (aux.siguiente != null) {
                texto += " <-> ";
            }
            aux = aux.siguiente;
        }

        texto += "]";
        return texto;
    }

    // ------------------ metodos privados de ayuda ------------------

    /**
     * Busca el nodo de una posicion. Si la posicion esta en la primera mitad
     * avanza desde la cabeza, si no, retrocede desde la cola (para eso sirve que sea doble).
     */
    private Nodo<T> nodoEn(int posicion) {
        if (posicion < 0 || posicion >= tamanio) {
            throw new IndexOutOfBoundsException("Posicion invalida: " + posicion);
        }

        Nodo<T> aux;
        if (posicion < tamanio / 2) {
            aux = cabeza;
            for (int i = 0; i < posicion; i++) {
                aux = aux.siguiente;
            }
        } else {
            aux = cola;
            for (int i = tamanio - 1; i > posicion; i--) {
                aux = aux.anterior;
            }
        }
        return aux;
    }

    /**
     * Saca un nodo de la lista arreglando los enlaces.
     * Casos: es el unico, es la cabeza, es la cola o esta en el medio.
     */
    private void quitarNodo(Nodo<T> nodo) {
        if (nodo == cabeza && nodo == cola) {
            // era el unico: la lista queda vacia
            cabeza = null;
            cola = null;
        } else if (nodo == cabeza) {
            // era el primero: el segundo pasa a ser la cabeza
            cabeza = cabeza.siguiente;
            cabeza.anterior = null;
        } else if (nodo == cola) {
            // era el ultimo: el penultimo pasa a ser la cola
            cola = cola.anterior;
            cola.siguiente = null;
        } else {
            // estaba en el medio: el de antes y el de despues se enlazan entre ellos
            nodo.anterior.siguiente = nodo.siguiente;
            nodo.siguiente.anterior = nodo.anterior;
        }
        tamanio--;
    }

    /**
     * Mete el valor antes del primer elemento que sea mayor que el.
     * Si empata con otros, queda despues de ellos. La usa ordenar().
     */
    private void insertarEnOrden(T valor, Comparator<T> comparador) {
        Nodo<T> aux = cabeza;

        // avanzar mientras el valor no sea menor que el del nodo
        while (aux != null && comparador.compare(valor, aux.valor) >= 0) {
            aux = aux.siguiente;
        }

        if (aux == null) {
            // ninguno es mayor: va al final
            agregarAlFinal(valor);
        } else if (aux == cabeza) {
            // es menor que todos: va al inicio
            agregarAlInicio(valor);
        } else {
            // va entre aux.anterior y aux
            Nodo<T> nuevo = new Nodo<>(valor);
            nuevo.anterior = aux.anterior;
            nuevo.siguiente = aux;
            aux.anterior.siguiente = nuevo;
            aux.anterior = nuevo;
            tamanio++;
        }
    }

    // ------------------ iteradores ------------------

    /**
     * Iterador que va de la cabeza a la cola.
     */
    private class IteradorLista implements Iterator<T> {
        private Nodo<T> actual = cabeza;

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

    /**
     * Iterador que va de la cola a la cabeza.
     */
    private class IteradorInverso implements Iterator<T> {
        private Nodo<T> actual = cola;

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
            actual = actual.anterior;
            return valor;
        }
    }
}