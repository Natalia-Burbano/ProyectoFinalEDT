package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Reloj;
import java.time.LocalDate;
import java.time.YearMonth;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

/**
 * Calendario de ocupacion: filas = apartamentos, columnas = noches del mes.
 * Ocupada en azul, bloqueada (fuera de servicio) en gris y libre en blanco.
 * Usa ocupadaLaNoche / reservaLaNoche del arbol de reservas de cada apartamento.
 * Su archivo es PanelCalendario.fxml.
 */
public class PanelCalendario implements Pantalla {

    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
            "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private static final String COLOR_OCUPADA = "#4a90d9";
    private static final String COLOR_BLOQUEADA = "#9e9e9e";
    private static final String COLOR_LIBRE = "#ffffff";
    private VentanaPrincipal ventana;
    private SistemaAlojamiento sistema;
    private YearMonth mes;

    @FXML private Label lblMes;
    @FXML private GridPane grilla;

    /** Lo llama VentanaPrincipal despues de cargar el .fxml. */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        this.ventana = ventana;
        this.mes = YearMonth.from(Reloj.hoy());
        refrescar();
    }

    @FXML
    private void initialize() {
        mes = YearMonth.from(Reloj.hoy());
    }

    @FXML
    private void mesAnterior() {
        mes = mes.minusMonths(1);
        refrescar();
    }

    @FXML
    private void mesSiguiente() {
        mes = mes.plusMonths(1);
        refrescar();
    }

    @FXML
    private void irAHoy() {
        mes = YearMonth.from(Reloj.hoy());
        refrescar();
    }

    /** Vuelve a dibujar la cuadricula del mes con los datos al dia. */
    @Override
    public void refrescar() {
        if (sistema == null) {
            return;
        }
        lblMes.setText(MESES[mes.getMonthValue() - 1] + " " + mes.getYear());
        grilla.getChildren().clear();
        int dias = mes.lengthOfMonth();

        grilla.add(etiqueta("Apartamento", "#eeeeee", true), 0, 0);
        for (int dia = 1; dia <= dias; dia++) {
            grilla.add(celda(String.valueOf(dia), "#eeeeee"), dia, 0);
        }
        int fila = 1;
        for (Apartamento apartamento : sistema.apartamentos()) {
            grilla.add(etiqueta(apartamento.getCodigo(), "#f7f7f7", true), 0, fila);
            for (int dia = 1; dia <= dias; dia++) {
                grilla.add(celdaDeNoche(apartamento, mes.atDay(dia)), dia, fila);
            }
            fila++;
        }
    }

    /** Una celda de la cuadricula: azul si hay una reserva activa esa noche, gris si esta bloqueado. */
    private Label celdaDeNoche(Apartamento apartamento, LocalDate noche) {
        Reserva reserva = apartamento.getReservas().reservaLaNoche(noche);
        if (reserva != null) {
            Label celda = celda("", COLOR_OCUPADA);
            Tooltip.install(celda, new Tooltip(reserva.getCodigo() + " - " + reserva.getTitular().getNombre()
                    + " (" + reserva.getEstado() + ")"));
            return celda;
        }
        if (apartamento.getEstado() == EstadoApartamento.FUERA_DE_SERVICIO) {
            Label celda = celda("", COLOR_BLOQUEADA);
            Tooltip.install(celda, new Tooltip("Fuera de servicio: " + apartamento.getMotivoFueraServicio()));
            return celda;
        }
        return celda("", COLOR_LIBRE);
    }

    private Label celda(String texto, String color) {
        Label celda = new Label(texto);
        celda.setMinSize(28, 24);
        celda.setAlignment(Pos.CENTER);
        celda.setStyle("-fx-background-color: " + color + "; -fx-border-color: #cccccc;");
        return celda;
    }

    private Label etiqueta(String texto, String color, boolean ancha) {
        Label etiqueta = celda(texto, color);
        if (ancha) {
            etiqueta.setMinWidth(90);
        }
        return etiqueta;
    }
}
