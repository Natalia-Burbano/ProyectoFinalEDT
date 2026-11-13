package co.edu.uniquindio.proyectofinal.estructuras;

import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import java.util.Iterator;

/**
 * Arbol binario de busqueda generico. Lo menor va a la izquierda y lo mayor a la derecha.
 * Todos los metodos importantes son recursivos: el metodo publico llama a uno privado
 * que recibe el nodo por donde va la recursion.
 */
public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {

    /** Nodo del arbol. Es protected para que ArbolReservas (otro paquete) lo pueda usar. */
    protected static class Nodo<E> {
        public E valor;
        public Nodo<E> izquierdo;
        public Nodo<E> derecho;

        public Nodo(E valor) {
            this.valor = valor;
        }
    }

    protected Nodo<T> raiz;
    protected int tamanio;

    /** Inserta un elemento. Devuelve false si ya existia (compareTo == 0). */
    public boolean insertar(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("No se puede insertar un dato nulo");
        }
        int antes = tamanio;
        raiz = insertar(raiz, valor);
        return tamanio > antes;
    }

    private Nodo<T> insertar(Nodo<T> nodo, T valor) {
        // CASO BASE: llegamos a un hueco, aqui va el nuevo nodo
        if (nodo == null) {
            tamanio++;
            return new Nodo<>(valor);
        }
        // CASO RECURSIVO: bajamos por la izquierda o por la derecha
        int c = valor.compareTo(nodo.valor);
        if (c < 0) {
            nodo.izquierdo = insertar(nodo.izquierdo, valor);
        } else if (c > 0) {
            nodo.derecho = insertar(nodo.derecho, valor);
        }
        // si c == 0 ya existe: no se hace nada
        return nodo;
    }

    /** Devuelve el elemento guardado que sea igual a la clave, o null si no esta. */
    public T buscar(T clave) {
        if (clave == null) {
            throw new IllegalArgumentException("No se puede buscar un dato nulo");
        }
        return buscar(raiz, clave);
    }

    private T buscar(Nodo<T> nodo, T clave) {
        // CASO BASE 1: no hay mas arbol, no esta
        if (nodo == null) {
            return null;
        }
        int c = clave.compareTo(nodo.valor);
        // CASO BASE 2: lo encontramos
        if (c == 0) {
            return nodo.valor;
        }
        // CASO RECURSIVO
        return c < 0 ? buscar(nodo.izquierdo, clave) : buscar(nodo.derecho, clave);
    }

    /** Dice si el elemento esta en el arbol. */
    public boolean contiene(T clave) {
        return buscar(clave) != null;
    }

    /** Elimina un elemento (hoja, un hijo o dos hijos). Devuelve false si no estaba. */
    public boolean eliminar(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("No se puede eliminar un dato nulo");
        }
        int antes = tamanio;
        raiz = eliminar(raiz, valor);
        return tamanio < antes;
    }

    private Nodo<T> eliminar(Nodo<T> nodo, T valor) {
        // CASO BASE: no esta
        if (nodo == null) {
            return null;
        }
        int c = valor.compareTo(nodo.valor);
        if (c < 0) {
            nodo.izquierdo = eliminar(nodo.izquierdo, valor);
        } else if (c > 0) {
            nodo.derecho = eliminar(nodo.derecho, valor);
        } else {
            // Encontrado. Caso 1 y 2: hoja o un solo hijo
            if (nodo.izquierdo == null) {
                tamanio--;
                return nodo.derecho;
            }
            if (nodo.derecho == null) {
                tamanio--;
                return nodo.izquierdo;
            }
            // Caso 3: dos hijos. Se reemplaza por el menor del subarbol derecho
            Nodo<T> sucesor = minimo(nodo.derecho);
            nodo.valor = sucesor.valor;
            nodo.derecho = eliminar(nodo.derecho, sucesor.valor); // aqui se descuenta el tamanio
        }
        return nodo;
    }

    /** Altura del arbol: vacio 0, una sola hoja 1. */
    public int altura() {
        return altura(raiz);
    }

    private int altura(Nodo<T> nodo) {
        if (nodo == null) { // CASO BASE
            return 0;
        }
        // CASO RECURSIVO
        return 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    /** El menor elemento. Arbol vacio: EstructuraVaciaException. */
    public T minimo() {
        if (raiz == null) {
            throw new EstructuraVaciaException("El arbol esta vacio");
        }
        return minimo(raiz).valor;
    }

    protected Nodo<T> minimo(Nodo<T> nodo) {
        if (nodo.izquierdo == null) { // CASO BASE
            return nodo;
        }
        return minimo(nodo.izquierdo); // CASO RECURSIVO
    }

    /** El mayor elemento. Arbol vacio: EstructuraVaciaException. */
    public T maximo() {
        if (raiz == null) {
            throw new EstructuraVaciaException("El arbol esta vacio");
        }
        return maximo(raiz).valor;
    }

    private Nodo<T> maximo(Nodo<T> nodo) {
        if (nodo.derecho == null) { // CASO BASE
            return nodo;
        }
        return maximo(nodo.derecho); // CASO RECURSIVO
    }

    /** Izquierda, raiz, derecha (sale ordenado). */
    public ListaDoble<T> inorden() {
        ListaDoble<T> resultado = new ListaDoble<>();
        inorden(raiz, resultado);
        return resultado;
    }

    private void inorden(Nodo<T> nodo, ListaDoble<T> resultado) {
        if (nodo == null) { // CASO BASE
            return;
        }
        inorden(nodo.izquierdo, resultado);
        resultado.agregarAlFinal(nodo.valor);
        inorden(nodo.derecho, resultado);
    }

    /** Raiz, izquierda, derecha. */
    public ListaDoble<T> preorden() {
        ListaDoble<T> resultado = new ListaDoble<>();
        preorden(raiz, resultado);
        return resultado;
    }

    private void preorden(Nodo<T> nodo, ListaDoble<T> resultado) {
        if (nodo == null) { // CASO BASE
            return;
        }
        resultado.agregarAlFinal(nodo.valor);
        preorden(nodo.izquierdo, resultado);
        preorden(nodo.derecho, resultado);
    }

    /** Izquierda, derecha, raiz. */
    public ListaDoble<T> postorden() {
        ListaDoble<T> resultado = new ListaDoble<>();
        postorden(raiz, resultado);
        return resultado;
    }

    private void postorden(Nodo<T> nodo, ListaDoble<T> resultado) {
        if (nodo == null) { // CASO BASE
            return;
        }
        postorden(nodo.izquierdo, resultado);
        postorden(nodo.derecho, resultado);
        resultado.agregarAlFinal(nodo.valor);
    }

    /** Nivel por nivel, de arriba hacia abajo (usa la Cola). */
    public ListaDoble<T> recorridoPorNiveles() {
        ListaDoble<T> resultado = new ListaDoble<>();
        if (raiz == null) {
            return resultado;
        }
        Cola<Nodo<T>> pendientes = new Cola<>();
        pendientes.encolar(raiz);
        while (!pendientes.estaVacia()) {
            Nodo<T> actual = pendientes.desencolar();
            resultado.agregarAlFinal(actual.valor);
            if (actual.izquierdo != null) {
                pendientes.encolar(actual.izquierdo);
            }
            if (actual.derecho != null) {
                pendientes.encolar(actual.derecho);
            }
        }
        return resultado;
    }

    /** Cantidad de elementos. */
    public int tamanio() {
        return tamanio;
    }

    /** Dice si no tiene elementos. */
    public boolean estaVacio() {
        return tamanio == 0;
    }

    /** Iterador en inorden: los elementos salen ordenados. */
    @Override
    public Iterator<T> iterator() {
        return inorden().iterator();
    }

    @Override
    public String toString() {
        return inorden().toString();
    }
}
