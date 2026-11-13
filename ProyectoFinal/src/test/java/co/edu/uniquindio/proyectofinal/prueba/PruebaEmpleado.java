package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Empleado;
import co.edu.uniquindio.proyectofinal.modelo.Rol;
import co.edu.uniquindio.proyectofinal.util.Prueba;

import java.util.Comparator;

/** Pruebas de Empleado. */
public class PruebaEmpleado {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /** Casos de prueba del empleado. */
    public static void ejecutar() {
        Prueba.titulo("Empleado");

        Empleado e1 = new Empleado("300", "Carolina", Rol.RECEPCIONISTA, "3101110001");
        Empleado e2 = new Empleado("100", "Jairo", Rol.PREPARACION, "3101110004");
        Empleado e3 = new Empleado("200", "Gustavo", Rol.ADMINISTRADOR, "3101110006");

        // compareTo y equals por documento
        Prueba.verificar(e2.compareTo(e3) < 0, "100 va antes que 200");
        Prueba.verificar(e1.compareTo(e3) > 0, "300 va despues de 200");
        Prueba.verificar(e1.compareTo(new Empleado("300", "Otro", Rol.ADMINISTRADOR, null)) == 0,
                "mismo documento: compareTo da 0");
        Prueba.verificar(e1.equals(new Empleado("300", "Otro", Rol.ADMINISTRADOR, null)),
                "mismo documento: son iguales");
        Prueba.verificar(!e1.equals(e2), "distinto documento: no son iguales");

        // soloDocumento sirve para buscar
        Empleado clave = Empleado.soloDocumento("300");
        Prueba.verificar(clave.equals(e1), "soloDocumento es igual al empleado con ese documento");
        Prueba.verificar(clave.getNombre() == null, "soloDocumento no tiene nombre");

        // ordenar los deja por documento
        ListaDoble<Empleado> lista = new ListaDoble<>();
        lista.agregarAlFinal(e1);
        lista.agregarAlFinal(e2);
        lista.agregarAlFinal(e3);
        lista.ordenar(Comparator.naturalOrder());
        Prueba.verificar(lista.obtener(0).getDocumento().equals("100"), "ordenado: primero 100");
        Prueba.verificar(lista.obtener(1).getDocumento().equals("200"), "ordenado: segundo 200");
        Prueba.verificar(lista.obtener(2).getDocumento().equals("300"), "ordenado: tercero 300");

        // errores
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new Empleado("", "Sin documento", Rol.RECEPCIONISTA, null),
                "documento vacio da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new Empleado("400", null, Rol.RECEPCIONISTA, null),
                "nombre nulo da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new Empleado("400", "Sin rol", null, null),
                "rol nulo da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> Empleado.soloDocumento(null),
                "soloDocumento(null) da error");
    }
}