package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.Cola;
import co.edu.uniquindio.proyectofinal.estructuras.Conjunto;
import co.edu.uniquindio.proyectofinal.estructuras.Grafo;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.GrupoBloque;
import co.edu.uniquindio.proyectofinal.modelo.NodoMapa;
import co.edu.uniquindio.proyectofinal.modelo.Ruta;
import co.edu.uniquindio.proyectofinal.modelo.TipoNodo;

import java.util.Objects;

/** Mapa del edificio: un grafo de nodos (apartamentos, recepcion, escaleras, aseo) y pasillos. */
public class MapaEdificio {

    private final Grafo<NodoMapa> grafo = new Grafo<>();

    // ---------- registro y busqueda ----------

    /** Registra un punto del mapa. Un id repetido da IllegalStateException. */
    public void agregarNodo(NodoMapa nodo) {
        if (nodo == null) {
            throw new IllegalArgumentException("El nodo no puede ser nulo");
        }
        if (!grafo.agregarVertice(nodo)) {
            throw new IllegalStateException("Ya existe un nodo con el id " + nodo.getId());
        }
    }

    /** Busca un nodo por su id. Si no esta devuelve null. */
    public NodoMapa buscarNodo(String id) {
        if (id == null) {
            return null;
        }
        for (NodoMapa nodo : grafo) {
            if (nodo.getId().equals(id)) {
                return nodo;
            }
        }
        return null;
    }

    /** Todos los nodos del mapa (para dibujarlos). */
    public ListaDoble<NodoMapa> nodos() {
        ListaDoble<NodoMapa> lista = new ListaDoble<>();
        for (NodoMapa nodo : grafo) {
            lista.agregarAlFinal(nodo);
        }
        return lista;
    }

    // ---------- pasillos ----------

    /** Conecta dos puntos con un pasillo de esos metros. */
    public void conectar(String idA, String idB, double metros) {
        grafo.agregarArista(exigirNodo(idA), exigirNodo(idB), metros);
    }

    public void cerrarPasillo(String idA, String idB) {
        grafo.cerrarArista(exigirNodo(idA), exigirNodo(idB));
    }

    public void abrirPasillo(String idA, String idB) {
        grafo.abrirArista(exigirNodo(idA), exigirNodo(idB));
    }

    /** Metros del pasillo entre dos puntos, o -1 si no hay pasillo (lo usa el mapa para dibujar). */
    public double metrosEntre(String idA, String idB) {
        return grafo.peso(exigirNodo(idA), exigirNodo(idB));
    }

    /** Dice si el pasillo entre dos puntos esta abierto. */
    public boolean pasilloAbierto(String idA, String idB) {
        return grafo.estaAbierta(exigirNodo(idA), exigirNodo(idB));
    }

    // ---------- Dijkstra ----------

    /** Ruta mas corta (Dijkstra con arreglos). Si no hay camino devuelve null. */
    public Ruta rutaMasCorta(String origen, String destino) {
        NodoMapa nodoOrigen = exigirNodo(origen);
        NodoMapa nodoDestino = exigirNodo(destino);

        int n = grafo.cantidadVertices();
        double[] distancia = new double[n];
        int[] anterior = new int[n];
        boolean[] visitado = new boolean[n];
        for (int i = 0; i < n; i++) {
            distancia[i] = Double.POSITIVE_INFINITY;
            anterior[i] = -1;
        }

        int indiceOrigen = grafo.indiceDe(nodoOrigen);
        int indiceDestino = grafo.indiceDe(nodoDestino);
        distancia[indiceOrigen] = 0;

        for (int paso = 0; paso < n; paso++) {
            // El no visitado con menor distancia
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (!visitado[i] && distancia[i] < Double.POSITIVE_INFINITY
                        && (u == -1 || distancia[i] < distancia[u])) {
                    u = i;
                }
            }
            if (u == -1) {
                break;                      // ya no queda nada alcanzable
            }
            visitado[u] = true;
            if (u == indiceDestino) {
                break;                      // ya tenemos la distancia al destino
            }
            // Relajar las aristas abiertas de u
            NodoMapa nodoU = grafo.verticeEn(u);
            for (NodoMapa vecino : grafo.vecinos(nodoU)) {
                int v = grafo.indiceDe(vecino);
                if (visitado[v]) {
                    continue;
                }
                double nueva = distancia[u] + grafo.peso(nodoU, vecino);
                if (nueva < distancia[v]) {
                    distancia[v] = nueva;
                    anterior[v] = u;
                }
            }
        }

        if (distancia[indiceDestino] == Double.POSITIVE_INFINITY) {
            return null;                    // no hay camino
        }

