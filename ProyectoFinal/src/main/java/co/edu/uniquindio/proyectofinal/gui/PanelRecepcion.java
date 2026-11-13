package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Folio;
import co.edu.uniquindio.proyectofinal.modelo.Movimiento;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Reloj;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Controlador de la pantalla de Recepcion (PanelRecepcion.fxml).
 * Tiene el turno actual, el check-in y check-out, el folio de cada reserva (cargos, pagos y deshacer)
 * y el boton para revisar las reservas pendientes que ya vencieron.
 */
public class PanelRecepcion implements Pantalla {

    // turno
    @FXML private Label etiquetaTurno;

    // llegadas y salidas
    @FXML private TextField campoReserva;
    @FXML private VBox filasHoy;

    // folio
    @FXML private ComboBox<String> comboFolio;
    @FXML private Label etiquetaCargos;
    @FXML private Label etiquetaPagos;
    @FXML private Label etiquetaSaldo;
    @FXML private VBox filasMovimientos;
    @FXML private TextField campoConcepto;
    @FXML private TextField campoValor;

    // vencimientos
    @FXML private Label etiquetaVencimientos;

    private SistemaAlojamiento sistema;
    private VentanaPrincipal ventana;

    /**
     * Guarda el sistema y la ventana, y conecta el combo del folio.
     */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        this.ventana = ventana;
        comboFolio.setOnAction(evento -> mostrarFolio());
    }

    /**
     * Vuelve a leer el turno, las llegadas y salidas de hoy, las reservas del combo y el folio escogido.
     */
    @Override
    public void refrescar() {
        mostrarTurno();
        llenarHoy();
        llenarComboFolio();
        mostrarFolio();
    }

    // ------------------ turno ------------------

    /**
     * Boton "Pasar turno": el turno pasa al siguiente recepcionista.
     */
    @FXML
    public void pasarTurno() {
        try {
            sistema.pasarTurno();
            mostrarTurno();
            ventana.mostrarMensaje("Ahora esta de turno " + sistema.turnoActual().getNombre());
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    private void mostrarTurno() {
        try {
            etiquetaTurno.setText(sistema.turnoActual().getNombre());
        } catch (RuntimeException e) {
            etiquetaTurno.setText("Sin recepcionistas");
        }
    }

    // ------------------ llegadas y salidas ------------------

    /**
     * Boton "Check-in": solo se puede si el apartamento esta Preparado y hoy es la entrada.
     */
    @FXML
    public void hacerCheckIn() {
        String codigo = codigoEscrito();
        if (codigo == null) {
            return;
        }
        try {
            sistema.checkIn(codigo);
            ventana.mostrarMensaje("Check-in listo para la reserva " + codigo);
            campoReserva.clear();
            ventana.refrescarTodo();
        } catch (RuntimeException e) {
            ventana.mostrarError("No se pudo hacer el check-in de " + codigo + ".\n" + e.getMessage());
        }
    }

    /**
     * Boton "Check-out": la reserva se finaliza y el apartamento entra a la cola de preparacion.
     */
    @FXML
    public void hacerCheckOut() {
        String codigo = codigoEscrito();
        if (codigo == null) {
            return;
        }
        try {
            sistema.checkOut(codigo);
            ventana.mostrarMensaje("Check-out listo. El apartamento quedo en la cola de preparacion");
            campoReserva.clear();
            ventana.refrescarTodo();
        } catch (RuntimeException e) {
            ventana.mostrarError("No se pudo hacer el check-out de " + codigo + ".\n" + e.getMessage());
        }
    }

    /**
     * Lista las reservas que llegan hoy (confirmadas) y las que salen hoy (en curso).
     * Al hacer clic en una, su codigo pasa al campo de texto.
     */
    private void llenarHoy() {
        filasHoy.getChildren().clear();
        LocalDate hoy = Reloj.hoy();

        for (Reserva r : sistema.todasLasReservas()) {
            boolean llegaHoy = r.getEstado() == EstadoReserva.CONFIRMADA && r.getEstancia().getEntrada().equals(hoy);
            boolean saleHoy = r.getEstado() == EstadoReserva.EN_CURSO && r.getEstancia().getSalida().equals(hoy);
            if (llegaHoy || saleHoy) {
                Label tipo = new Label(llegaHoy ? "Llega" : "Sale");
                tipo.getStyleClass().addAll("chip", llegaHoy ? "estado-ocupado" : "estado-pendiente");
                Label texto = new Label(r.getCodigo() + "  -  " + r.getApartamento().getCodigo()
                        + "  -  " + r.getTitular().getNombre());

                HBox fila = new HBox(10, tipo, texto);
                fila.getStyleClass().addAll("fila", "fila-clic");
                fila.setOnMouseClicked(evento -> campoReserva.setText(r.getCodigo()));
                filasHoy.getChildren().add(fila);
            }
        }

        if (filasHoy.getChildren().isEmpty()) {
            filasHoy.getChildren().add(textoVacio("Hoy no hay llegadas ni salidas."));
        }
    }

    // ------------------ folio ------------------

    /**
     * Boton "Registrar cargo".
     */
    @FXML
    public void registrarCargo() {
        registrarMovimiento(true);
    }

    /**
     * Boton "Registrar pago".
     */
    @FXML
    public void registrarPago() {
        registrarMovimiento(false);
    }

    /**
     * Boton "Deshacer ultima": no borra nada, registra el movimiento contrario.
     */
    @FXML
    public void deshacerUltima() {
        String codigo = comboFolio.getValue();
        if (codigo == null) {
            ventana.mostrarError("Primero escoge una reserva.");
            return;
        }
        try {
            Movimiento correccion = sistema.deshacerUltima(codigo);
            ventana.mostrarMensaje("Se deshizo el movimiento #" + correccion.getCorrigeA()
                    + " con una correccion de " + Formato.pesos(correccion.getValor()));
            ventana.refrescarTodo();
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /**
     * Registra un cargo o un pago con lo que se escribio en concepto y valor.
     */
    private void registrarMovimiento(boolean esCargo) {
        String codigo = comboFolio.getValue();
        if (codigo == null) {
            ventana.mostrarError("Primero escoge una reserva.");
            return;
        }
        String concepto = campoConcepto.getText().trim();
        if (concepto.isEmpty()) {
            ventana.mostrarError("Escribe el concepto, por ejemplo \"Lavanderia\".");
            return;
        }

        long valor;
        try {
            valor = Long.parseLong(campoValor.getText().trim());
        } catch (NumberFormatException e) {
            ventana.mostrarError("El valor debe ser un numero sin puntos ni signos, por ejemplo 25000.");
            return;
        }

        try {
            if (esCargo) {
                sistema.registrarCargo(codigo, concepto, valor);
            } else {
                sistema.registrarPago(codigo, concepto, valor);
            }
            ventana.mostrarMensaje((esCargo ? "Cargo" : "Pago") + " de " + Formato.pesos(valor)
                    + " registrado en " + codigo);
            campoConcepto.clear();
            campoValor.clear();
            ventana.refrescarTodo();
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /**
     * Pone en el combo los codigos de todas las reservas que tienen folio.
     * Si ya habia una escogida, la deja escogida.
     */
    private void llenarComboFolio() {
        String escogida = comboFolio.getValue();
        comboFolio.getItems().clear();
        for (Folio folio : sistema.folios().todos()) {
            comboFolio.getItems().add(folio.getCodigoReserva());
        }
        if (escogida != null && comboFolio.getItems().contains(escogida)) {
            comboFolio.setValue(escogida);
        }
    }

    /**
     * Muestra los movimientos del folio escogido, con sus totales y el saldo.
     */
    private void mostrarFolio() {
        filasMovimientos.getChildren().clear();
        String codigo = comboFolio.getValue();

        if (codigo == null) {
            etiquetaCargos.setText("-");
            etiquetaPagos.setText("-");
            etiquetaSaldo.setText("-");
            filasMovimientos.getChildren().add(textoVacio("Escoge una reserva para ver su folio."));
            return;
        }

        Folio folio = sistema.folio(codigo);
        etiquetaCargos.setText(Formato.pesos(folio.totalCargos()));
        etiquetaPagos.setText(Formato.pesos(folio.totalPagos()));
        etiquetaSaldo.setText(Formato.pesos(folio.saldo()));

        filasMovimientos.getChildren().add(filaMovimiento(true, "#", "Tipo", "Concepto", "Valor", "Fecha"));
        for (Movimiento m : folio) {
            HBox fila = filaMovimiento(false, String.valueOf(m.getNumero()), m.getTipo().toString(),
                    m.getConcepto(), Formato.pesos(m.getValor()), Formato.fechaHora(m.getFechaHora()));
            if (m.isEsCorreccion()) {
                fila.getStyleClass().add("fila-correccion");
            }
            filasMovimientos.getChildren().add(fila);
        }
    }

    private HBox filaMovimiento(boolean encabezado, String numero, String tipo, String concepto,
                                String valor, String fecha) {
        HBox fila = new HBox(celda(numero, 35), celda(tipo, 70), celda(concepto, 220),
                celda(valor, 110), celda(fecha, 130));
        fila.getStyleClass().add(encabezado ? "fila-encabezado" : "fila");
        return fila;
    }

    // ------------------ vencimientos ------------------

    /**
     * Boton "Revisar vencimientos": cancela las reservas pendientes que pasaron 24 horas sin anticipo.
     */
    @FXML
    public void revisarVencimientos() {
        try {
            ListaDoble<Reserva> vencidas = sistema.revisarVencimientos();
            if (vencidas.estaVacia()) {
                etiquetaVencimientos.setText("Ninguna reserva pendiente ha vencido.");
            } else {
                String codigos = "";
                for (Reserva r : vencidas) {
                    codigos += (codigos.isEmpty() ? "" : ", ") + r.getCodigo();
                }
                etiquetaVencimientos.setText("Se cancelaron " + vencidas.tamanio() + ": " + codigos
                        + ". Sus noches quedaron libres.");
            }
            ventana.refrescarTodo();
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    // ------------------ ayudantes ------------------

    /**
     * Devuelve el codigo escrito en el campo, o null (y avisa) si esta vacio.
     */
    private String codigoEscrito() {
        String codigo = campoReserva.getText().trim().toUpperCase();
        if (codigo.isEmpty()) {
            ventana.mostrarError("Escribe el codigo de la reserva, por ejemplo R0001.");
            return null;
        }
        return codigo;
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
