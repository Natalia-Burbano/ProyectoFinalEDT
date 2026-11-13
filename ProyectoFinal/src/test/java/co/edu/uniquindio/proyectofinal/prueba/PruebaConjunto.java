package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.Conjunto;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/** Pruebas del Conjunto. */
public class PruebaConjunto {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /** Casos de prueba del conjunto. */
    public static void ejecutar() {
        Prueba.titulo("Conjunto");

        Conjunto<String> c = new Conjunto<>();
        Prueba.verificar(c.estaVacio(), "un conjunto nuevo esta vacio");
        Prueba.verificar(c.tamanio() == 0, "un conjunto nuevo tiene tamanio 0");
        Prueba.verificar(c.toString().equals("{}"), "un conjunto vacio se muestra como {}");

        // agregar sin repetidos
        Prueba.verificar(c.agregar("A"), "agregar A devuelve true");
        Prueba.verificar(c.agregar("B"), "agregar B devuelve true");
        Prueba.verificar(!c.agregar("A"), "el segundo A devuelve false");
        Prueba.verificar(c.tamanio() == 2, "con A, B, A el tamanio es 2");
        Prueba.verificar(c.toString().equals("{A, B}"), "toString da {A, B}");

        // contiene
        Prueba.verificar(c.contiene("B"), "contiene B");
        Prueba.verificar(!c.contiene("Z"), "no contiene Z");

        // for-each
        int cuenta = 0;
        for (String s : c) {
            cuenta++;
        }
        Prueba.verificar(cuenta == 2, "el for-each recorre 2 elementos");

        // eliminar
        Prueba.verificar(c.eliminar("A"), "eliminar A devuelve true");
        Prueba.verificar(!c.contiene("A"), "despues de eliminar, A ya no esta");
        Prueba.verificar(!c.eliminar("A"), "eliminar uno que no esta devuelve false");
        Prueba.verificar(c.toString().equals("{B}"), "despues de eliminar A queda {B}");

        // eliminar y volver a agregar (la cola queda bien enlazada)
        c.agregar("C");
        Prueba.verificar(c.toString().equals("{B, C}"), "agregar C despues de eliminar da {B, C}");
        c.eliminar("C");
        c.agregar("D");
        Prueba.verificar(c.toString().equals("{B, D}"), "eliminar la cola y agregar D da {B, D}");

        // funciona con otro tipo de dato
        Conjunto<Integer> numeros = new Conjunto<>();
        numeros.agregar(1);
        numeros.agregar(2);
        numeros.agregar(1);
        Prueba.verificar(numeros.tamanio() == 2, "funciona con Integer");

        // null da error
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> c.agregar(null), "agregar null da error");
    }
}