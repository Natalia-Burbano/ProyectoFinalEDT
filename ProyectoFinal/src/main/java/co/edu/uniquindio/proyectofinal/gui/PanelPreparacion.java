package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.Ruta;
import co.edu.uniquindio.proyectofinal.modelo.TareaPreparacion;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controlador de la pantalla de Preparacion (PanelPreparacion.fxml).
 * Muestra la cola de preparacion en orden de prioridad, los apartamentos que se estan preparando,
 * la ruta sugerida para recorrer los pendientes y el tiempo promedio de preparacion.
 */
public class PanelPreparacion implements Pantalla {

    @FXML private Label etiquetaPendientes;
    @FXML private Label etiquetaEnPreparacion;
    @FXML private Label etiquetaPromedio;
    @FXML private VBox filasCola;
    @FXML private VBox filasEnPreparacion;
    @FXML private Label etiquetaRuta;
    @FXML private Button botonIniciar;

    private SistemaAlojamiento sistema;
    private VentanaPrincipal ventana;

    /**
     * Guarda el sistema y la ventana para usarlos despues.
     */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        this.ventana = ventana;
    }

    /**
     * Vuelve a leer la cola, los que se estan preparando, la ruta y el promedio.
     */
    @Override
    public void refrescar() {
        ListaDoble<TareaPreparacion> cola = sistema.colaPreparacion();
        ListaDoble<TareaPreparacion> enPreparacion = sistema.preparacion().enPreparacion();

        etiquetaPendientes.setText(String.valueOf(cola.tamanio()));
        etiquetaEnPreparacion.setText(String.valueOf(enPreparacion.tamanio()));
        etiquetaPromedio.setText(Math.round(sistema.preparacion().tiempoPromedioMinutos()) + " min");
        botonIniciar.setDisable(cola.estaVacia());

        llenarCola(cola);
        llenarEnPreparacion(enPreparacion);
        mostrarRuta(cola);
    }

    /**
     * Boton "Iniciar siguiente": saca el primero de la cola y lo pasa a En preparacion.
     */
    @FXML
    public void iniciarSiguiente() {
        try {
            Apartamento apto = sistema.iniciarPreparacion();
            ventana.mostrarMensaje("Se empezo a preparar el apartamento " + apto.getCodigo());
            ventana.refrescarTodo();
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /**
     * Boton "Ver en el mapa": lleva a la pantalla del mapa, donde se dibuja la ruta.
     */
    @FXML
    public void verEnMapa() {
        ventana.irA("Mapa");
    }

    // ------------------ metodos privados de ayuda ------------------

    /**
     * Una fila por cada apartamento de la cola, en el orden en que se van a preparar.
     */
    private void llenarCola(ListaDoble<TareaPreparacion> cola) {
        filasCola.getChildren().clear();
        filasCola.getChildren().add(fila(true, "#", "Apartamento", "Prioridad", "Llegan", "Pendiente desde"));

        if (cola.estaVacia()) {
            filasCola.getChildren().add(textoVacio("No hay apartamentos esperando preparacion."));
            return;
        }

        int posicion = 1;
        for (TareaPreparacion tarea : cola) {
            HBox fila = fila(false, String.valueOf(posicion), tarea.getCodigoApartamento(),
                    "", Formato.hora(tarea.getHoraLlegada()), Formato.fechaHora(tarea.getPendienteDesde()));

            // en la columna de prioridad va un chip de color en vez de texto
            Label chip = new Label(tarea.isEntradaHoy() ? "1 - Entrada hoy" : "2 - Sin entrada");
            chip.getStyleClass().addAll("chip", tarea.isEntradaHoy() ? "prioridad-1" : "prioridad-2");
            HBox celdaPrioridad = new HBox(chip);
            celdaPrioridad.setPrefWidth(150);
            fila.getChildren().set(2, celdaPrioridad);

            filasCola.getChildren().add(fila);
            posicion++;
        }
    }

    /**
     * Los apartamentos que el personal esta preparando, cada uno con su boton Terminar.
     */
    private void llenarEnPreparacion(ListaDoble<TareaPreparacion> enPreparacion) {
        filasEnPreparacion.getChildren().clear();

        if (enPreparacion.estaVacia()) {
            filasEnPreparacion.getChildren().add(textoVacio("Nadie esta preparando un apartamento ahora."));
            return;
        }

        for (TareaPreparacion tarea : enPreparacion) {
            Label codigo = new Label(tarea.getCodigoApartamento());
            codigo.getStyleClass().add("fila-titulo");
            codigo.setPrefWidth(120);

            Button terminar = new Button("Terminar");
            terminar.getStyleClass().add("boton-secundario");
            terminar.setOnAction(evento -> terminar(tarea.getCodigoApartamento()));

            HBox fila = new HBox(10, codigo, terminar);
            fila.getStyleClass().add("fila");
            filasEnPreparacion.getChildren().add(fila);
        }
    }

    /**
     * Termina la preparacion de un apartamento: queda Preparado.
     */
    private void terminar(String codigo) {
        try {
            sistema.terminarPreparacion(codigo);
            ventana.mostrarMensaje("El apartamento " + codigo + " quedo preparado");
            ventana.refrescarTodo();
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /**
     * Muestra la ruta mas corta que sale de recepcion y pasa por los pendientes en el orden de la cola.
     */
    private void mostrarRuta(ListaDoble<TareaPreparacion> cola) {
        if (cola.estaVacia()) {
            etiquetaRuta.setText("No hay pendientes, no hace falta ruta.");
            return;
        }
        try {
            Ruta ruta = sistema.rutaPreparacion();
            etiquetaRuta.setText(ruta == null ? "No se encontro un camino para los pendientes." : ruta.toString());
        } catch (RuntimeException e) {
            etiquetaRuta.setText("No se pudo calcular la ruta: " + e.getMessage());
        }
    }

    /**
     * Arma una fila de la tabla con sus columnas. Si es encabezado se ve en gris y negrita.
     */
    private HBox fila(boolean encabezado, String numero, String apartamento, String prioridad,
                      String llegada, String desde) {
        HBox fila = new HBox(
                celda(numero, 40),
                celda(apartamento, 110),
                celda(prioridad, 150),
                celda(llegada, 80),
                celda(desde, 150));
        fila.getStyleClass().add(encabezado ? "fila-encabezado" : "fila");
        return fila;
    }

    private Label celda(String texto, double ancho) {
        Label celda = new Label(texto);
        celda.setPrefWidth(ancho);
        return celda;
    }

    private Label textoVacio(String texto) {
        Label vacio = new Label(texto);
        vacio.getStyleClass().add("texto-suave");
        return vacio;
    }
}
