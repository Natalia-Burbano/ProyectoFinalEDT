package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.GrupoBloque;
import co.edu.uniquindio.proyectofinal.modelo.NodoMapa;
import co.edu.uniquindio.proyectofinal.modelo.Ruta;
import co.edu.uniquindio.proyectofinal.modelo.TipoNodo;
import co.edu.uniquindio.proyectofinal.servicio.MapaEdificio;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

/**
 * Controlador de la pantalla del Mapa (PanelMapa.fxml).
 * Dibuja el grafo del edificio: los apartamentos son circulos del color de su estado,
 * recepcion, escaleras y aseo son cuadrados, y los pasillos son lineas con sus metros
 * (punteadas si estan cerradas). Tambien dibuja en grueso la ruta calculada con Dijkstra.
 */
public class PanelMapa implements Pantalla {

    private static final double RADIO = 22;     // tamanio de los circulos de los apartamentos
    private static final double LADO = 34;      // tamanio de los cuadrados de los puntos de servicio

    @FXML private Pane lienzo;
    @FXML private ComboBox<String> comboOrigen;
    @FXML private ComboBox<String> comboDestino;
    @FXML private ComboBox<String> comboPasillo;
    @FXML private Label etiquetaResultado;
    @FXML private Label etiquetaConexo;

    private SistemaAlojamiento sistema;
    private VentanaPrincipal ventana;

    // lo que esta resaltado en el dibujo (null o vacio si no hay nada)
    private Ruta rutaActual;
    private ListaDoble<NodoMapa> ordenRecorrido;
    private ListaDoble<NodoMapa> inalcanzables;
    private ListaDoble<GrupoBloque> grupos;

