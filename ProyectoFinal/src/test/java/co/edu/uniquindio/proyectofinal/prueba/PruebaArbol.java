package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/** Pruebas del arbol binario de busqueda. */
public class PruebaArbol {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    public static void ejecutar() {
        Prueba.titulo("ArbolBinarioBusqueda");
        ArbolBinarioBusqueda<Integer> arbol = new ArbolBinarioBusqueda<>();
        Prueba.verificar(arbol.estaVacio() && arbol.altura() == 0, "arbol vacio: tamanio 0 y altura 0");
        Prueba.esperarExcepcion(EstructuraVaciaException.class, () -> arbol.minimo(), "minimo de arbol vacio da error");
        Prueba.esperarExcepcion(EstructuraVaciaException.class, () -> arbol.maximo(), "maximo de arbol vacio da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class, () -> arbol.insertar(null), "insertar null da error");

        int[] datos = {50, 30, 70, 20, 40, 60, 80};
        for (int d : datos) {
            arbol.insertar(d);
        }
        Prueba.verificar(arbol.tamanio() == 7, "tamanio 7");
        Prueba.verificar(arbol.inorden().toString().equals("[20 <-> 30 <-> 40 <-> 50 <-> 60 <-> 70 <-> 80]"), "inorden ordenado");
        Prueba.verificar(arbol.preorden().toString().equals("[50 <-> 30 <-> 20 <-> 40 <-> 70 <-> 60 <-> 80]"), "preorden");
        Prueba.verificar(arbol.postorden().toString().equals("[20 <-> 40 <-> 30 <-> 60 <-> 80 <-> 70 <-> 50]"), "postorden");
        Prueba.verificar(arbol.recorridoPorNiveles().toString().equals("[50 <-> 30 <-> 70 <-> 20 <-> 40 <-> 60 <-> 80]"), "por niveles");
        Prueba.verificar(arbol.altura() == 3, "altura 3");
        Prueba.verificar(!arbol.insertar(40) && arbol.tamanio() == 7, "un repetido devuelve false y no cambia el tamanio");
        Prueba.verificar(arbol.buscar(60) != null && arbol.buscar(60) == 60, "buscar 60 lo encuentra");
        Prueba.verificar(arbol.buscar(65) == null, "buscar 65 da null");
        Prueba.verificar(arbol.contiene(80) && !arbol.contiene(81), "contiene");
        Prueba.verificar(arbol.minimo() == 20 && arbol.maximo() == 80, "minimo 20 y maximo 80");

        int suma = 0;
        for (int v : arbol) {
            suma += v;
        }
        Prueba.verificar(suma == 350, "el for-each recorre los 7 elementos");

        Prueba.verificar(arbol.eliminar(20), "eliminar una hoja (20)");
        Prueba.verificar(arbol.inorden().toString().equals("[30 <-> 40 <-> 50 <-> 60 <-> 70 <-> 80]"), "sin el 20");
        Prueba.verificar(arbol.eliminar(30), "eliminar uno con un hijo (30)");
        Prueba.verificar(arbol.inorden().toString().equals("[40 <-> 50 <-> 60 <-> 70 <-> 80]"), "sin el 30");
        Prueba.verificar(arbol.eliminar(50), "eliminar la raiz con dos hijos (50)");
        Prueba.verificar(arbol.preorden().obtener(0) == 60, "la nueva raiz es 60");
        Prueba.verificar(arbol.inorden().toString().equals("[40 <-> 60 <-> 70 <-> 80]") && arbol.tamanio() == 4,
                "queda ordenado y con tamanio 4");
        Prueba.verificar(!arbol.eliminar(999), "eliminar algo que no esta da false");

        ArbolBinarioBusqueda<String> palabras = new ArbolBinarioBusqueda<>();
        palabras.insertar("mango");
        palabras.insertar("cafe");
        palabras.insertar("zapote");
        Prueba.verificar(palabras.inorden().toString().equals("[cafe <-> mango <-> zapote]"), "funciona con String");
    }
}
