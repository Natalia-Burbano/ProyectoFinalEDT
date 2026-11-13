package co.edu.uniquindio.proyectofinal.app;

import co.edu.uniquindio.proyectofinal.gui.VentanaPrincipal;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicacion.
 * Crea el sistema y abre la ventana principal (hecha en SceneBuilder).
 */
public class App extends Application {

    /** Ruta de la carpeta donde estan los .fxml y la hoja de estilos. */
    public static final String RUTA_GUI = "/co/edu/uniquindio/proyectofinal/gui/";

    private static SistemaAlojamiento sistema;

    /**
     * JavaFX llama a este metodo cuando la aplicacion arranca.
     */
    @Override
    public void start(Stage escenario) throws Exception {
        FXMLLoader cargador = new FXMLLoader(App.class.getResource(RUTA_GUI + "VentanaPrincipal.fxml"));
        Parent raiz = cargador.load();

        // el controlador de la ventana recibe el sistema para poder mostrar los datos
        VentanaPrincipal ventana = cargador.getController();
        ventana.iniciar(sistema);

        Scene escena = new Scene(raiz, 1200, 760);
        escena.getStylesheets().add(App.class.getResource(RUTA_GUI + "estilos.css").toExternalForm());

        escenario.setTitle("SGA - Aparta-Suites UQ");
        escenario.setMinWidth(1000);
        escenario.setMinHeight(640);
        escenario.setScene(escena);
        escenario.show();
    }

    /**
     * Crea el sistema de alojamiento y lanza la ventana.
     */
    public static void main(String[] args) {
        sistema = new SistemaAlojamiento();
        launch(args);
    }
}
