package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Empleado;

/** Los empleados guardados en un arbol binario de busqueda, por documento. */
public class RegistroEmpleados {

    private final ArbolBinarioBusqueda<Empleado> arbol = new ArbolBinarioBusqueda<>();

    /** Registra un empleado. Documento repetido: IllegalStateException. */
    public void registrar(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        if (!arbol.insertar(empleado)) {
            throw new IllegalStateException("Ya existe un empleado con el documento "
                    + empleado.getDocumento());
        }
    }

    /** Busca por documento. Si no esta devuelve null. */
    public Empleado buscar(String documento) {
        return arbol.buscar(Empleado.soloDocumento(documento));
    }

    /** Elimina por documento. Devuelve false si no existia. */
    public boolean eliminar(String documento) {
        return arbol.eliminar(Empleado.soloDocumento(documento));
    }

    /** Todos los empleados, ordenados por documento (recorrido inorden). */
    public ListaDoble<Empleado> todos() {
        return arbol.inorden();
    }

    public int cantidad() {
        return arbol.tamanio();
    }

    // Los usa el panel de administracion
    public int altura() { return arbol.altura(); }
    public ListaDoble<Empleado> preorden() { return arbol.preorden(); }
    public ListaDoble<Empleado> postorden() { return arbol.postorden(); }
}