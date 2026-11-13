package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/**
 * Pruebas de la ListaDoble.
 * Por ahora solo prueba agregar, tamanio, estaVacia y toString.
 * Las demas pruebas se agregan cuando esten los otros metodos.
 */
public class PruebaListaDoble {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /**
     * Casos de prueba de la lista.
     */
    public static void ejecutar() {
        Prueba.titulo("ListaDoble");

        // lista vacia
        ListaDoble<String> lista = new ListaDoble<>();
        Prueba.verificar(lista.estaVacia(), "una lista nueva esta vacia");
        Prueba.verificar(lista.tamanio() == 0, "una lista nueva tiene tamanio 0");
        Prueba.verificar(lista.toString().equals("[]"), "una lista vacia se muestra como []");

        // agregar al final
        lista.agregarAlFinal("B");
        lista.agregarAlFinal("C");
        Prueba.verificar(lista.toString().equals("[B <-> C]"), "agregar B y C al final da [B <-> C]");

        // agregar al inicio
        lista.agregarAlInicio("A");
        Prueba.verificar(lista.toString().equals("[A <-> B <-> C]"), "agregar A al inicio da [A <-> B <-> C]");
        Prueba.verificar(lista.tamanio() == 3, "despues de agregar 3 el tamanio es 3");
        Prueba.verificar(!lista.estaVacia(), "con elementos ya no esta vacia");

        // agregar al inicio en una lista vacia
        ListaDoble<String> otra = new ListaDoble<>();
        otra.agregarAlInicio("X");
        Prueba.verificar(otra.toString().equals("[X]"), "agregar al inicio en una lista vacia da [X]");

        // funciona con otro tipo de dato
        ListaDoble<Integer> numeros = new ListaDoble<>();
        numeros.agregarAlFinal(5);
        numeros.agregarAlFinal(10);
        Prueba.verificar(numeros.toString().equals("[5 <-> 10]"), "funciona con Integer");

        // null da error
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> lista.agregarAlFinal(null), "agregar null al final da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> lista.agregarAlInicio(null), "agregar null al inicio da error");
        Prueba.verificar(lista.tamanio() == 3, "despues de intentar agregar null el tamanio sigue en 3");
    }
}