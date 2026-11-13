package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.Grafo;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/** Pruebas del Grafo. */
public class PruebaGrafo {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /** Cuenta los vecinos (por aristas abiertas) de un vertice. */
    private static int contarVecinos(Grafo<String> g, String v) {
        int n = 0;
        for (String vecino : g.vecinos(v)) {
            n++;
        }
        return n;
    }

    /** Casos de prueba del grafo. */
    public static void ejecutar() {
        Prueba.titulo("Grafo");

        // vertices
        Grafo<String> g = new Grafo<>();
        Prueba.verificar(g.cantidadVertices() == 0, "un grafo nuevo no tiene vertices");
        Prueba.verificar(g.agregarVertice("Recepcion"), "agregar Recepcion devuelve true");
        g.agregarVertice("A101");
        g.agregarVertice("A102");
        g.agregarVertice("Escalera");
        Prueba.verificar(!g.agregarVertice("A101"), "no acepta vertices repetidos");
        Prueba.verificar(g.cantidadVertices() == 4, "hay 4 vertices");
        Prueba.verificar(g.existeVertice("A102"), "existeVertice A102");
        Prueba.verificar(!g.existeVertice("Z999"), "no existe Z999");

        // aristas: Recepcion-A101 (10), A101-A102 (5), Recepcion-Escalera (8)
        g.agregarArista("Recepcion", "A101", 10);
        g.agregarArista("A101", "A102", 5);
        g.agregarArista("Recepcion", "Escalera", 8);

        Prueba.verificar(g.peso("Recepcion", "A101") == 10, "peso Recepcion-A101 es 10");
        Prueba.verificar(g.peso("A101", "Recepcion") == 10, "conecta en los dos sentidos");
        Prueba.verificar(g.peso("A102", "Escalera") == -1, "si no estan conectados el peso es -1");
        Prueba.verificar(g.estaAbierta("A101", "A102"), "la arista nueva esta abierta");

        // vecinos de A101: Recepcion y A102
        Prueba.verificar(contarVecinos(g, "A101") == 2, "A101 tiene 2 vecinos");
        boolean tieneRecepcion = false;
        boolean tieneA102 = false;
        for (String v : g.vecinos("A101")) {
            if (v.equals("Recepcion")) {
                tieneRecepcion = true;
            }
            if (v.equals("A102")) {
                tieneA102 = true;
            }
        }
        Prueba.verificar(tieneRecepcion && tieneA102, "los vecinos de A101 son Recepcion y A102");

        // cerrar una arista
        g.cerrarArista("A101", "A102");
        Prueba.verificar(contarVecinos(g, "A101") == 1, "cerrada, A102 ya no es vecino de A101");
        Prueba.verificar(contarVecinos(g, "A102") == 0, "cerrada, A101 ya no es vecino de A102");
        Prueba.verificar(g.peso("A101", "A102") == 5, "cerrada, el peso sigue siendo 5");
        Prueba.verificar(!g.estaAbierta("A101", "A102"), "estaAbierta da false");
        Prueba.verificar(!g.estaAbierta("A102", "A101"), "estaAbierta da false en el otro sentido");

        // abrirla de nuevo
        g.abrirArista("A101", "A102");
        Prueba.verificar(contarVecinos(g, "A101") == 2, "al abrirla vuelve a ser vecino");
        Prueba.verificar(g.estaAbierta("A101", "A102"), "estaAbierta da true");

        // agregar una arista que ya existe solo cambia el peso
        g.agregarArista("A101", "A102", 7);
        Prueba.verificar(g.peso("A102", "A101") == 7, "repetir una arista cambia el peso");
        Prueba.verificar(contarVecinos(g, "A101") == 2, "y no duplica los vecinos");

        // indices
        Prueba.verificar(g.indiceDe("Recepcion") == 0, "indiceDe Recepcion es 0");
        Prueba.verificar(g.indiceDe("A102") == 2, "indiceDe A102 es 2");
        Prueba.verificar(g.indiceDe("Z999") == -1, "indiceDe de uno que no existe es -1");
        Prueba.verificar(g.verticeEn(1).equals("A101"), "verticeEn(1) es A101");

        // for-each sobre los vertices
        int cuenta = 0;
        for (String v : g) {
            cuenta++;
        }
        Prueba.verificar(cuenta == 4, "el for-each recorre los 4 vertices");

        // errores
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> g.agregarArista("A101", "Fantasma", 3), "arista con un vertice que no existe da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> g.agregarArista("A101", "A102", 0), "peso 0 da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> g.agregarArista("A101", "A101", 3), "conectar un vertice consigo mismo da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> g.cerrarArista("A102", "Escalera"), "cerrar una arista que no existe da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> g.vecinos("Z999"), "vecinos de un vertice que no existe da error");
        Prueba.esperarExcepcion(IndexOutOfBoundsException.class,
                () -> g.verticeEn(9), "verticeEn fuera de rango da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> g.agregarVertice(null), "agregar un vertice null da error");
    }
}