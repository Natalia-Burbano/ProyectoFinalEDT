package co.edu.uniquindio.proyectofinal.estructuras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Grafo no dirigido con peso. Las aristas se pueden cerrar sin borrarlas. */
public class Grafo<T> implements Iterable<T> {

    private static class Arista<T> {
        NodoVertice<T> destino;
        double peso;
        boolean abierta = true;
        Arista<T> siguiente;

        Arista(NodoVertice<T> destino, double peso) {
            this.destino = destino;
            this.peso = peso;
        }
    }

    private static class NodoVertice<T> {
        T valor;
        Arista<T> aristas;          // primera arista de este vertice
        NodoVertice<T> siguiente;   // siguiente vertice del grafo

        NodoVertice(T valor) {
            this.valor = valor;
        }
    }

    private NodoVertice<T> primero;
    private NodoVertice<T> ultimoVertice;
    private int cantidad;

    // ---------- vertices ----------

    /** Agrega un vertice. Devuelve false si ya existia. */
    public boolean agregarVertice(T valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El vertice no puede ser nulo");
        }
        if (existeVertice(valor)) {
            return false;
        }
        NodoVertice<T> nuevo = new NodoVertice<>(valor);
        if (primero == null) {
            primero = nuevo;
        } else {
            ultimoVertice.siguiente = nuevo;
        }
        ultimoVertice = nuevo;
        cantidad++;
        return true;
    }

    public boolean existeVertice(T valor) {
        return buscar(valor) != null;
    }

    public int cantidadVertices() {
        return cantidad;
    }

    /** Posicion del vertice (0 a n-1) o -1 si no existe. Sirve para Dijkstra con arreglos. */
    public int indiceDe(T valor) {
        int i = 0;
        NodoVertice<T> actual = primero;
        while (actual != null) {
            if (actual.valor.equals(valor)) {
                return i;
            }
            actual = actual.siguiente;
            i++;
        }
        return -1;
    }

    /** El vertice que esta en esa posicion. */
    public T verticeEn(int indice) {
        if (indice < 0 || indice >= cantidad) {
            throw new IndexOutOfBoundsException("Indice invalido: " + indice);
        }
        NodoVertice<T> actual = primero;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.valor;
    }

    // ---------- aristas ----------

    /** Conecta a y b en los dos sentidos. Los dos deben existir y el peso ser mayor que 0. */
    public void agregarArista(T a, T b, double peso) {
        if (peso <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que 0");
        }
        NodoVertice<T> na = buscar(a);
        NodoVertice<T> nb = buscar(b);
        if (na == null || nb == null) {
            throw new IllegalArgumentException("Los dos vertices deben existir");
        }
        if (na == nb) {
            throw new IllegalArgumentException("No se puede conectar un vertice consigo mismo");
        }
        Arista<T> existente = buscarArista(na, nb);
        if (existente != null) {                 // ya estaban conectados: solo cambia el peso
            existente.peso = peso;
            buscarArista(nb, na).peso = peso;
            return;
        }
        agregarAlFinal(na, new Arista<>(nb, peso));
        agregarAlFinal(nb, new Arista<>(na, peso));
    }

    /** Marca el pasillo como cerrado (no lo borra). */
    public void cerrarArista(T a, T b) {
        cambiarEstado(a, b, false);
    }

    /** Marca el pasillo como abierto. */
    public void abrirArista(T a, T b) {
        cambiarEstado(a, b, true);
    }

    public boolean estaAbierta(T a, T b) {
        Arista<T> arista = buscarArista(buscar(a), buscar(b));
        return arista != null && arista.abierta;
    }

    /** Peso de la arista, o -1 si no estan conectados. Da el peso aunque este cerrada. */
    public double peso(T a, T b) {
        Arista<T> arista = buscarArista(buscar(a), buscar(b));
        if (arista == null) {
            return -1;
        }
        return arista.peso;
    }

    /** Solo los vecinos por aristas abiertas. */
    public Iterable<T> vecinos(T valor) {
        NodoVertice<T> nodo = buscar(valor);
        if (nodo == null) {
            throw new IllegalArgumentException("El vertice no existe");
        }
        return () -> new Iterator<T>() {
            private Arista<T> actual = saltarCerradas(nodo.aristas);

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException("No hay mas vecinos");
                }
                T vecino = actual.destino.valor;
                actual = saltarCerradas(actual.siguiente);
                return vecino;
            }
        };
    }

    /** Recorre los vertices en el orden en que se agregaron. */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private NodoVertice<T> actual = primero;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException("No hay mas vertices");
                }
                T valor = actual.valor;
                actual = actual.siguiente;
                return valor;
            }
        };
    }

    // ---------- metodos privados ----------

    private NodoVertice<T> buscar(T valor) {
        NodoVertice<T> actual = primero;
        while (actual != null) {
            if (actual.valor.equals(valor)) {
                return actual;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /** La arista que va de origen a destino, o null. */
    private Arista<T> buscarArista(NodoVertice<T> origen, NodoVertice<T> destino) {
        if (origen == null || destino == null) {
            return null;
        }
        Arista<T> actual = origen.aristas;
        while (actual != null) {
            if (actual.destino == destino) {
                return actual;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    private void agregarAlFinal(NodoVertice<T> origen, Arista<T> nueva) {
        if (origen.aristas == null) {
            origen.aristas = nueva;
            return;
        }
        Arista<T> actual = origen.aristas;
        while (actual.siguiente != null) {
            actual = actual.siguiente;
        }
        actual.siguiente = nueva;
    }

    private Arista<T> saltarCerradas(Arista<T> arista) {
        while (arista != null && !arista.abierta) {
            arista = arista.siguiente;
        }
        return arista;
    }

    private void cambiarEstado(T a, T b, boolean abierta) {
        NodoVertice<T> na = buscar(a);
        NodoVertice<T> nb = buscar(b);
        Arista<T> ida = buscarArista(na, nb);
        Arista<T> vuelta = buscarArista(nb, na);
        if (ida == null || vuelta == null) {
            throw new IllegalArgumentException("Esos vertices no estan conectados");
        }
        ida.abierta = abierta;
        vuelta.abierta = abierta;
    }
}
