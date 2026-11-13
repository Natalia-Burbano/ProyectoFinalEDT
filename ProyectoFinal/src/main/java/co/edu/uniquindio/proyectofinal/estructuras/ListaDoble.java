package co.edu.uniquindio.proyectofinal.estructuras;

/**
 * Lista doblemente enlazada generica.
 * Cada nodo conoce al nodo de antes y al de despues, por eso se puede
 * recorrer en los dos sentidos.
 */
public class ListaDoble<T> {

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
}