package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.Cola;
import co.edu.uniquindio.proyectofinal.estructuras.Pila;
import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/**
 * Pruebas de la Pila y la Cola.
 */
public class PruebaPilaCola {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /**
     * Casos de prueba de la pila y la cola.
     */
    public static void ejecutar() {
        Prueba.titulo("Pila");

        Pila<Integer> pila = new Pila<>();
        Prueba.verificar(pila.estaVacia(), "una pila nueva esta vacia");
        pila.apilar(1);
        pila.apilar(2);
        pila.apilar(3);
        Prueba.verificar(pila.tamanio() == 3, "despues de apilar 3 el tamanio es 3");
        Prueba.verificar(pila.cima() == 3, "la cima es 3 (el ultimo que entro)");
        Prueba.verificar(pila.tamanio() == 3, "ver la cima no saca nada");

        String recorrido = "";
        for (Integer n : pila) {
            recorrido += n + " ";
        }
        Prueba.verificar(recorrido.trim().equals("3 2 1"), "el for-each va de la cima hacia abajo: 3 2 1");

        Prueba.verificar(pila.desapilar() == 3, "desapilar da 3");
        Prueba.verificar(pila.desapilar() == 2, "desapilar da 2");
        Prueba.verificar(pila.desapilar() == 1, "desapilar da 1");
        Prueba.verificar(pila.estaVacia(), "despues de sacar todo queda vacia");

        Prueba.esperarExcepcion(EstructuraVaciaException.class, pila::desapilar, "desapilar una pila vacia da error");
        Prueba.esperarExcepcion(EstructuraVaciaException.class, pila::cima, "ver la cima de una pila vacia da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class, () -> pila.apilar(null), "apilar null da error");

        Pila<String> pantallas = new Pila<>();
        pantallas.apilar("Inicio");
        pantallas.apilar("Reservas");
        Prueba.verificar(pantallas.desapilar().equals("Reservas"), "funciona con String (como el boton atras)");

        Prueba.titulo("Cola");

        Cola<Integer> cola = new Cola<>();
        Prueba.verificar(cola.estaVacia(), "una cola nueva esta vacia");
        cola.encolar(1);
        cola.encolar(2);
        cola.encolar(3);
        Prueba.verificar(cola.tamanio() == 3, "despues de encolar 3 el tamanio es 3");
        Prueba.verificar(cola.frente() == 1, "el frente es 1 (el primero que entro)");

        String orden = "";
        for (Integer n : cola) {
            orden += n + " ";
        }
        Prueba.verificar(orden.trim().equals("1 2 3"), "el for-each va del frente al final: 1 2 3");

        Prueba.verificar(cola.desencolar() == 1, "desencolar da 1");
        Prueba.verificar(cola.desencolar() == 2, "desencolar da 2");
        Prueba.verificar(cola.desencolar() == 3, "desencolar da 3");
        Prueba.verificar(cola.estaVacia(), "despues de sacar todo queda vacia");

        cola.encolar(7);
        Prueba.verificar(cola.frente() == 7 && cola.tamanio() == 1, "despues de vaciarla se puede volver a encolar");
        cola.desencolar();

        Prueba.esperarExcepcion(EstructuraVaciaException.class, cola::desencolar, "desencolar una cola vacia da error");
        Prueba.esperarExcepcion(EstructuraVaciaException.class, cola::frente, "ver el frente de una cola vacia da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class, () -> cola.encolar(null), "encolar null da error");
    }
}