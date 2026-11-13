package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ColaPrioridad;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/**
 * Pruebas de la ColaPrioridad.
 */
public class PruebaColaPrioridad {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /**
     * Casos de prueba de la cola de prioridad.
     */
    public static void ejecutar() {
        Prueba.titulo("ColaPrioridad");

        // menor primero
        ColaPrioridad<Integer> cola = new ColaPrioridad<>((a, b) -> Integer.compare(a, b));
        Prueba.verificar(cola.estaVacia(), "una cola nueva esta vacia");

        cola.insertar(5);
        cola.insertar(1);
        cola.insertar(4);
        cola.insertar(2);
        cola.insertar(3);
        Prueba.verificar(cola.tamanio() == 5, "despues de insertar 5 el tamanio es 5");
        Prueba.verificar(cola.verPrimero() == 1, "el primero es 1");
        Prueba.verificar(cola.tamanio() == 5, "ver el primero no saca nada");

        String recorrido = "";
        for (Integer n : cola) {
            recorrido += n + " ";
        }
        Prueba.verificar(recorrido.trim().equals("1 2 3 4 5"), "el for-each da 1 2 3 4 5 (en orden de salida)");

        ListaDoble<Integer> copia = cola.enOrden();
        Prueba.verificar(copia.toString().equals("[1 <-> 2 <-> 3 <-> 4 <-> 5]"), "enOrden da [1, 2, 3, 4, 5]");
        Prueba.verificar(cola.tamanio() == 5, "enOrden no vacia la cola");

        String salida = "";
        while (!cola.estaVacia()) {
            salida += cola.extraer() + " ";
        }
        Prueba.verificar(salida.trim().equals("1 2 3 4 5"), "insertar 5, 1, 4, 2, 3 y extraer da 1 a 5");

        Prueba.esperarExcepcion(EstructuraVaciaException.class, cola::extraer, "extraer de una cola vacia da error");
        Prueba.esperarExcepcion(EstructuraVaciaException.class, cola::verPrimero, "ver el primero de una cola vacia da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class, () -> cola.insertar(null), "insertar null da error");

        // mayor primero (comparador al reves)
        ColaPrioridad<Integer> alReves = new ColaPrioridad<>((a, b) -> Integer.compare(b, a));
        alReves.insertar(5);
        alReves.insertar(1);
        alReves.insertar(4);
        alReves.insertar(2);
        alReves.insertar(3);
        String bajando = "";
        while (!alReves.estaVacia()) {
            bajando += alReves.extraer() + " ";
        }
        Prueba.verificar(bajando.trim().equals("5 4 3 2 1"), "con el comparador al reves da 5 a 1");

        // eliminar
        ColaPrioridad<Integer> otra = new ColaPrioridad<>((a, b) -> Integer.compare(a, b));
        otra.insertar(3);
        otra.insertar(1);
        otra.insertar(2);
        Prueba.verificar(otra.eliminar(2), "eliminar uno del medio devuelve true");
        Prueba.verificar(otra.toString().equals("[1, 3]"), "despues de eliminar el del medio queda [1, 3]");
        Prueba.verificar(otra.eliminar(1), "eliminar el primero devuelve true");
        Prueba.verificar(otra.verPrimero() == 3, "despues de eliminar el primero, el primero es 3");
        Prueba.verificar(!otra.eliminar(9), "eliminar uno que no esta devuelve false");
        otra.insertar(0);
        Prueba.verificar(otra.toString().equals("[0, 3]"), "despues de eliminar sigue metiendo en orden");

        // empates: entre iguales sale primero el que llego primero
        ColaPrioridad<String> porLargo = new ColaPrioridad<>((a, b) -> Integer.compare(a.length(), b.length()));
        porLargo.insertar("AAA");
        porLargo.insertar("B");
        porLargo.insertar("CCC");
        porLargo.insertar("D");
        Prueba.verificar(porLargo.toString().equals("[B, D, AAA, CCC]"),
                "con empates sale primero el que llego primero: [B, D, AAA, CCC]");

        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new ColaPrioridad<Integer>(null), "crear la cola sin comparador da error");
    }
}