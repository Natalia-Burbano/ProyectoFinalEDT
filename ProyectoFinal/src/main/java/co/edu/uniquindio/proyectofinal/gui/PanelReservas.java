package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.comparadores.ComparadoresApartamento;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.ConflictoCanal;
import co.edu.uniquindio.proyectofinal.modelo.Cotizacion;
import co.edu.uniquindio.proyectofinal.modelo.NocheLiquidada;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.modelo.SolicitudExterna;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Reloj;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Pantalla de reservas: disponibilidad con orden, cotizacion noche por noche, crear reservas
 * (portal o directa), pagar anticipo (confirma la reserva), cancelar viendo la devolucion,
 * grupos grandes y canal externo.
 * Su archivo es PanelReservas.fxml (se abre y se edita en SceneBuilder).
 */
public class PanelReservas implements Pantalla {

    private static final String[] ORDENES = {"Precio", "Capacidad", "Dormitorios", "Numero de reservas"};

    private SistemaAlojamiento sistema;
    private ListaDoble<Ocupante> ocupantes = new ListaDoble<>();
    private VentanaPrincipal ventana;

    // --- disponibilidad y reserva
    @FXML private DatePicker dpEntrada;
    @FXML private DatePicker dpSalida;
    @FXML private TextField tfPersonas;
    @FXML private ComboBox<String> cbOrden;
    @FXML private TableView<Apartamento> tablaDisponibles;
    @FXML private TableColumn<Apartamento, String> colDCodigo;
    @FXML private TableColumn<Apartamento, String> colDNombre;
    @FXML private TableColumn<Apartamento, String> colDBloque;
    @FXML private TableColumn<Apartamento, String> colDPiso;
    @FXML private TableColumn<Apartamento, String> colDDorm;
    @FXML private TableColumn<Apartamento, String> colDCap;
    @FXML private TableColumn<Apartamento, String> colDTarifa;
    @FXML private TextField tfDocumento;
    @FXML private TextField tfHoraLlegada;
    @FXML private ComboBox<String> cbCanal;
    @FXML private TextField tfOcNombre;
    @FXML private DatePicker dpOcNacimiento;
    @FXML private TableView<Ocupante> tablaOcupantes;
    @FXML private TableColumn<Ocupante, String> colONombre;
    @FXML private TableColumn<Ocupante, String> colONacimiento;
    @FXML private TextArea taCotizacion;

    // --- reservas
    @FXML private TextField tfFiltroDocumento;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, String> colRCodigo;
    @FXML private TableColumn<Reserva, String> colRHuesped;
    @FXML private TableColumn<Reserva, String> colRApto;
    @FXML private TableColumn<Reserva, String> colREntrada;
    @FXML private TableColumn<Reserva, String> colRSalida;
    @FXML private TableColumn<Reserva, String> colREstado;
    @FXML private TableColumn<Reserva, String> colRCanal;
    @FXML private TableColumn<Reserva, String> colRTotal;
    @FXML private TableColumn<Reserva, String> colRSaldo;
    @FXML private TextField tfAnticipo;
    @FXML private Label lblMensaje;

    // --- grupos grandes
    @FXML private DatePicker dpGrupoEntrada;
    @FXML private DatePicker dpGrupoSalida;
    @FXML private TextField tfGrupoPersonas;
    @FXML private TextArea taGrupo;

    // --- canal externo
    @FXML private TableView<SolicitudExterna> tablaSolicitudes;
    @FXML private TableColumn<SolicitudExterna, String> colSPlataforma;
    @FXML private TableColumn<SolicitudExterna, String> colSHuesped;
    @FXML private TableColumn<SolicitudExterna, String> colSApto;
    @FXML private TableColumn<SolicitudExterna, String> colSEntrada;
    @FXML private TableColumn<SolicitudExterna, String> colSSalida;
    @FXML private TableView<ConflictoCanal> tablaConflictos;
    @FXML private TableColumn<ConflictoCanal, String> colCPlataforma;
    @FXML private TableColumn<ConflictoCanal, String> colCApto;
    @FXML private TableColumn<ConflictoCanal, String> colCMotivo;
    @FXML private TableColumn<ConflictoCanal, String> colCFecha;
    @FXML private Label lblResultado;
    @FXML private TextField tfExtDocumento;
    @FXML private TextField tfExtApto;
    @FXML private DatePicker dpExtEntrada;
    @FXML private DatePicker dpExtSalida;

