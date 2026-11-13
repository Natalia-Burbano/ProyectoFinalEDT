package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * Controlador de la pantalla de Inicio (PanelInicio.fxml).
 * Muestra un cuadro por apartamento con el color de su estado:
 * verde Preparado, azul Ocupado, amarillo Pendiente o En preparacion y rojo Fuera de servicio.
 * Al hacer clic en un cuadro se ve el detalle a la derecha.
 */
public class PanelInicio implements Pantalla {

    @FXML private FlowPane tablero;
    @FXML private Label etiquetaTotal;
    @FXML private Label etiquetaPreparados;
    @FXML private Label etiquetaOcupados;
    @FXML private Label etiquetaPendientes;
    @FXML private Label etiquetaFuera;

    @FXML private VBox panelDetalle;
    @FXML private Label detalleCodigo;
    @FXML private Label detalleNombre;
    @FXML private Label detalleEstado;
    @FXML private Label detalleUbicacion;
    @FXML private Label detalleCapacidad;
    @FXML private Label detalleTarifas;
    @FXML private Label detalleCaracteristicas;
    @FXML private Label detalleReservas;
    @FXML private Label detalleMotivo;

    private SistemaAlojamiento sistema;

    /**
     * Guarda el sistema y deja oculto el detalle hasta que se escoja un apartamento.
     */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        ocultarDetalle();
    }

    /**
     * Vuelve a dibujar el tablero con los estados actuales y cuenta cuantos hay de cada estado.
     */
    @Override
    public void refrescar() {
        tablero.getChildren().clear();

        int preparados = 0;
        int ocupados = 0;
        int pendientes = 0;
        int fuera = 0;

        for (Apartamento apto : sistema.apartamentos()) {
            tablero.getChildren().add(crearTarjeta(apto));

            EstadoApartamento estado = apto.getEstado();
            if (estado == EstadoApartamento.PREPARADO) {
                preparados++;
            } else if (estado == EstadoApartamento.OCUPADO) {
                ocupados++;
            } else if (estado == EstadoApartamento.FUERA_DE_SERVICIO) {
                fuera++;
            } else {
                pendientes++; // pendiente o en preparacion
            }
        }

        if (tablero.getChildren().isEmpty()) {
            Label vacio = new Label("Todavia no hay apartamentos. Usa el boton \"Cargar escenario de prueba\".");
            vacio.getStyleClass().add("texto-suave");
            tablero.getChildren().add(vacio);
            ocultarDetalle();
        }

        etiquetaTotal.setText(String.valueOf(preparados + ocupados + pendientes + fuera));
        etiquetaPreparados.setText(String.valueOf(preparados));
        etiquetaOcupados.setText(String.valueOf(ocupados));
        etiquetaPendientes.setText(String.valueOf(pendientes));
        etiquetaFuera.setText(String.valueOf(fuera));
    }

    /**
     * Cierra el panel del detalle.
     */
    @FXML
    public void cerrarDetalle() {
        ocultarDetalle();
    }

    // ------------------ metodos privados de ayuda ------------------

    /**
     * Arma el cuadro de un apartamento: codigo, nombre y estado, con el color de su estado.
     */
    private VBox crearTarjeta(Apartamento apto) {
        Label codigo = new Label(apto.getCodigo());
        codigo.getStyleClass().add("tarjeta-codigo");

        Label nombre = new Label(apto.getNombre());
        nombre.getStyleClass().add("tarjeta-nombre");

        Label estado = new Label(apto.getEstado().getTexto());
        estado.getStyleClass().add("tarjeta-estado");

        VBox tarjeta = new VBox(4, codigo, nombre, estado);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.getStyleClass().add(claseDelEstado(apto.getEstado()));
        tarjeta.setOnMouseClicked(evento -> mostrarDetalle(apto));
        return tarjeta;
    }

    /**
     * Llena el panel de la derecha con los datos del apartamento.
     */
    private void mostrarDetalle(Apartamento apto) {
        detalleCodigo.setText(apto.getCodigo());
        detalleNombre.setText(apto.getNombre());
        detalleEstado.setText(apto.getEstado().getTexto());
        detalleEstado.getStyleClass().setAll("label", "chip", claseDelEstado(apto.getEstado()));
        detalleUbicacion.setText("Bloque " + apto.getBloque() + "  -  Piso " + apto.getPiso());
        detalleCapacidad.setText(apto.getDormitorios() + " dormitorios  -  hasta " + apto.getCapacidad() + " personas");
        detalleTarifas.setText("Baja " + Formato.pesos(apto.getTarifaBaja()) + " / persona"
                + "\nMedia " + Formato.pesos(apto.getTarifaMedia()) + " / persona"
                + "\nAlta " + Formato.pesos(apto.getTarifaAlta()) + " / persona");
        detalleCaracteristicas.setText(textoCaracteristicas(apto));
        detalleReservas.setText(apto.getReservas().activas().tamanio() + " reservas activas");

        boolean estaFuera = apto.getEstado() == EstadoApartamento.FUERA_DE_SERVICIO;
        detalleMotivo.setText(estaFuera ? "Motivo: " + apto.getMotivoFueraServicio() : "");
        detalleMotivo.setVisible(estaFuera);
        detalleMotivo.setManaged(estaFuera);

        panelDetalle.setVisible(true);
        panelDetalle.setManaged(true);
    }

    private void ocultarDetalle() {
        panelDetalle.setVisible(false);
        panelDetalle.setManaged(false);
    }

    /**
     * Nombre de la clase de estilos (en estilos.css) que le da el color a cada estado.
     */
    private String claseDelEstado(EstadoApartamento estado) {
        if (estado == EstadoApartamento.PREPARADO) {
            return "estado-preparado";
        }
        if (estado == EstadoApartamento.OCUPADO) {
            return "estado-ocupado";
        }
        if (estado == EstadoApartamento.FUERA_DE_SERVICIO) {
            return "estado-fuera";
        }
        return "estado-pendiente"; // pendiente de preparacion o en preparacion
    }

    private String textoCaracteristicas(Apartamento apto) {
        String texto = "";
        for (String c : apto.getCaracteristicas()) {
            if (!texto.isEmpty()) {
                texto += "  -  ";
            }
            texto += c;
        }
        return texto.isEmpty() ? "Sin caracteristicas" : texto;
    }
}
