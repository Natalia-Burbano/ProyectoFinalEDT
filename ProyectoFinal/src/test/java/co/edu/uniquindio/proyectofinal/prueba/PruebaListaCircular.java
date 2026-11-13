package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ListaCircular;
import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/** Pruebas de la ListaCircular. */
public class PruebaListaCircular {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /** Casos de prueba de la lista circular. */
    public static void ejecutar() {
        Prueba.titulo("ListaCircular");

        ListaCircular<String> lista = new ListaCircular<>();
        Prueba.verificar(lista.estaVacia(), "una lista nueva esta vacia");
        Prueba.verificar(lista.tamanio() == 0, "una lista nueva tiene tamanio 0");

        // vacia: actual y avanzar dan error
        Prueba.esperarExcepcion(EstructuraVaciaException.class,
                () -> lista.actual(), "actual en una lista vacia da EstructuraVaciaException");
        Prueba.esperarExcepcion(EstructuraVaciaException.class,
                () -> lista.avanzar(), "avanzar en una lista vacia da EstructuraVaciaException");

        // agregar
        lista.agregar("R1");
        lista.agregar("R2");
        lista.agregar("R3");
        Prueba.verificar(lista.tamanio() == 3, "despues de agregar 3 el tamanio es 3");
        Prueba.verificar(lista.actual().equals("R1"), "el actual al inicio es R1");

        // avanzar 4 veces: R2, R3, R1, R2
        Prueba.verificar(lista.avanzar().equals("R2"), "avanzar 1 da R2");
        Prueba.verificar(lista.avanzar().equals("R3"), "avanzar 2 da R3");
        Prueba.verificar(lista.avanzar().equals("R1"), "avanzar 3 da R1 (vuelve al primero)");
        Prueba.verificar(lista.avanzar().equals("R2"), "avanzar 4 da R2");

        // for-each: una sola vuelta, empezando en el actual (R2)
        int cuenta = 0;
        String primero = null;
        for (String s : lista) {
            if (cuenta == 0) {
                primero = s;
            }
            cuenta++;
        }
        Prueba.verificar(cuenta == 3, "el for-each da una sola vuelta de 3 elementos");
        Prueba.verificar("R2".equals(primero), "el for-each empieza en el actual (R2)");

        // eliminar el actual: el actual pasa al siguiente
        Prueba.verificar(lista.eliminar("R2"), "eliminar R2 (el actual) devuelve true");
        Prueba.verificar(lista.actual().equals("R3"), "el actual pasa a R3");
        Prueba.verificar(lista.tamanio() == 2, "despues de eliminar el tamanio es 2");
        Prueba.verificar(!lista.eliminar("R9"), "eliminar uno que no esta devuelve false");

        // sigue girando bien con 2 elementos
        Prueba.verificar(lista.avanzar().equals("R1"), "avanzar despues de R3 da R1");
        Prueba.verificar(lista.avanzar().equals("R3"), "avanzar despues de R1 da R3");

        // eliminar el ultimo de la vuelta y agregar de nuevo (queda bien enlazada)
        lista.eliminar("R3");
        lista.agregar("R4");
        int cuenta2 = 0;
        for (String s : lista) {
            cuenta2++;
        }
        Prueba.verificar(cuenta2 == 2, "eliminar R3 y agregar R4 deja 2 elementos");

        // eliminar hasta dejarla vacia
        lista.eliminar("R1");
        lista.eliminar("R4");
        Prueba.verificar(lista.estaVacia(), "al eliminar todos queda vacia");
        Prueba.esperarExcepcion(EstructuraVaciaException.class,
                () -> lista.actual(), "vacia otra vez: actual da EstructuraVaciaException");

        // null da error
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> lista.agregar(null), "agregar null da error");
    }
}