package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.util.Prueba;

import java.util.Iterator;

/**
 * Pruebas de la ListaDoble.
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

        // ---------- lista vacia ----------
        ListaDoble<String> lista = new ListaDoble<>();
        Prueba.verificar(lista.estaVacia(), "una lista nueva esta vacia");
        Prueba.verificar(lista.tamanio() == 0, "una lista nueva tiene tamanio 0");
        Prueba.verificar(lista.toString().equals("[]"), "una lista vacia se muestra como []");
        Prueba.verificar(!lista.eliminar("A"), "eliminar en una lista vacia devuelve false");

        // ---------- agregar ----------
        lista.agregarAlFinal("B");
        lista.agregarAlFinal("C");
        Prueba.verificar(lista.toString().equals("[B <-> C]"), "agregar B y C al final da [B <-> C]");

        lista.agregarAlInicio("A");
        Prueba.verificar(lista.toString().equals("[A <-> B <-> C]"), "agregar A al inicio da [A <-> B <-> C]");
        Prueba.verificar(lista.tamanio() == 3, "despues de agregar 3 el tamanio es 3");
        Prueba.verificar(!lista.estaVacia(), "con elementos ya no esta vacia");

        ListaDoble<String> otra = new ListaDoble<>();
        otra.agregarAlInicio("X");
        Prueba.verificar(otra.toString().equals("[X]"), "agregar al inicio en una lista vacia da [X]");

        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> lista.agregarAlFinal(null), "agregar null al final da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> lista.agregarAlInicio(null), "agregar null al inicio da error");
        Prueba.verificar(lista.tamanio() == 3, "despues de intentar agregar null el tamanio sigue en 3");

        // ---------- obtener y contiene ----------
        Prueba.verificar(lista.obtener(0).equals("A"), "obtener(0) da A");
        Prueba.verificar(lista.obtener(1).equals("B"), "obtener(1) da B");
        Prueba.verificar(lista.obtener(2).equals("C"), "obtener(2) da C (lo busca desde la cola)");
        Prueba.esperarExcepcion(IndexOutOfBoundsException.class,
                () -> lista.obtener(-1), "obtener(-1) da error");
        Prueba.esperarExcepcion(IndexOutOfBoundsException.class,
                () -> lista.obtener(lista.tamanio()), "obtener(tamanio) da error");
        Prueba.verificar(lista.contiene("B"), "contiene B");
        Prueba.verificar(!lista.contiene("Z"), "no contiene Z");

        // ---------- eliminar sus cuatro casos ----------
        ListaDoble<String> letras = new ListaDoble<>();
        letras.agregarAlFinal("A");
        letras.agregarAlFinal("B");
        letras.agregarAlFinal("C");
        letras.agregarAlFinal("D");

        Prueba.verificar(letras.eliminar("A"), "eliminar la cabeza devuelve true");
        Prueba.verificar(letras.toString().equals("[B <-> C <-> D]"), "sin la cabeza queda [B <-> C <-> D]");

        Prueba.verificar(letras.eliminar("D"), "eliminar la cola devuelve true");
        Prueba.verificar(letras.toString().equals("[B <-> C]"), "sin la cola queda [B <-> C]");

        letras.agregarAlFinal("E");
        Prueba.verificar(letras.toString().equals("[B <-> C <-> E]"),
                "despues de quitar la cola, agregar al final queda bien enlazado");
        Prueba.verificar(alReves(letras).equals("E C B"), "y al reves tambien queda bien: E C B");

        Prueba.verificar(letras.eliminar("C"), "eliminar uno del medio devuelve true");
        Prueba.verificar(letras.toString().equals("[B <-> E]"), "sin el del medio queda [B <-> E]");
        Prueba.verificar(alReves(letras).equals("E B"), "al reves despues de quitar el del medio da E B");

        Prueba.verificar(!letras.eliminar("Z"), "eliminar uno que no esta devuelve false");

        ListaDoble<String> uno = new ListaDoble<>();
        uno.agregarAlFinal("U");
        Prueba.verificar(uno.eliminar("U"), "eliminar el unico devuelve true");
        Prueba.verificar(uno.estaVacia() && uno.toString().equals("[]"), "despues de quitar el unico queda vacia");
        uno.agregarAlFinal("V");
        uno.agregarAlInicio("T");
        Prueba.verificar(uno.toString().equals("[T <-> V]"), "despues de vaciarla se puede volver a agregar");
        Prueba.verificar(alReves(uno).equals("V T"), "y queda bien enlazada al reves: V T");

        // ---------- eliminarEn ----------
        ListaDoble<String> pos = new ListaDoble<>();
        pos.agregarAlFinal("P");
        pos.agregarAlFinal("Q");
        pos.agregarAlFinal("R");
        Prueba.verificar(pos.eliminarEn(1).equals("Q"), "eliminarEn(1) devuelve Q");
        Prueba.verificar(pos.toString().equals("[P <-> R]"), "despues de eliminarEn(1) queda [P <-> R]");
        Prueba.esperarExcepcion(IndexOutOfBoundsException.class,
                () -> pos.eliminarEn(5), "eliminarEn(5) da error");

        // ---------- iteradores ----------
        String adelante = "";
        for (String s : lista) {
            adelante += s + " ";
        }
        Prueba.verificar(adelante.trim().equals("A B C"), "el for-each da A B C");
        Prueba.verificar(alReves(lista).equals("C B A"), "el iterador inverso da C B A");

        Iterator<String> it = otra.iterator();
        it.next();
        Prueba.esperarExcepcion(java.util.NoSuchElementException.class,
                it::next, "next() sin mas elementos da error");

        // ---------- ordenar ----------
        ListaDoble<Integer> numeros = new ListaDoble<>();
        numeros.agregarAlFinal(5);
        numeros.agregarAlFinal(1);
        numeros.agregarAlFinal(4);
        numeros.agregarAlFinal(2);
        numeros.ordenar((a, b) -> Integer.compare(a, b));
        Prueba.verificar(numeros.toString().equals("[1 <-> 2 <-> 4 <-> 5]"), "ordenar [5, 1, 4, 2] da [1, 2, 4, 5]");
        Prueba.verificar(alReves(numeros).equals("5 4 2 1"), "despues de ordenar queda bien enlazada al reves");
        Prueba.verificar(numeros.tamanio() == 4, "ordenar no cambia el tamanio");

        numeros.ordenar((a, b) -> Integer.compare(b, a));
        Prueba.verificar(numeros.toString().equals("[5 <-> 4 <-> 2 <-> 1]"), "con el comparador al reves da [5, 4, 2, 1]");

        ListaDoble<String> palabras = new ListaDoble<>();
        palabras.agregarAlFinal("pera");
        palabras.agregarAlFinal("banano");
        palabras.agregarAlFinal("mango");
        palabras.ordenar((a, b) -> a.compareTo(b));
        Prueba.verificar(palabras.toString().equals("[banano <-> mango <-> pera]"), "ordenar funciona con String");

        // ---------- copia ----------
        ListaDoble<String> copia = lista.copia();
        Prueba.verificar(copia.toString().equals(lista.toString()), "la copia tiene lo mismo");
        copia.agregarAlFinal("Z");
        Prueba.verificar(lista.tamanio() == 3, "cambiar la copia no cambia la original");
    }

    /**
     * Recorre la lista con el iterador inverso y la devuelve como texto, por ejemplo "C B A".
     */
    private static <T> String alReves(ListaDoble<T> lista) {
        String texto = "";
        Iterator<T> it = lista.iteradorInverso();
        while (it.hasNext()) {
            texto += it.next() + " ";
        }
        return texto.trim();
    }
}