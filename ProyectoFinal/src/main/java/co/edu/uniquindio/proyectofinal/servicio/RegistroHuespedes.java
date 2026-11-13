package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;

/** Registro de huespedes guardado en un arbol binario de busqueda (por documento). */
public class RegistroHuespedes {

    private final ArbolBinarioBusqueda<Huesped> arbol = new ArbolBinarioBusqueda<>();

    /** Registra un huesped. Documento repetido: IllegalStateException. */
    public void registrar(Huesped huesped) {
        if (huesped == null) {
            throw new IllegalArgumentException("El huesped no puede ser nulo");
        }
        if (!arbol.insertar(huesped)) {
            throw new IllegalStateException("Ya existe un huesped con el documento " + huesped.getDocumento());
        }
    }

    /** Busca por documento. Devuelve null si no existe. */
    public Huesped buscar(String documento) {
        if (documento == null || documento.isBlank()) {
            return null;
        }
        return arbol.buscar(Huesped.soloDocumento(documento));
    }

    /** Elimina por documento. Devuelve false si no existia. */
    public boolean eliminar(String documento) {
        Huesped huesped = buscar(documento);
        if (huesped == null) {
            return false;
        }
        return arbol.eliminar(huesped);
    }

    /** Todos los huespedes ordenados por documento. */
    public ListaDoble<Huesped> todos() {
        return arbol.inorden();
    }

    /** Cantidad de huespedes. */
    public int cantidad() {
        return arbol.tamanio();
    }

    /** Altura del arbol. */
    public int altura() {
        return arbol.altura();
    }

    /** Recorrido preorden (para administracion). */
    public ListaDoble<Huesped> preorden() {
        return arbol.preorden();
    }

    /** Recorrido postorden (para administracion). */
    public ListaDoble<Huesped> postorden() {
        return arbol.postorden();
    }
}
