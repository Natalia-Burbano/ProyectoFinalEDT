package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.modelo.NodoMapa;
import co.edu.uniquindio.proyectofinal.modelo.TipoNodo;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/** Pruebas de NodoMapa. */
public class PruebaNodoMapa {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /** Casos de prueba del nodo del mapa. */
    public static void ejecutar() {
        Prueba.titulo("NodoMapa");

        NodoMapa a = new NodoMapa("A101", TipoNodo.APARTAMENTO, "Cafetal", "A", 1, 150, 400, "A101");
        NodoMapa b = new NodoMapa("A101", TipoNodo.APARTAMENTO, "Otro nombre", "B", 2, 0, 0, "A101");
        NodoMapa c = new NodoMapa("REC", TipoNodo.RECEPCION, "Recepcion", "A", 1, 450, 500, null);

        // iguales por id
        Prueba.verificar(a.equals(b), "dos nodos con el mismo id son iguales");
        Prueba.verificar(a.hashCode() == b.hashCode(), "y tienen el mismo hashCode");
        Prueba.verificar(!a.equals(c), "nodos con distinto id no son iguales");
        Prueba.verificar(!a.equals(null), "un nodo no es igual a null");

        // toString
        Prueba.verificar(a.toString().equals("Cafetal"), "toString devuelve el nombre");

        // datos
        Prueba.verificar(a.getTipo() == TipoNodo.APARTAMENTO, "guarda el tipo");
        Prueba.verificar(a.getCodigoApartamento().equals("A101"), "un apartamento guarda su codigo");
        Prueba.verificar(c.getCodigoApartamento() == null, "una recepcion no tiene codigo de apartamento");
        Prueba.verificar(a.getX() == 150 && a.getY() == 400, "guarda la posicion x, y");
        Prueba.verificar(a.getPiso() == 1 && a.getBloque().equals("A"), "guarda bloque y piso");

        // errores
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new NodoMapa("", TipoNodo.RECEPCION, "Recepcion", "A", 1, 0, 0, null),
                "id vacio da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new NodoMapa("X1", null, "Nodo", "A", 1, 0, 0, null),
                "tipo nulo da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new NodoMapa("X1", TipoNodo.ASEO, " ", "A", 1, 0, 0, null),
                "nombre vacio da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new NodoMapa("A999", TipoNodo.APARTAMENTO, "Sin codigo", "A", 1, 0, 0, null),
                "un apartamento sin codigo da error");
    }
}