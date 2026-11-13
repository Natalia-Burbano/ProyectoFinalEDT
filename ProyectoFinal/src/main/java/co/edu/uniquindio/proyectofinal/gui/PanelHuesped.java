package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

/**
 * Pantalla del huesped: perfil con foto opcional, sus reservas con el saldo de cada folio,
 * favoritos, historial de estancias y cancelacion viendo antes la devolucion.
 * Su archivo es PanelHuesped.fxml.
 */
public class PanelHuesped implements Pantalla {

    private SistemaAlojamiento sistema;
    private Huesped actual;
    private VentanaPrincipal ventana;

    @FXML private TextField tfDocumento;
    @FXML private TextField tfNombre;
    @FXML private DatePicker dpNacimiento;
    @FXML private TextField tfTelefono;
    @FXML private TextField tfCorreo;
    @FXML private TextField tfRutaFoto;
    @FXML private ImageView ivFoto;
    @FXML private Label lblEstado;

    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colCodigo;
    @FXML private TableColumn<Reserva, String> colApto;
    @FXML private TableColumn<Reserva, String> colEntrada;
    @FXML private TableColumn<Reserva, String> colSalida;
    @FXML private TableColumn<Reserva, String> colEstado;
    @FXML private TableColumn<Reserva, String> colTotal;
    @FXML private TableColumn<Reserva, String> colSaldo;

    @FXML private ListView<String> lvFavoritos;
    @FXML private TextField tfFavorito;

    @FXML private TableView<Reserva> tablaHistorial;
    @FXML private TableColumn<Reserva, String> colHCodigo;
    @FXML private TableColumn<Reserva, String> colHApto;
    @FXML private TableColumn<Reserva, String> colHEntrada;
    @FXML private TableColumn<Reserva, String> colHSalida;
    @FXML private TableColumn<Reserva, String> colHTotal;

