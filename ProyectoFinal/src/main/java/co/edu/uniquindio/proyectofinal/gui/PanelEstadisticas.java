package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.FilaReporte;
import co.edu.uniquindio.proyectofinal.servicio.Reportes;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Reloj;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Controlador de la pantalla de Estadisticas (PanelEstadisticas.fxml).
 * Muestra una tabla por cada reporte y un grafico de barras sencillo de la ocupacion por apartamento.
 * El grafico se arma con barras (Region) cuyo ancho es proporcional al porcentaje.
 */
public class PanelEstadisticas implements Pantalla {

    private static final double ANCHO_MAXIMO_BARRA = 320; // ancho de una barra al 100 %

    @FXML private DatePicker fechaDesde;
    @FXML private DatePicker fechaHasta;
    @FXML private VBox graficoOcupacion;
    @FXML private FlowPane tablasReportes;

    private SistemaAlojamiento sistema;
    private VentanaPrincipal ventana;

    /**
     * Guarda el sistema y la ventana.
     */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        this.ventana = ventana;
    }

    /**
     * Si no hay periodo escogido usa el mes de la fecha del sistema, y vuelve a calcular todo.
     */
    @Override
    public void refrescar() {
        if (fechaDesde.getValue() == null || fechaHasta.getValue() == null) {
            LocalDate hoy = Reloj.hoy();
            fechaDesde.setValue(hoy.withDayOfMonth(1));
            fechaHasta.setValue(hoy.withDayOfMonth(hoy.lengthOfMonth()));
        }
        actualizar();
    }

    /**
     * Boton "Actualizar": recalcula los reportes con el periodo escogido.
     */
    @FXML
    public void actualizar() {
        LocalDate desde = fechaDesde.getValue();
        LocalDate hasta = fechaHasta.getValue();
        Reportes reportes = new Reportes(sistema);

        try {
            dibujarGrafico(reportes.ocupacionPorApartamento(desde, hasta));

            tablasReportes.getChildren().clear();
            tablasReportes.getChildren().add(tabla("Ocupacion por temporada", "noches", reportes.ocupacionPorTemporada()));
            tablasReportes.getChildren().add(tabla("Ingresos del periodo", "pesos", reportes.ingresosPorPeriodo(desde, hasta)));
            tablasReportes.getChildren().add(tabla("Reservas por canal", "", reportes.reservasPorCanal()));
            tablasReportes.getChildren().add(tabla("Conflictos del canal externo", "", reportes.conflictosDeCanal()));
            tablasReportes.getChildren().add(tabla("Preparacion", "", reportes.tiempoPromedioPreparacion()));
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    // ------------------ metodos privados de ayuda ------------------

    /**
     * Una barra por apartamento: el codigo, la barra y el porcentaje.
     */
    private void dibujarGrafico(ListaDoble<FilaReporte> filas) {
        graficoOcupacion.getChildren().clear();

        if (filas.estaVacia()) {
            graficoOcupacion.getChildren().add(textoVacio("No hay apartamentos para mostrar."));
            return;
        }

        for (FilaReporte fila : filas) {
            Label codigo = new Label(fila.getEtiqueta());
            codigo.setPrefWidth(70);

            Region barra = new Region();
            barra.getStyleClass().add("barra");
            double porcentaje = Math.max(0, Math.min(100, fila.getValor()));
            barra.setPrefWidth(Math.max(3, ANCHO_MAXIMO_BARRA * porcentaje / 100));
            barra.setPrefHeight(16);

            // la barra va dentro de un fondo gris del ancho completo, para comparar facil
            HBox fondo = new HBox(barra);
            fondo.getStyleClass().add("barra-fondo");
            fondo.setPrefWidth(ANCHO_MAXIMO_BARRA);

            Label valor = new Label(fila.getValorTexto() + " %");
            valor.getStyleClass().add("barra-valor");

            HBox renglon = new HBox(10, codigo, fondo, valor);
            renglon.getStyleClass().add("barra-renglon");
            graficoOcupacion.getChildren().add(renglon);
        }
    }

    /**
     * Arma la tarjeta de un reporte: un titulo y una fila por cada dato (etiqueta y valor).
     */
    private VBox tabla(String titulo, String unidad, ListaDoble<FilaReporte> filas) {
        Label etiquetaTitulo = new Label(titulo);
        etiquetaTitulo.getStyleClass().add("subtitulo");
        VBox tarjeta = new VBox(8, etiquetaTitulo);
        tarjeta.getStyleClass().add("tarjeta-reporte");

        if (filas.estaVacia()) {
            tarjeta.getChildren().add(textoVacio("Sin datos."));
            return tarjeta;
        }

        for (FilaReporte fila : filas) {
            Label etiqueta = new Label(fila.getEtiqueta());
            etiqueta.setPrefWidth(170);
            etiqueta.setWrapText(true);

            String texto = "pesos".equals(unidad) ? "$ " + fila.getValorTexto() : fila.getValorTexto() + " " + unidad;
            Label valor = new Label(texto.trim());
            valor.getStyleClass().add("reporte-valor");

            HBox renglon = new HBox(10, etiqueta, valor);
            renglon.getStyleClass().add("reporte-renglon");
            tarjeta.getChildren().add(renglon);
        }
        return tarjeta;
    }

    private Label textoVacio(String texto) {
        Label vacio = new Label(texto);
        vacio.getStyleClass().add("texto-suave");
        return vacio;
    }
}
