package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import java.util.Comparator;

/** Catalogo de apartamentos guardado en un arbol binario de busqueda (por codigo). */
public class CatalogoApartamentos {

    private final ArbolBinarioBusqueda<Apartamento> arbol = new ArbolBinarioBusqueda<>();

    /** Registra un apartamento. Codigo repetido: IllegalStateException. */
    public void agregar(Apartamento apartamento) {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento no puede ser nulo");
        }
        if (!arbol.insertar(apartamento)) {
            throw new IllegalStateException("Ya existe un apartamento con el codigo " + apartamento.getCodigo());
        }
    }

    /** Busca por codigo en el arbol. Devuelve null si no existe. */
    public Apartamento buscar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return arbol.buscar(Apartamento.soloCodigo(codigo));
    }

    /**
     * Elimina un apartamento. Devuelve false si no existe.
     * Solo se elimina si no tiene reservas activas (si las tiene: IllegalStateException).
     */
    public boolean eliminar(String codigo) {
        Apartamento apartamento = buscar(codigo);
        if (apartamento == null) {
            return false;
        }
        if (!apartamento.getReservas().activas().estaVacia()) {
            throw new IllegalStateException("El apartamento " + codigo + " tiene reservas activas y no se puede eliminar");
        }
        return arbol.eliminar(apartamento);
    }

    /** Todos los apartamentos ordenados por codigo (recorrido inorden). */
    public ListaDoble<Apartamento> todos() {
        return arbol.inorden();
    }

    /**
     * Los apartamentos que sirven para la estancia: no estan fuera de servicio, tienen capacidad
     * suficiente y no tienen solapamiento (se revisa en el arbol de reservas de cada uno).
     */
    public ListaDoble<Apartamento> disponibles(Estancia estancia, int personas) {
        if (estancia == null) {
            throw new IllegalArgumentException("La estancia no puede ser nula");
        }
        if (personas <= 0) {
            throw new IllegalArgumentException("Las personas deben ser mayor a 0");
        }
        ListaDoble<Apartamento> libres = new ListaDoble<>();
        for (Apartamento a : arbol) {
            if (a.getEstado() == EstadoApartamento.FUERA_DE_SERVICIO) {
                continue;
            }
            if (a.getCapacidad() < personas) {
                continue;
            }
            if (a.getReservas().haySolapamiento(estancia)) {
                continue;
            }
            libres.agregarAlFinal(a);
        }
        return libres;
    }

    /** Igual que disponibles, pero ordenados con el comparador que se escoja. */
    public ListaDoble<Apartamento> disponiblesOrdenados(Estancia estancia, int personas,
                                                        Comparator<Apartamento> orden) {
        if (orden == null) {
            throw new IllegalArgumentException("El orden no puede ser nulo");
        }
        ListaDoble<Apartamento> libres = disponibles(estancia, personas);
        libres.ordenar(orden);
        return libres;
    }

    /** Altura del arbol del catalogo. */
    public int altura() {
        return arbol.altura();
    }

    /** Cantidad de apartamentos. */
    public int cantidad() {
        return arbol.tamanio();
    }

    /** Recorrido preorden (para administracion). */
    public ListaDoble<Apartamento> preorden() {
        return arbol.preorden();
    }

    /** Recorrido postorden (para administracion). */
    public ListaDoble<Apartamento> postorden() {
        return arbol.postorden();
    }
}