    /** Lo llama VentanaPrincipal despues de cargar el .fxml. */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        this.ventana = ventana;
        refrescar();
    }

    /** JavaFX lo llama solo, al terminar de cargar el .fxml. */
    @FXML
    private void initialize() {
        cbOrden.getItems().addAll(ORDENES);
        cbOrden.setValue(ORDENES[0]);
        cbCanal.getItems().addAll("Portal", "Directa");
        cbCanal.setValue("Portal");
        tfPersonas.setText("2");
        tfGrupoPersonas.setText("10");

        AyudaTablas.columna(colDCodigo, a -> a.getCodigo());
        AyudaTablas.columna(colDNombre, a -> a.getNombre());
        AyudaTablas.columna(colDBloque, a -> a.getBloque());
        AyudaTablas.columna(colDPiso, a -> String.valueOf(a.getPiso()));
        AyudaTablas.columna(colDDorm, a -> String.valueOf(a.getDormitorios()));
        AyudaTablas.columna(colDCap, a -> String.valueOf(a.getCapacidad()));
        AyudaTablas.columna(colDTarifa, a -> AyudaTablas.dinero(a.getTarifaBaja()));
        AyudaTablas.columna(colONombre, o -> o.getNombre());
        AyudaTablas.columna(colONacimiento, o -> String.valueOf(o.getFechaNacimiento()));

        AyudaTablas.columna(colRCodigo, r -> r.getCodigo());
        AyudaTablas.columna(colRHuesped, r -> r.getTitular().getNombre());
        AyudaTablas.columna(colRApto, r -> r.getApartamento().getCodigo());
        AyudaTablas.columna(colREntrada, r -> String.valueOf(r.getEstancia().getEntrada()));
        AyudaTablas.columna(colRSalida, r -> String.valueOf(r.getEstancia().getSalida()));
        AyudaTablas.columna(colREstado, r -> r.getEstado().getTexto());
        AyudaTablas.columna(colRCanal, r -> String.valueOf(r.getCanal()));
        AyudaTablas.columna(colRTotal, r -> AyudaTablas.dinero(r.getValorTotal()));
        AyudaTablas.columna(colRSaldo, r -> saldoTexto(r));

        AyudaTablas.columna(colSPlataforma, s -> s.getPlataforma());
        AyudaTablas.columna(colSHuesped, s -> s.getDocumentoHuesped());
        AyudaTablas.columna(colSApto, s -> s.getCodigoApartamento());
        AyudaTablas.columna(colSEntrada, s -> String.valueOf(s.getEntrada()));
        AyudaTablas.columna(colSSalida, s -> String.valueOf(s.getSalida()));
        AyudaTablas.columna(colCPlataforma, c -> c.getSolicitud().getPlataforma());
        AyudaTablas.columna(colCApto, c -> c.getSolicitud().getCodigoApartamento());
        AyudaTablas.columna(colCMotivo, c -> c.getMotivo());
        AyudaTablas.columna(colCFecha, c -> String.valueOf(c.getFecha()));
    }

    /** Vuelve a mostrar los datos al dia (la llama VentanaPrincipal despues de cada operacion). */
    @Override
    public void refrescar() {
        if (sistema == null) {
            return;
        }
        tablaOcupantes.setItems(AyudaTablas.aObservable(ocupantes));
        cargarReservas();
        tablaSolicitudes.setItems(AyudaTablas.aObservable(sistema.canalExterno().pendientes()));
        tablaConflictos.setItems(AyudaTablas.aObservable(sistema.canalExterno().conflictos()));
    }

    // ------------------------------------------------------------------ disponibilidad y reserva

    @FXML
    private void buscarDisponibles() {
        try {
            int personas = leerEntero(tfPersonas.getText(), "personas");
            ListaDoble<Apartamento> libres = sistema.buscarDisponibles(entrada(), salida(), personas, ordenElegido());
            tablaDisponibles.setItems(AyudaTablas.aObservable(libres));
            lblMensaje.setText(libres.tamanio() + " apartamento(s) disponible(s)");
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void agregarOcupante() {
        try {
            if (dpOcNacimiento.getValue() == null) {
                throw new IllegalArgumentException("Escoja la fecha de nacimiento del ocupante");
            }
            ocupantes.agregarAlFinal(new Ocupante(tfOcNombre.getText(), dpOcNacimiento.getValue()));
            tfOcNombre.clear();
            dpOcNacimiento.setValue(null);
            tablaOcupantes.setItems(AyudaTablas.aObservable(ocupantes));
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void quitarOcupante() {
        Ocupante seleccionado = tablaOcupantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AyudaTablas.error("Escoja un ocupante de la tabla");
            return;
        }
        ocupantes.eliminar(seleccionado);
        tablaOcupantes.setItems(AyudaTablas.aObservable(ocupantes));
    }

    @FXML
    private void cotizar() {
        try {
            ListaDoble<Ocupante> todos = sistema.ocupantesConTitular(tfDocumento.getText(), ocupantes);
            Cotizacion cotizacion = sistema.cotizar(codigoSeleccionado(), entrada(), salida(), todos);
            StringBuilder texto = new StringBuilder();
            texto.append("Personas: ").append(todos.tamanio()).append("\n");
            for (NocheLiquidada noche : cotizacion.getDesglose()) {
                texto.append(noche.getFecha()).append("  ").append(noche.getTemporada())
                        .append("  ").append(noche.getPersonasQuePagan()).append(" x ")
                        .append(AyudaTablas.dinero(noche.getTarifaPorPersona()))
                        .append(" = ").append(AyudaTablas.dinero(noche.getValor())).append("\n");
            }
            texto.append("TOTAL: ").append(AyudaTablas.dinero(cotizacion.getTotal())).append("\n");
            texto.append("Anticipo del 30 %: ").append(AyudaTablas.dinero(cotizacion.getAnticipo()));
            taCotizacion.setText(texto.toString());
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void reservar() {
        try {
            String codigo = codigoSeleccionado();
            LocalTime hora = leerHora(tfHoraLlegada.getText());
            Reserva reserva;
            if ("Portal".equals(cbCanal.getValue())) {
                reserva = sistema.reservarPortal(tfDocumento.getText(), codigo, entrada(), salida(), ocupantes, hora);
                AyudaTablas.info("Reserva creada", "Codigo " + reserva.getCodigo() + ". Queda Pendiente: pague el anticipo de "
                        + AyudaTablas.dinero(sistema.anticipoRequerido(reserva.getCodigo()))
                        + " antes de " + reserva.getVenceEn() + " o se cancelara sola.");
            } else {
                reserva = sistema.reservarDirecta(tfDocumento.getText(), codigo, entrada(), salida(), ocupantes, hora);
                AyudaTablas.info("Reserva creada", "Codigo " + reserva.getCodigo() + ". Queda Confirmada.");
            }
            ocupantes = new ListaDoble<>();
            taCotizacion.clear();
            buscarDisponibles();
            refrescar();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------ reservas

    @FXML
    private void cargarReservas() {
        if (sistema == null) {
            return;
        }
        try {
            String documento = tfFiltroDocumento.getText();
            ListaDoble<Reserva> lista = (documento == null || documento.isBlank())
                    ? sistema.todasLasReservas()
                    : sistema.reservasDe(documento.trim());
            tablaReservas.setItems(AyudaTablas.aObservable(lista));
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void pagarAnticipo() {
        try {
            Reserva reserva = reservaSeleccionada();
            long valor = tfAnticipo.getText().isBlank()
                    ? sistema.anticipoRequerido(reserva.getCodigo())
                    : leerLong(tfAnticipo.getText(), "anticipo");
            sistema.pagarAnticipo(reserva.getCodigo(), valor);
            lblMensaje.setText("Anticipo pagado: la reserva " + reserva.getCodigo() + " quedo Confirmada");
            tfAnticipo.clear();
            refrescar();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void verDevolucion() {
        try {
            Reserva reserva = reservaSeleccionada();
            AyudaTablas.info("Devolucion", "Si cancela hoy la reserva " + reserva.getCodigo() + " se le devolverian "
                    + AyudaTablas.dinero(sistema.consultarDevolucion(reserva.getCodigo())));
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        try {
            Reserva reserva = reservaSeleccionada();
            long devolucion = sistema.consultarDevolucion(reserva.getCodigo());
            if (AyudaTablas.confirmar("Se le devolverian " + AyudaTablas.dinero(devolucion)
                    + ". Desea cancelar la reserva " + reserva.getCodigo() + "?")) {
                sistema.cancelarReserva(reserva.getCodigo());
                lblMensaje.setText("Reserva " + reserva.getCodigo() + " cancelada");
                refrescar();
            }
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------ grupos grandes

    @FXML
    private void asignarGrupo() {
        try {
            if (dpGrupoEntrada.getValue() == null || dpGrupoSalida.getValue() == null) {
                throw new IllegalArgumentException("Escoja las fechas del grupo");
            }
            int personas = leerEntero(tfGrupoPersonas.getText(), "personas");
            ListaDoble<Apartamento> combinacion = sistema.asignarGrupoGrande(
                    dpGrupoEntrada.getValue(), dpGrupoSalida.getValue(), personas);
            StringBuilder texto = new StringBuilder();
            texto.append("Se necesitan ").append(combinacion.tamanio()).append(" apartamento(s):\n");
            int capacidad = 0;
            for (Apartamento a : combinacion) {
                texto.append(" - ").append(a.getCodigo()).append(" (").append(a.getNombre())
                        .append(", capacidad ").append(a.getCapacidad()).append(")\n");
                capacidad += a.getCapacidad();
            }
            texto.append("Capacidad total: ").append(capacidad).append(" para ").append(personas).append(" personas");
            taGrupo.setText(texto.toString());
        } catch (RuntimeException e) {
            taGrupo.clear();
            AyudaTablas.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------ canal externo

    @FXML
    private void procesarSiguiente() {
        try {
            lblResultado.setText(sistema.procesarSiguienteSolicitud());
            refrescar();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    /** Simula que la plataforma de terceros manda una solicitud (para la demostracion). */
    @FXML
    private void simularSolicitud() {
        try {
            if (dpExtEntrada.getValue() == null || dpExtSalida.getValue() == null) {
                throw new IllegalArgumentException("Escoja las fechas de la solicitud");
            }
            SolicitudExterna solicitud = new SolicitudExterna("Plataforma simulada", tfExtDocumento.getText().trim(),
                    tfExtApto.getText().trim(), dpExtEntrada.getValue(), dpExtSalida.getValue(),
                    new ListaDoble<Ocupante>(), Reserva.HORA_POR_DEFECTO, Reloj.ahora());
            sistema.recibirSolicitudExterna(solicitud);
            lblResultado.setText("Solicitud recibida: queda en la cola");
            refrescar();
        } catch (RuntimeException e) {
            AyudaTablas.error(e.getMessage());
        }
    }

    // ------------------------------------------------------------------ auxiliares

    private LocalDate entrada() {
        if (dpEntrada.getValue() == null) {
            throw new IllegalArgumentException("Escoja la fecha de entrada");
        }
        return dpEntrada.getValue();
    }

    private LocalDate salida() {
        if (dpSalida.getValue() == null) {
            throw new IllegalArgumentException("Escoja la fecha de salida");
        }
        return dpSalida.getValue();
    }

    private Comparator<Apartamento> ordenElegido() {
        String orden = cbOrden.getValue();
        if ("Capacidad".equals(orden)) {
            return ComparadoresApartamento.POR_CAPACIDAD;
        }
        if ("Dormitorios".equals(orden)) {
            return ComparadoresApartamento.POR_DORMITORIOS;
        }
        if ("Numero de reservas".equals(orden)) {
            return ComparadoresApartamento.POR_NUMERO_RESERVAS;
        }
        return ComparadoresApartamento.POR_PRECIO;
    }

    private String codigoSeleccionado() {
        Apartamento apartamento = tablaDisponibles.getSelectionModel().getSelectedItem();
        if (apartamento == null) {
            throw new IllegalArgumentException("Escoja un apartamento de la tabla de disponibilidad");
        }
        return apartamento.getCodigo();
    }

    private Reserva reservaSeleccionada() {
        Reserva reserva = tablaReservas.getSelectionModel().getSelectedItem();
        if (reserva == null) {
            throw new IllegalArgumentException("Escoja una reserva de la tabla");
        }
        return reserva;
    }

    private String saldoTexto(Reserva reserva) {
        try {
            return AyudaTablas.dinero(sistema.folios().saldo(reserva.getCodigo()));
        } catch (RuntimeException e) {
            return "-";
        }
    }

    private int leerEntero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Escriba un numero valido en " + campo);
        }
    }

    private long leerLong(String texto, String campo) {
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Escriba un numero valido en " + campo);
        }
    }

    private LocalTime leerHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return null; // la reserva usa la hora por defecto
        }
        try {
            return LocalTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La hora debe escribirse asi: 15:00");
        }
    }
}
