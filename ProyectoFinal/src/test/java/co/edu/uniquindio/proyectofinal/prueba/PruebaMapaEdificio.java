package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.Conjunto;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.GrupoBloque;
import co.edu.uniquindio.proyectofinal.modelo.NodoMapa;
import co.edu.uniquindio.proyectofinal.modelo.Ruta;
import co.edu.uniquindio.proyectofinal.modelo.TipoNodo;
import co.edu.uniquindio.proyectofinal.servicio.MapaEdificio;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/** Pruebas del MapaEdificio. */
public class PruebaMapaEdificio {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /** Casos de prueba del mapa. */
    public static void ejecutar() {
        Prueba.titulo("MapaEdificio");

        // REC-A101 (10), REC-ESC (8), ESC-A201 (6), A101-A201 (20)
        MapaEdificio mapa = new MapaEdificio();
        mapa.agregarNodo(new NodoMapa("REC", TipoNodo.RECEPCION, "Recepcion", "A", 1, 0, 0, null));
        mapa.agregarNodo(new NodoMapa("ESC", TipoNodo.ESCALERA, "Escalera", "A", 1, 0, 0, null));
        mapa.agregarNodo(new NodoMapa("A101", TipoNodo.APARTAMENTO, "Apto 101", "A", 1, 0, 0, "A101"));
        mapa.agregarNodo(new NodoMapa("A201", TipoNodo.APARTAMENTO, "Apto 201", "A", 2, 0, 0, "A201"));
        mapa.conectar("REC", "A101", 10);
        mapa.conectar("REC", "ESC", 8);
        mapa.conectar("ESC", "A201", 6);
        mapa.conectar("A101", "A201", 20);

        // registro y busqueda
        Prueba.verificar(mapa.buscarNodo("A101") != null, "buscarNodo encuentra A101");
        Prueba.verificar(mapa.buscarNodo("Z999") == null, "buscarNodo de uno que no existe da null");
        Prueba.esperarExcepcion(IllegalStateException.class,
                () -> mapa.agregarNodo(new NodoMapa("REC", TipoNodo.RECEPCION, "Otra", "A", 1, 0, 0, null)),
                "un id repetido da IllegalStateException");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> mapa.conectar("REC", "Z999", 5), "conectar con un nodo que no existe da error");

        // Dijkstra: REC -> ESC -> A201 con 14
        Ruta ruta = mapa.rutaMasCorta("REC", "A201");
        Prueba.verificar(ruta != null && ruta.getDistancia() == 14, "la ruta de REC a A201 mide 14");
        Prueba.verificar(ruta.getNodos().tamanio() == 3, "la ruta tiene 3 nodos");
        Prueba.verificar(ruta.getNodos().obtener(1).getId().equals("ESC"), "pasa por la escalera");
        Prueba.verificar(ruta.toString().equals("REC -> ESC -> A201 (14 m)"), "toString de la ruta");

        // cerrar ESC-A201: la ruta cambia a REC, A101, A201 con 30
        mapa.cerrarPasillo("ESC", "A201");
        Ruta alterna = mapa.rutaMasCorta("REC", "A201");
        Prueba.verificar(alterna.getDistancia() == 30, "con ESC-A201 cerrado la ruta mide 30");
        Prueba.verificar(alterna.getNodos().obtener(1).getId().equals("A101"), "ahora pasa por A101");
        Prueba.verificar(mapa.esConexo(), "todavia se llega a todo");

        // cerrar tambien A101-A201: A201 queda inalcanzable
        mapa.cerrarPasillo("A101", "A201");
        Prueba.verificar(mapa.rutaMasCorta("REC", "A201") == null, "sin camino la ruta es null");
        Prueba.verificar(mapa.inalcanzablesDesde("REC").tamanio() == 1, "queda 1 apartamento inalcanzable");
        Prueba.verificar(mapa.inalcanzablesDesde("REC").obtener(0).getId().equals("A201"),
                "el inalcanzable es A201");
        Prueba.verificar(!mapa.esConexo(), "esConexo da false");