    /**
     * Guarda el sistema y la ventana, y deja el dibujo sin nada resaltado.
     */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        this.ventana = ventana;
        limpiarResaltado();
    }

    /**
     * Vuelve a llenar los combos y a dibujar el mapa con los estados actuales.
     */
    @Override
    public void refrescar() {
        llenarCombos();
        dibujar();
    }

    // ------------------ botones ------------------

    /**
     * Ruta mas corta entre el origen y el destino escogidos (Dijkstra).
     */
    @FXML
    public void calcularRuta() {
        String origen = comboOrigen.getValue();
        String destino = comboDestino.getValue();
        if (origen == null || destino == null) {
            ventana.mostrarError("Escoge el origen y el destino.");
            return;
        }
        limpiarResaltado();
        rutaActual = mapa().rutaMasCorta(origen, destino);
        etiquetaResultado.setText(rutaActual == null
                ? "No hay camino de " + origen + " a " + destino + " (puede haber pasillos cerrados)."
                : "Ruta mas corta: " + rutaActual
                + "\n\nEn el mapa la ruta va en verde oscuro y el numero de cada punto es el paso.");
        dibujar();
    }

    /**
     * Ruta que sale de recepcion y pasa por los apartamentos de la cola de preparacion.
     */
    @FXML
    public void calcularRutaPreparacion() {
        limpiarResaltado();
        try {
            rutaActual = sistema.rutaPreparacion();
            etiquetaResultado.setText(rutaActual == null
                    ? "No hay apartamentos pendientes de preparacion."
                    : "Ruta de preparacion: " + rutaActual
                    + "\n\nEn el mapa la ruta va en verde oscuro y el numero de cada punto es el paso.");
        } catch (RuntimeException e) {
            etiquetaResultado.setText("No se pudo calcular la ruta: " + e.getMessage());
        }
        dibujar();
    }

    /**
     * Cierra el pasillo escogido y muestra los apartamentos a los que ya no se llega.
     */
    @FXML
    public void cerrarPasillo() {
        cambiarPasillo(false);
    }

    /**
     * Vuelve a abrir el pasillo escogido.
     */
    @FXML
    public void abrirPasillo() {
        cambiarPasillo(true);
    }

    /**
     * Recorrido por niveles (BFS) desde el origen; en el mapa sale el numero de orden de cada punto.
     */
    @FXML
    public void recorrerBFS() {
        recorrer(true);
    }

    /**
     * Recorrido en profundidad (DFS, recursivo) desde el origen.
     */
    @FXML
    public void recorrerDFS() {
        recorrer(false);
    }

    /**
     * Muestra los apartamentos de cada bloque (armado con BFS desde recepcion).
     */
    @FXML
    public void agruparPorBloque() {
        limpiarResaltado();
        grupos = mapa().agruparPorBloque();
        String texto = "";
        for (GrupoBloque grupo : grupos) {
            texto += "Bloque " + grupo.getBloque() + " (" + grupo.cantidad() + "): ";
            String codigos = "";
            for (NodoMapa nodo : grupo.getApartamentos()) {
                codigos += (codigos.isEmpty() ? "" : ", ") + nodo.getId();
            }
            texto += codigos + "\n";
        }
        etiquetaResultado.setText(texto.isEmpty() ? "No hay apartamentos en el mapa."
                : texto.trim() + "\n\nEn el mapa cada bloque queda dentro de un recuadro de color.");
        dibujar();
    }

    /**
     * Quita la ruta, el orden y los inalcanzables del dibujo.
     */
    @FXML
    public void limpiar() {
        limpiarResaltado();
        etiquetaResultado.setText("");
        dibujar();
    }

    // ------------------ dibujo ------------------

    /**
     * Borra el lienzo y dibuja primero los pasillos y despues los puntos (asi los puntos quedan encima).
     */
    private void dibujar() {
        lienzo.getChildren().clear();
        ListaDoble<NodoMapa> nodos = mapa().nodos();

        if (nodos.estaVacia()) {
            Label vacio = new Label("El mapa esta vacio. Usa el boton \"Cargar escenario de prueba\".");
            vacio.getStyleClass().add("texto-suave");
            vacio.setLayoutX(20);
            vacio.setLayoutY(20);
            lienzo.getChildren().add(vacio);
            etiquetaConexo.setText("");
            return;
        }

        // si se agrupo por bloque, primero van los recuadros de cada bloque (quedan al fondo)
        if (grupos != null) {
            int numero = 0;
            for (GrupoBloque grupo : grupos) {
                dibujarZona(grupo, numero);
                numero++;
            }
        }

        // pasillos: se revisa cada pareja de puntos una sola vez
        for (int i = 0; i < nodos.tamanio(); i++) {
            for (int j = i + 1; j < nodos.tamanio(); j++) {
                NodoMapa a = nodos.obtener(i);
                NodoMapa b = nodos.obtener(j);
                double metros = mapa().metrosEntre(a.getId(), b.getId());
                if (metros > 0) {
                    dibujarPasillo(a, b, metros);
                }
            }
        }

        // puntos del mapa
        for (NodoMapa nodo : nodos) {
            dibujarNodo(nodo);
        }

        etiquetaConexo.setText(mapa().esConexo()
                ? "Desde recepcion se llega a todo el edificio."
                : "Hay puntos a los que no se llega desde recepcion.");
    }

    private void dibujarPasillo(NodoMapa a, NodoMapa b, double metros) {
        Line linea = new Line(a.getX(), a.getY(), b.getX(), b.getY());
        if (estaEnRuta(a, b)) {
            linea.getStyleClass().add("pasillo-ruta");
        } else if (mapa().pasilloAbierto(a.getId(), b.getId())) {
            linea.getStyleClass().add("pasillo");
        } else {
            linea.getStyleClass().add("pasillo-cerrado");
        }
        // si hay una ruta, los pasillos que no son de la ruta se ven apagados
        if (rutaActual != null && !estaEnRuta(a, b)) {
            linea.getStyleClass().add("atenuado");
        }

        // los metros van en la mitad de la linea
        Label etiqueta = new Label(Math.round(metros) + " m");
        etiqueta.getStyleClass().add("pasillo-metros");
        etiqueta.setLayoutX((a.getX() + b.getX()) / 2 - 12);
        etiqueta.setLayoutY((a.getY() + b.getY()) / 2 - 9);
        etiqueta.setMouseTransparent(true);

        lienzo.getChildren().addAll(linea, etiqueta);
    }

    private void dibujarNodo(NodoMapa nodo) {
        double x = nodo.getX();
        double y = nodo.getY();

        // numero que va dentro del punto: el paso de la ruta o el orden del recorrido (0 si no tiene)
        int numero = 0;
        if (rutaActual != null) {
            numero = posicionEn(rutaActual.getNodos(), nodo);
        } else if (ordenRecorrido != null) {
            numero = posicionEn(ordenRecorrido, nodo);
        }
        boolean apagado = (rutaActual != null && numero == 0) || contiene(inalcanzables, nodo);

        if (nodo.getTipo() == TipoNodo.APARTAMENTO) {
            Circle circulo = new Circle(x, y, RADIO);
            circulo.getStyleClass().addAll("nodo", claseDelEstado(nodo));
            marcar(circulo, numero > 0, apagado, contiene(inalcanzables, nodo));
            circulo.setOnMouseClicked(evento -> mostrarApartamento(nodo));
            lienzo.getChildren().add(circulo);
        } else {
            Rectangle cuadro = new Rectangle(x - LADO / 2, y - LADO / 2, LADO, LADO);
            cuadro.getStyleClass().addAll("nodo", "nodo-servicio");
            marcar(cuadro, numero > 0, apagado, false);
            lienzo.getChildren().add(cuadro);
        }

        // el numero va grande, centrado dentro de la figura
        if (numero > 0) {
            Label etiquetaNumero = new Label(String.valueOf(numero));
            etiquetaNumero.getStyleClass().add("nodo-numero");
            etiquetaNumero.setLayoutX(x - RADIO);
            etiquetaNumero.setLayoutY(y - 11);
            etiquetaNumero.setMouseTransparent(true);
            lienzo.getChildren().add(etiquetaNumero);
        }

        // el id del punto, centrado debajo de la figura
        Label nombre = new Label(nodo.getId());
        nombre.getStyleClass().add("nodo-texto");
        if (apagado) {
            nombre.getStyleClass().add("atenuado");
        }
        nombre.setLayoutX(x - 22);
        nombre.setLayoutY(y + RADIO + 2);
        nombre.setMouseTransparent(true);
        lienzo.getChildren().add(nombre);
    }

    /**
     * Le pone a la figura las clases de estilo segun lo que se este mostrando.
     */
    private void marcar(Node figura, boolean resaltado, boolean apagado, boolean inalcanzable) {
        if (resaltado) {
            figura.getStyleClass().add("nodo-resaltado");
        }
        if (apagado) {
            figura.getStyleClass().add("atenuado");
        }
        if (inalcanzable) {
            figura.getStyleClass().add("nodo-inalcanzable");
        }
    }

    /**
     * Recuadro de color detras de los apartamentos de un bloque, con el nombre del bloque.
     */
    private void dibujarZona(GrupoBloque grupo, int numero) {
        if (grupo.getApartamentos().estaVacia()) {
            return;
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = 0;
        double maxY = 0;
        for (NodoMapa nodo : grupo.getApartamentos()) {
            minX = Math.min(minX, nodo.getX());
            minY = Math.min(minY, nodo.getY());
            maxX = Math.max(maxX, nodo.getX());
            maxY = Math.max(maxY, nodo.getY());
        }

        double margen = 45;
        Rectangle zona = new Rectangle(minX - margen, minY - margen,
                (maxX - minX) + 2 * margen, (maxY - minY) + 2 * margen + 15);
        zona.getStyleClass().addAll("zona-bloque", numero % 2 == 0 ? "zona-uno" : "zona-dos");
        zona.setMouseTransparent(true);

        Label titulo = new Label("Bloque " + grupo.getBloque());
        titulo.getStyleClass().add("zona-titulo");
        titulo.setLayoutX(minX - margen + 10);
        titulo.setLayoutY(minY - margen + 6);
        titulo.setMouseTransparent(true);

        lienzo.getChildren().addAll(zona, titulo);
    }

    // ------------------ metodos privados de ayuda ------------------

    private void cambiarPasillo(boolean abrir) {
        String pasillo = comboPasillo.getValue();
        if (pasillo == null) {
            ventana.mostrarError("Escoge un pasillo.");
            return;
        }
        // el texto del combo es "A101 - A102 (5 m)": se sacan los dos ids
        String[] partes = pasillo.split(" ");
        String idA = partes[0];
        String idB = partes[2];

        limpiarResaltado();
        if (abrir) {
            mapa().abrirPasillo(idA, idB);
            etiquetaResultado.setText("Se abrio el pasillo " + idA + " - " + idB + ".");
        } else {
            mapa().cerrarPasillo(idA, idB);
            inalcanzables = mapa().inalcanzablesDesde("REC");
            String codigos = "";
            for (NodoMapa nodo : inalcanzables) {
                codigos += (codigos.isEmpty() ? "" : ", ") + nodo.getId();
            }
            etiquetaResultado.setText("Se cerro el pasillo " + idA + " - " + idB + ". "
                    + (codigos.isEmpty() ? "Todavia se llega a todos los apartamentos."
                    : "Ya no se llega a: " + codigos
                    + ". En el mapa se ven apagados y con borde rojo."));
        }
        dibujar();
    }

    private void recorrer(boolean porNiveles) {
        String origen = comboOrigen.getValue();
        if (origen == null) {
            ventana.mostrarError("Escoge el origen del recorrido.");
            return;
        }
        limpiarResaltado();
        ordenRecorrido = porNiveles ? mapa().recorridoBFS(origen) : mapa().recorridoDFS(origen);

        String texto = "";
        for (NodoMapa nodo : ordenRecorrido) {
            texto += (texto.isEmpty() ? "" : " -> ") + nodo.getId();
        }
        etiquetaResultado.setText((porNiveles ? "BFS" : "DFS") + " desde " + origen + ": " + texto
                + "\n\nEn el mapa el numero dentro de cada punto es el orden en que se visito.");
        dibujar();
    }

    /**
     * Al hacer clic en un apartamento se muestran sus datos.
     */
    private void mostrarApartamento(NodoMapa nodo) {
        Apartamento apto = sistema.buscarApartamento(nodo.getCodigoApartamento());
        if (apto != null) {
            etiquetaResultado.setText(apto.getCodigo() + " - " + apto.getNombre() + "\nEstado: "
                    + apto.getEstado().getTexto() + "\nBloque " + apto.getBloque() + ", piso " + apto.getPiso()
                    + ", hasta " + apto.getCapacidad() + " personas");
        }
    }

    /**
     * Llena los combos con los ids de los puntos y con los pasillos. Deja lo que ya estaba escogido.
     */
    private void llenarCombos() {
        String origen = comboOrigen.getValue();
        String destino = comboDestino.getValue();
        String pasillo = comboPasillo.getValue();

        comboOrigen.getItems().clear();
        comboDestino.getItems().clear();
        comboPasillo.getItems().clear();

        ListaDoble<NodoMapa> nodos = mapa().nodos();
        for (NodoMapa nodo : nodos) {
            comboOrigen.getItems().add(nodo.getId());
            comboDestino.getItems().add(nodo.getId());
        }
        for (int i = 0; i < nodos.tamanio(); i++) {
            for (int j = i + 1; j < nodos.tamanio(); j++) {
                String idA = nodos.obtener(i).getId();
                String idB = nodos.obtener(j).getId();
                double metros = mapa().metrosEntre(idA, idB);
                if (metros > 0) {
                    comboPasillo.getItems().add(idA + " - " + idB + " (" + Math.round(metros) + " m)");
                }
            }
        }

        comboOrigen.setValue(origen != null ? origen : (nodos.estaVacia() ? null : "REC"));
        comboDestino.setValue(destino);
        comboPasillo.setValue(pasillo);
    }

    /**
     * Dice si el pasillo entre a y b hace parte de la ruta (dos puntos seguidos de la ruta).
     */
    private boolean estaEnRuta(NodoMapa a, NodoMapa b) {
        if (rutaActual == null) {
            return false;
        }
        ListaDoble<NodoMapa> camino = rutaActual.getNodos();
        for (int i = 0; i < camino.tamanio() - 1; i++) {
            NodoMapa actual = camino.obtener(i);
            NodoMapa siguiente = camino.obtener(i + 1);
            if ((actual.equals(a) && siguiente.equals(b)) || (actual.equals(b) && siguiente.equals(a))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clase de estilos con el color del estado del apartamento (los mismos colores del Inicio).
     */
    private String claseDelEstado(NodoMapa nodo) {
        Apartamento apto = sistema.buscarApartamento(nodo.getCodigoApartamento());
        if (apto == null) {
            return "nodo-servicio";
        }
        EstadoApartamento estado = apto.getEstado();
        if (estado == EstadoApartamento.PREPARADO) {
            return "nodo-preparado";
        }
        if (estado == EstadoApartamento.OCUPADO) {
            return "nodo-ocupado";
        }
        if (estado == EstadoApartamento.FUERA_DE_SERVICIO) {
            return "nodo-fuera";
        }
        return "nodo-pendiente";
    }

    private boolean contiene(ListaDoble<NodoMapa> lista, NodoMapa nodo) {
        return lista != null && lista.contiene(nodo);
    }

    /**
     * Posicion (desde 1) del nodo en el recorrido, o 0 si no esta.
     */
    private int posicionEn(ListaDoble<NodoMapa> lista, NodoMapa nodo) {
        if (lista == null) {
            return 0;
        }
        int posicion = 1;
        for (NodoMapa n : lista) {
            if (n.equals(nodo)) {
                return posicion;
            }
            posicion++;
        }
        return 0;
    }

    private void limpiarResaltado() {
        rutaActual = null;
        ordenRecorrido = null;
        inalcanzables = null;
        grupos = null;
    }

    private MapaEdificio mapa() {
        return sistema.mapa();
    }
}
