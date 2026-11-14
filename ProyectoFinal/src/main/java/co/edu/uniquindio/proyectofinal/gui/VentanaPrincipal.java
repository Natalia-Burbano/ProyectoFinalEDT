package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.app.App;
import co.edu.uniquindio.proyectofinal.datos.CargadorEscenario;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.estructuras.Pila;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Reloj;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de la ventana principal (VentanaPrincipal.fxml).
 * Arriba estan el selector de rol, un boton por pantalla, el boton Atras y el boton para cargar el escenario.
 * En el centro se muestra la pantalla escogida.
 * El boton Atras usa una Pila con los nombres de las pantallas por las que se paso.
 */
public class VentanaPrincipal {

    // pantallas que ve cada rol (cada nombre corresponde al archivo Panel<Nombre>.fxml)
    // el huesped empieza en su pantalla, porque primero tiene que entrar con su documento
    private static final String[] PANTALLAS_HUESPED = {"Huesped", "Inicio", "Reservas", "Calendario"};
    private static final String[] PANTALLAS_RECEPCIONISTA = {"Inicio", "Reservas", "Recepcion", "Preparacion", "Calendario", "Mapa"};
    private static final String[] PANTALLAS_ADMINISTRADOR = {"Inicio", "Mapa", "Calendario", "Estadisticas", "Administracion"};

    @FXML private ComboBox<String> comboRol;
    @FXML private HBox barraPantallas;
    @FXML private Button botonAtras;
    @FXML private StackPane contenedor;
    @FXML private Label etiquetaFecha;
    @FXML private Label etiquetaSesion;
    @FXML private Label etiquetaMensaje;

    private SistemaAlojamiento sistema;
    private Pila<String> historial;                        // pantallas anteriores, para el boton Atras
    private final ListaDoble<PantallaCargada> cargadas;    // pantallas que ya se cargaron una vez
    private String actual;                                 // nombre de la pantalla que se esta viendo
    private Huesped huespedSesion;                         // el huesped que entro (solo en el rol Huesped)

    /** JavaFX crea el controlador con este constructor cuando carga el fxml. */
    public VentanaPrincipal() {
        historial = new Pila<>();
        cargadas = new ListaDoble<>();
        actual = null;
        huespedSesion = null;
    }

    /**
     * La llama App despues de cargar el fxml. Deja todo listo y muestra el Inicio.
     */
    public void iniciar(SistemaAlojamiento sistema) {
        this.sistema = sistema;
        comboRol.getItems().addAll("Huesped", "Recepcionista", "Administrador");
        comboRol.setValue("Recepcionista");
        // se conecta despues de poner el valor inicial, para que no se dispare al abrir la ventana
        comboRol.setOnAction(evento -> cambiarRol());
        crearBotones();
        actualizarFecha();
        actualizarSesion();
        irA("Inicio");
    }

    /**
     * Muestra otra pantalla. La que se estaba viendo se apila para poder volver con Atras.
     */
    public void irA(String nombre) {
        if (nombre.equals(actual)) {
            return;
        }
        if (actual != null) {
            historial.apilar(actual);
        }
        mostrar(nombre);
    }

    /**
     * Vuelve a la pantalla anterior: desapila su nombre y la muestra.
     * Si la pila esta vacia el boton queda desactivado.
     */
    @FXML
    public void atras() {
        if (!historial.estaVacia()) {
            mostrar(historial.desapilar());
        }
    }

    /**
     * Pide a todas las pantallas cargadas que vuelvan a leer los datos.
     * Se llama despues de cada operacion que cambia algo.
     */
    public void refrescarTodo() {
        for (PantallaCargada p : cargadas) {
            if (p.controlador != null) {
                p.controlador.refrescar();
            }
        }
        actualizarFecha();
        actualizarSesion();
    }

    /**
     * Cuando se escoge otro rol cambian los botones y se va a la primera pantalla del rol.
     * Al cambiar de rol se cierra la sesion del huesped que hubiera.
     */
    public void cambiarRol() {
        historial = new Pila<>();
        actual = null;
        huespedSesion = null;
        actualizarSesion();
        crearBotones();
        irA(pantallasDelRol()[0]);
        if (esRolHuesped()) {
            mostrarMensaje("Escriba su documento y pulse Entrar para ver sus reservas");
        } else {
            mostrarMensaje("Ahora estas viendo el sistema como " + comboRol.getValue());
        }
    }

    // ------------------ sesion del huesped ------------------

    /**
     * Dice si se esta usando el sistema con el rol Huesped.
     * En ese rol cada huesped solo ve y cambia sus propias reservas.
     */
    public boolean esRolHuesped() {
        return "Huesped".equals(comboRol.getValue());
    }

    /**
     * El huesped que entro con su documento, o null si nadie ha entrado.
     */
    public Huesped getHuespedSesion() {
        return huespedSesion;
    }

    /**
     * Deja al huesped como el que esta usando el sistema y actualiza todas las pantallas.
     */
    public void iniciarSesion(Huesped huesped) {
        huespedSesion = huesped;
        actualizarSesion();
        refrescarTodo();
        mostrarMensaje("Bienvenido(a), " + huesped.getNombre());
    }

    /**
     * El huesped sale: ya nadie esta usando el sistema como huesped.
     */
    public void cerrarSesion() {
        huespedSesion = null;
        actualizarSesion();
        refrescarTodo();
        mostrarMensaje("Sesion cerrada");
    }