        // reabrir todo
        mapa.abrirPasillo("ESC", "A201");
        mapa.abrirPasillo("A101", "A201");
        Prueba.verificar(mapa.esConexo(), "al abrir los pasillos vuelve a ser conexo");

        // misma ruta de un nodo a si mismo
        Prueba.verificar(mapa.rutaMasCorta("REC", "REC").getDistancia() == 0, "de REC a REC mide 0");

        // BFS: REC, luego sus vecinos
        ListaDoble<NodoMapa> bfs = mapa.recorridoBFS("REC");
        Prueba.verificar(bfs.tamanio() == 4, "BFS visita los 4 nodos");
        Prueba.verificar(bfs.obtener(0).getId().equals("REC"), "BFS empieza en REC");
        Prueba.verificar(bfs.obtener(1).getId().equals("A101") && bfs.obtener(2).getId().equals("ESC"),
                "BFS sigue con los vecinos de REC");

        // DFS: cada nodo una sola vez
        ListaDoble<NodoMapa> dfs = mapa.recorridoDFS("REC");
        Conjunto<String> vistos = new Conjunto<>();
        boolean sinRepetidos = true;
        for (NodoMapa n : dfs) {
            if (!vistos.agregar(n.getId())) {
                sinRepetidos = false;
            }
        }
        Prueba.verificar(dfs.tamanio() == 4 && sinRepetidos, "DFS visita cada nodo una sola vez");
        Prueba.verificar(dfs.obtener(0).getId().equals("REC"), "DFS empieza en REC");

        // ruta de preparacion: REC -> A201 (14) -> A101 (20) = 34
        ListaDoble<String> codigos = new ListaDoble<>();
        codigos.agregarAlFinal("A201");
        codigos.agregarAlFinal("A101");
        Ruta preparacion = mapa.rutaPreparacion(codigos);
        Prueba.verificar(preparacion.getDistancia() == 34, "la ruta de preparacion mide 34");
        Prueba.verificar(preparacion.toString().equals("REC -> ESC -> A201 -> A101 (34 m)"),
                "la ruta de preparacion sale de REC y pasa por A201 y luego A101");
        Prueba.verificar(mapa.rutaPreparacion(new ListaDoble<String>()).getNodos().tamanio() == 1,
                "sin apartamentos pendientes la ruta es solo REC");
        codigos.agregarAlInicio("C999"); // un apartamento que no esta dibujado en el mapa
        Prueba.verificar(mapa.rutaPreparacion(codigos).getDistancia() == 34,
                "un apartamento que no esta en el mapa se salta y la ruta de los demas sigue igual");

        // agrupar por bloque
        MapaEdificio mapa2 = new MapaEdificio();
        mapa2.agregarNodo(new NodoMapa("REC", TipoNodo.RECEPCION, "Recepcion", "A", 1, 0, 0, null));
        mapa2.agregarNodo(new NodoMapa("A101", TipoNodo.APARTAMENTO, "Apto A101", "A", 1, 0, 0, "A101"));
        mapa2.agregarNodo(new NodoMapa("A102", TipoNodo.APARTAMENTO, "Apto A102", "A", 1, 0, 0, "A102"));
        mapa2.agregarNodo(new NodoMapa("B101", TipoNodo.APARTAMENTO, "Apto B101", "B", 1, 0, 0, "B101"));
        mapa2.conectar("REC", "A101", 5);
        mapa2.conectar("REC", "A102", 6);
        mapa2.conectar("REC", "B101", 7);
        ListaDoble<GrupoBloque> grupos = mapa2.agruparPorBloque();
        Prueba.verificar(grupos.tamanio() == 2, "hay 2 bloques");
        Prueba.verificar(grupos.obtener(0).getBloque().equals("A") && grupos.obtener(0).cantidad() == 2,
                "el bloque A tiene 2 apartamentos");
        Prueba.verificar(grupos.obtener(1).getBloque().equals("B") && grupos.obtener(1).cantidad() == 1,
                "el bloque B tiene 1 apartamento");

        // errores
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> mapa.rutaMasCorta("REC", "Z999"), "ruta a un nodo que no existe da error");
    }
}