    /** Lo llama VentanaPrincipal despues de cargar el .fxml. */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        this.ventana = ventana;
        refrescar();
    }

    @FXML
    private void initialize() {
        AyudaTablas.columna(colCodigo, r -> r.getCodigo());
        AyudaTablas.columna(colApto, r -> r.getApartamento().getCodigo());
        AyudaTablas.columna(colEntrada, r -> String.valueOf(r.getEstancia().getEntrada()));
        AyudaTablas.columna(colSalida, r -> String.valueOf(r.getEstancia().getSalida()));
        AyudaTablas.columna(colEstado, r -> r.getEstado().getTexto());
        AyudaTablas.columna(colTotal, r -> AyudaTablas.dinero(r.getValorTotal()));
        AyudaTablas.columna(colSaldo, r -> saldoTexto(r));
        AyudaTablas.columna(colHCodigo, r -> r.getCodigo());
        AyudaTablas.columna(colHApto, r -> r.getApartamento().getCodigo());
        AyudaTablas.columna(colHEntrada, r -> String.valueOf(r.getEstancia().getEntrada()));
        AyudaTablas.columna(colHSalida, r -> String.valueOf(r.getEstancia().getSalida()));
        AyudaTablas.columna(colHTotal, r -> AyudaTablas.dinero(r.getValorTotal()));
    }

    /** Vuelve a mostrar las reservas, favoritos e historial del huesped escogido. */
    @Override
    public void refrescar() {
        if (sistema == null) {
            return;
        }
        if (actual == null) {
            tablaReservas.getItems().clear();
            tablaHistorial.getItems().clear();
            lvFavoritos.getItems().clear();
            return;
        }
        tablaReservas.setItems(AyudaTablas.aObservable(sistema.reservasDe(actual.getDocumento())));
        tablaHistorial.setItems(AyudaTablas.aObservable(sistema.historial(actual.getDocumento())));
        lvFavoritos.setItems(AyudaTablas.aObservable(actual.getFavoritos()));
    }

    @FXML
    private void buscar() {
        try {
            Huesped encontrado = sistema.buscarHuesped(tfDocumento.getText());
            if (encontrado == null) {
                throw new IllegalArgumentException("No existe un huesped con ese documento. Llene el perfil y pulse Registrar.");
            }
            actual = encontrado;
            mostrarPerfil();
            refrescar();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void registrar() {
        try {
            if (dpNacimiento.getValue() == null) {
                throw new IllegalArgumentException("Escoja la fecha de nacimiento");
            }
            Huesped nuevo = new Huesped(tfDocumento.getText(), tfNombre.getText(), dpNacimiento.getValue(),
                    tfTelefono.getText(), tfCorreo.getText(), tfRutaFoto.getText());
            sistema.registrarHuesped(nuevo);
            actual = nuevo;
            lblEstado.setText("Huesped registrado: " + nuevo.getNombre());
            refrescar();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void guardarCambios() {
        try {
            if (actual == null) {
                throw new IllegalArgumentException("Primero busque o registre un huesped");
            }
            actual.setNombre(tfNombre.getText());
            actual.setTelefono(tfTelefono.getText());
            actual.setCorreo(tfCorreo.getText());
            actual.setRutaFoto(tfRutaFoto.getText());
            lblEstado.setText("Cambios guardados");
            mostrarPerfil();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void elegirFoto() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Escoja la fotografia");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File archivo = selector.showOpenDialog(tfDocumento.getScene().getWindow());
        if (archivo != null) {
            tfRutaFoto.setText(archivo.getAbsolutePath());
            mostrarFoto(archivo.getAbsolutePath());
        }
    }

    @FXML
    private void cancelarReserva() {
        try {
            Reserva reserva = tablaReservas.getSelectionModel().getSelectedItem();
            if (reserva == null) {
                throw new IllegalArgumentException("Escoja una reserva de la tabla");
            }
            long devolucion = sistema.consultarDevolucion(reserva.getCodigo());
            if (AyudaTablas.confirmar("Si cancela la reserva " + reserva.getCodigo() + " se le devolverian "
                    + AyudaTablas.dinero(devolucion) + ". Desea cancelarla?")) {
                sistema.cancelarReserva(reserva.getCodigo());
                lblEstado.setText("Reserva " + reserva.getCodigo() + " cancelada");
                refrescar();
            }
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void agregarFavorito() {
        try {
            exigirHuesped();
            if (!sistema.agregarFavorito(actual.getDocumento(), tfFavorito.getText())) {
                lblEstado.setText("Ese apartamento ya estaba en favoritos");
            }
            tfFavorito.clear();
            refrescar();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void quitarFavorito() {
        try {
            exigirHuesped();
            String codigo = lvFavoritos.getSelectionModel().getSelectedItem();
            if (codigo == null) {
                codigo = tfFavorito.getText();
            }
            if (!sistema.quitarFavorito(actual.getDocumento(), codigo)) {
                lblEstado.setText("Ese apartamento no estaba en favoritos");
            }
            tfFavorito.clear();
            refrescar();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    private void exigirHuesped() {
        if (actual == null) {
            throw new IllegalArgumentException("Primero busque o registre un huesped");
        }
    }

    private void mostrarPerfil() {
        tfDocumento.setText(actual.getDocumento());
        tfNombre.setText(actual.getNombre());
        dpNacimiento.setValue(actual.getFechaNacimiento());
        tfTelefono.setText(actual.getTelefono() == null ? "" : actual.getTelefono());
        tfCorreo.setText(actual.getCorreo() == null ? "" : actual.getCorreo());
        tfRutaFoto.setText(actual.getRutaFoto() == null ? "" : actual.getRutaFoto());
        mostrarFoto(actual.getRutaFoto());
    }

    private void mostrarFoto(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            ivFoto.setImage(null);
            return;
        }
        try {
            ivFoto.setImage(new Image(new File(ruta).toURI().toString()));
        } catch (RuntimeException e) {
            ivFoto.setImage(null);
        }
    }

    private String saldoTexto(Reserva reserva) {
        try {
            return AyudaTablas.dinero(sistema.folios().saldo(reserva.getCodigo()));
        } catch (RuntimeException e) {
            return "-";
        }
    }
}