    /**
     * Boton "Cargar escenario de prueba".
     */
    @FXML
    public void cargarEscenario() {
        if (!sistema.apartamentos().estaVacia()) {
            mostrarMensaje("El escenario ya estaba cargado");
            return;
        }
        try {
            // el cargador lee el escenario.txt de resources y deja el Reloj en la fecha de la demo
            CargadorEscenario.cargarDesdeRecursos(sistema);
            refrescarTodo();
            mostrarMensaje("Escenario de prueba cargado: " + sistema.apartamentos().tamanio() + " apartamentos");
        } catch (RuntimeException e) {
            mostrarError("No se pudo cargar el escenario: " + e.getMessage());
        }
    }

    /**
     * Escribe un mensaje en la barra de abajo.
     */
    public void mostrarMensaje(String texto) {
        etiquetaMensaje.setText(texto);
    }

    /**
     * Muestra una ventanita de error. La pueden usar todas las pantallas.
     */
    public void mostrarError(String texto) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Algo salio mal");
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }

    /**
     * Muestra una ventanita de aviso. La pueden usar todas las pantallas.
     */
    public void mostrarAviso(String titulo, String texto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }

    public SistemaAlojamiento getSistema() {
        return sistema;
    }

    // ------------------ metodos privados de ayuda ------------------

    /**
     * Pone en el centro la pantalla con ese nombre y la refresca.
     */
    private void mostrar(String nombre) {
        PantallaCargada pantalla = buscarOCargar(nombre);
        contenedor.getChildren().setAll(pantalla.vista);
        if (pantalla.controlador != null) {
            pantalla.controlador.refrescar();
        }
        actual = nombre;
        botonAtras.setDisable(historial.estaVacia());
        marcarBotonActivo();
    }

    /**
     * Si la pantalla ya se habia cargado la devuelve; si no, carga su fxml.
     * Si el fxml todavia no existe, muestra un aviso de "pantalla en construccion".
     */
    private PantallaCargada buscarOCargar(String nombre) {
        for (PantallaCargada p : cargadas) {
            if (p.nombre.equals(nombre)) {
                return p;
            }
        }

        PantallaCargada nueva;
        URL archivo = App.class.getResource(App.RUTA_GUI + "Panel" + nombre + ".fxml");
        if (archivo == null) {
            nueva = new PantallaCargada(nombre, pantallaEnConstruccion(nombre), null);
        } else {
            try {
                FXMLLoader cargador = new FXMLLoader(archivo);
                Parent vista = cargador.load();
                Pantalla controlador = cargador.getController();
                controlador.iniciar(sistema, this);
                nueva = new PantallaCargada(nombre, vista, controlador);
            } catch (IOException e) {
                mostrarError("No se pudo abrir la pantalla " + nombre + ": " + e.getMessage());
                nueva = new PantallaCargada(nombre, pantallaEnConstruccion(nombre), null);
            }
        }
        cargadas.agregarAlFinal(nueva);
        return nueva;
    }

    /**
     * Arma los botones de las pantallas segun el rol escogido.
     */
    private void crearBotones() {
        barraPantallas.getChildren().clear();
        barraPantallas.getChildren().add(botonAtras);

        for (String nombre : pantallasDelRol()) {
            Button boton = new Button(nombre);
            boton.getStyleClass().add("boton-nav");
            boton.setOnAction(evento -> irA(nombre));
            barraPantallas.getChildren().add(boton);
        }
    }

    /**
     * Resalta el boton de la pantalla que se esta viendo.
     */
    private void marcarBotonActivo() {
        for (Node nodo : barraPantallas.getChildren()) {
            if (nodo != botonAtras) {
                Button boton = (Button) nodo;
                boton.getStyleClass().remove("boton-nav-activo");
                if (boton.getText().equals(actual)) {
                    boton.getStyleClass().add("boton-nav-activo");
                }
            }
        }
    }

    private String[] pantallasDelRol() {
        String rol = comboRol.getValue();
        if ("Huesped".equals(rol)) {
            return PANTALLAS_HUESPED;
        }
        if ("Administrador".equals(rol)) {
            return PANTALLAS_ADMINISTRADOR;
        }
        return PANTALLAS_RECEPCIONISTA;
    }

    /**
     * Lo que se muestra mientras una pantalla todavia no esta hecha.
     */
    private Parent pantallaEnConstruccion(String nombre) {
        Label titulo = new Label(nombre);
        titulo.getStyleClass().add("titulo-pantalla");
        Label texto = new Label("Esta pantalla todavia esta en construccion.");
        texto.getStyleClass().add("texto-suave");
        VBox caja = new VBox(8, titulo, texto);
        caja.getStyleClass().add("pantalla-vacia");
        return caja;
    }

    /**
     * Arriba se muestra quien entro como huesped. En los otros roles no se muestra nada.
     */
    private void actualizarSesion() {
        if (!esRolHuesped()) {
            etiquetaSesion.setText("");
        } else if (huespedSesion == null) {
            etiquetaSesion.setText("Sin entrar");
        } else {
            etiquetaSesion.setText("Huesped: " + huespedSesion.getNombre());
        }
    }

    private void actualizarFecha() {
        etiquetaFecha.setText(Reloj.ahora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm")));
    }

    /**
     * Una pantalla que ya se cargo: su nombre, lo que se ve y su controlador.
     */
    private static class PantallaCargada {
        private final String nombre;
        private final Parent vista;
        private final Pantalla controlador; // null si la pantalla esta en construccion

        private PantallaCargada(String nombre, Parent vista, Pantalla controlador) {
            this.nombre = nombre;
            this.vista = vista;
            this.controlador = controlador;
        }
    }
}