        // Armar el camino yendo hacia atras desde el destino
        ListaDoble<NodoMapa> camino = new ListaDoble<>();
        int actual = indiceDestino;
        while (actual != -1) {
            camino.agregarAlInicio(grafo.verticeEn(actual));
            actual = anterior[actual];
        }
        return new Ruta(camino, distancia[indiceDestino]);
    }

    /**
     * Sale de recepcion y une las rutas mas cortas en el orden de los codigos dados.
     * Si a un apartamento no se llega (pasillos cerrados) se salta.
     * Tambien se salta si el apartamento no esta dibujado en el mapa (por ejemplo uno que se
     * agrego desde Administracion), para que la ruta de los demas se siga pudiendo calcular.
     */
    public Ruta rutaPreparacion(ListaDoble<String> codigos) {
        if (codigos == null) {
            throw new IllegalArgumentException("La lista de codigos no puede ser nula");
        }
        NodoMapa recepcion = buscarRecepcion();
        if (recepcion == null) {
            throw new IllegalArgumentException("El mapa no tiene recepcion");
        }

        ListaDoble<NodoMapa> recorrido = new ListaDoble<>();
        recorrido.agregarAlFinal(recepcion);
        double total = 0;
        NodoMapa actual = recepcion;

        for (String codigo : codigos) {
            NodoMapa destino = buscarApartamento(codigo);
            if (destino == null) {
                continue;                   // no esta en el mapa: no se puede llegar caminando
            }
            Ruta tramo = rutaMasCorta(actual.getId(), destino.getId());
            if (tramo == null) {
                continue;
            }
            boolean primero = true;
            for (NodoMapa nodo : tramo.getNodos()) {
                if (primero) {
                    primero = false;        // el primero es donde ya estamos
                } else {
                    recorrido.agregarAlFinal(nodo);
                }
            }
            total += tramo.getDistancia();
            actual = destino;
        }
        return new Ruta(recorrido, total);
    }

    // ---------- recorridos ----------

    /** Recorrido por niveles desde un nodo (usa la Cola). */
    public ListaDoble<NodoMapa> recorridoBFS(String origen) {
        return bfs(exigirNodo(origen));
    }

    /** Recorrido en profundidad desde un nodo (recursivo). */
    public ListaDoble<NodoMapa> recorridoDFS(String origen) {
        ListaDoble<NodoMapa> orden = new ListaDoble<>();
        dfs(exigirNodo(origen), new Conjunto<NodoMapa>(), orden);
        return orden;
    }

    private void dfs(NodoMapa actual, Conjunto<NodoMapa> visitados, ListaDoble<NodoMapa> orden) {
        // CASO BASE: el nodo ya fue visitado
        if (visitados.contiene(actual)) {
            return;
        }
        // CASO RECURSIVO: marcar el nodo y visitar a sus vecinos
        visitados.agregar(actual);
        orden.agregarAlFinal(actual);
        for (NodoMapa vecino : grafo.vecinos(actual)) {
            dfs(vecino, visitados, orden);
        }
    }

    private ListaDoble<NodoMapa> bfs(NodoMapa inicio) {
        ListaDoble<NodoMapa> orden = new ListaDoble<>();
        Conjunto<NodoMapa> visitados = new Conjunto<>();
        Cola<NodoMapa> cola = new Cola<>();
        visitados.agregar(inicio);
        cola.encolar(inicio);
        while (!cola.estaVacia()) {
            NodoMapa actual = cola.desencolar();
            orden.agregarAlFinal(actual);
            for (NodoMapa vecino : grafo.vecinos(actual)) {
                if (visitados.agregar(vecino)) {   // true solo si era nuevo
                    cola.encolar(vecino);
                }
            }
        }
        return orden;
    }

    // ---------- analisis del grafo ----------

    /** Apartamentos a los que ya no se llega desde el origen (por pasillos cerrados). */
    public ListaDoble<NodoMapa> inalcanzablesDesde(String origen) {
        ListaDoble<NodoMapa> alcanzados = bfs(exigirNodo(origen));
        ListaDoble<NodoMapa> inalcanzables = new ListaDoble<>();
        for (NodoMapa nodo : grafo) {
            if (nodo.getTipo() == TipoNodo.APARTAMENTO && !alcanzados.contiene(nodo)) {
                inalcanzables.agregarAlFinal(nodo);
            }
        }
        return inalcanzables;
    }

    /** Dice si desde recepcion se llega a todo el edificio. */
    public boolean esConexo() {
        NodoMapa recepcion = buscarRecepcion();
        if (recepcion == null) {
            return false;
        }
        return bfs(recepcion).tamanio() == grafo.cantidadVertices();
    }

    /** Agrupa los apartamentos por bloque, usando un recorrido BFS desde recepcion. */
    public ListaDoble<GrupoBloque> agruparPorBloque() {
        ListaDoble<GrupoBloque> grupos = new ListaDoble<>();
        ListaDoble<NodoMapa> alcanzados = new ListaDoble<>();
        NodoMapa recepcion = buscarRecepcion();
        if (recepcion != null) {
            alcanzados = bfs(recepcion);
        }
        for (NodoMapa nodo : alcanzados) {
            if (nodo.getTipo() == TipoNodo.APARTAMENTO) {
                agregarAGrupo(grupos, nodo);
            }
        }
        // Los que ya no se alcanzan tambien pertenecen a su bloque
        for (NodoMapa nodo : grafo) {
            if (nodo.getTipo() == TipoNodo.APARTAMENTO && !alcanzados.contiene(nodo)) {
                agregarAGrupo(grupos, nodo);
            }
        }
        return grupos;
    }

    // ---------- metodos privados ----------

    private void agregarAGrupo(ListaDoble<GrupoBloque> grupos, NodoMapa nodo) {
        for (GrupoBloque grupo : grupos) {
            if (Objects.equals(grupo.getBloque(), nodo.getBloque())) {
                grupo.agregar(nodo);
                return;
            }
        }
        GrupoBloque nuevo = new GrupoBloque(nodo.getBloque());
        nuevo.agregar(nodo);
        grupos.agregarAlFinal(nuevo);
    }

    private NodoMapa exigirNodo(String id) {
        NodoMapa nodo = buscarNodo(id);
        if (nodo == null) {
            throw new IllegalArgumentException("No existe el nodo " + id);
        }
        return nodo;
    }

    private NodoMapa buscarRecepcion() {
        for (NodoMapa nodo : grafo) {
            if (nodo.getTipo() == TipoNodo.RECEPCION) {
                return nodo;
            }
        }
        return null;
    }

    private NodoMapa buscarApartamento(String codigo) {
        for (NodoMapa nodo : grafo) {
            if (nodo.getTipo() == TipoNodo.APARTAMENTO && nodo.getCodigoApartamento().equals(codigo)) {
                return nodo;
            }
        }
        return null;
    }
}