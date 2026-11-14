package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.Empleado;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.PeriodoTemporada;
import co.edu.uniquindio.proyectofinal.modelo.Rol;
import co.edu.uniquindio.proyectofinal.modelo.Temporada;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controlador de la pantalla de Administracion (PanelAdministracion.fxml).
 * Tiene una pestania para empleados, otra para apartamentos y otra para huespedes
 * (agregar, buscar, eliminar, altura del arbol y sus recorridos inorden, preorden y postorden),
 * y una pestania para definir temporadas y cambiar tarifas.
 */
public class PanelAdministracion implements Pantalla {

    // ---------- empleados ----------
    @FXML private TextField empDocumento;
    @FXML private TextField empNombre;
    @FXML private ComboBox<String> empRol;
    @FXML private TextField empTelefono;
    @FXML private TextField empBuscar;
    @FXML private Label empArbol;
    @FXML private Label empRecorridos;
    @FXML private VBox empListado;

    // ---------- apartamentos ----------
    @FXML private TextField aptCodigo;
    @FXML private TextField aptNombre;
    @FXML private TextField aptBloque;
    @FXML private TextField aptPiso;
    @FXML private TextField aptDormitorios;
    @FXML private TextField aptCapacidad;
    @FXML private TextField aptCaracteristicas;
    @FXML private TextField aptTarifaBaja;
    @FXML private TextField aptTarifaMedia;
    @FXML private TextField aptTarifaAlta;
    @FXML private TextField aptBuscar;
    @FXML private TextField aptMotivo;
    @FXML private Label aptArbol;
    @FXML private Label aptRecorridos;
    @FXML private VBox aptListado;

    // ---------- huespedes ----------
    @FXML private TextField hueDocumento;
    @FXML private TextField hueNombre;
    @FXML private DatePicker hueNacimiento;
    @FXML private TextField hueTelefono;
    @FXML private TextField hueCorreo;
    @FXML private TextField hueBuscar;
    @FXML private Label hueArbol;
    @FXML private Label hueRecorridos;
    @FXML private VBox hueListado;

    // ---------- temporadas y tarifas ----------
    @FXML private ComboBox<String> tempTipo;
    @FXML private DatePicker tempDesde;
    @FXML private DatePicker tempHasta;
    @FXML private VBox tempListado;
    @FXML private ComboBox<String> tarApartamento;
    @FXML private ComboBox<String> tarTemporada;
    @FXML private TextField tarValor;
    @FXML private Label tarActuales;

    private SistemaAlojamiento sistema;
    private VentanaPrincipal ventana;

    /**
     * Guarda el sistema y la ventana, y llena los combos que no cambian.
     */
    @Override
    public void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana) {
        this.sistema = sistema;
        this.ventana = ventana;
        for (Rol rol : Rol.values()) {
            empRol.getItems().add(rol.name());
        }
        for (Temporada t : Temporada.values()) {
            tempTipo.getItems().add(t.name());
            tarTemporada.getItems().add(t.name());
        }
        tarApartamento.setOnAction(evento -> mostrarTarifasActuales());
    }

    /**
     * Vuelve a llenar las listas, la altura de los arboles y los combos.
     */
    @Override
    public void refrescar() {
        // empleados
        empListado.getChildren().clear();
        for (Empleado e : sistema.empleados()) {
            empListado.getChildren().add(renglon(e.getDocumento() + "  -  " + e.getNombre() + "  -  " + e.getRol()));
        }
        empArbol.setText(datosArbol(sistema.registroEmpleados().cantidad(), sistema.registroEmpleados().altura()));

        // apartamentos
        aptListado.getChildren().clear();
        for (Apartamento a : sistema.apartamentos()) {
            aptListado.getChildren().add(renglon(a.getCodigo() + "  -  " + a.getNombre() + "  -  "
                    + a.getEstado().getTexto() + "  -  hasta " + a.getCapacidad() + " personas"));
        }
        aptArbol.setText(datosArbol(sistema.catalogo().cantidad(), sistema.catalogo().altura()));

        // huespedes
        hueListado.getChildren().clear();
        for (Huesped h : sistema.huespedes()) {
            hueListado.getChildren().add(renglon(h.getDocumento() + "  -  " + h.getNombre() + "  -  " + h.getCorreo()));
        }
        hueArbol.setText(datosArbol(sistema.registroHuespedes().cantidad(), sistema.registroHuespedes().altura()));

        // temporadas y tarifas
        llenarTemporadas();
        String escogido = tarApartamento.getValue();
        tarApartamento.getItems().clear();
        for (Apartamento a : sistema.apartamentos()) {
            tarApartamento.getItems().add(a.getCodigo());
        }
        tarApartamento.setValue(escogido);
        mostrarTarifasActuales();
    }

    // ================== empleados ==================

    /** Boton "Agregar": registra el empleado en el arbol. */
    @FXML
    public void agregarEmpleado() {
        try {
            if (empRol.getValue() == null) {
                throw new IllegalArgumentException("Escoge el rol del empleado.");
            }
            Empleado empleado = new Empleado(empDocumento.getText(), empNombre.getText(),
                    Rol.valueOf(empRol.getValue()), empTelefono.getText());
            sistema.registrarEmpleado(empleado); // si es recepcionista entra tambien a los turnos
            limpiar(empDocumento, empNombre, empTelefono);
            terminar("Empleado " + empleado.getNombre() + " agregado");
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /** Boton "Buscar": busca el empleado por documento en el arbol. */
    @FXML
    public void buscarEmpleado() {
        Empleado e = sistema.registroEmpleados().buscar(empBuscar.getText().trim());
        if (e == null) {
            ventana.mostrarAviso("Buscar empleado", "No hay ningun empleado con ese documento.");
        } else {
            ventana.mostrarAviso("Empleado encontrado", e.getNombre() + "\nDocumento: " + e.getDocumento()
                    + "\nRol: " + e.getRol() + "\nTelefono: " + e.getTelefono());
        }
    }

    /** Boton "Eliminar": quita el empleado del arbol (y de los turnos si es recepcionista). */
    @FXML
    public void eliminarEmpleado() {
        String documento = empBuscar.getText().trim();
        Empleado e = sistema.registroEmpleados().buscar(documento);
        if (e == null) {
            ventana.mostrarError("No hay ningun empleado con ese documento.");
            return;
        }
        if (e.getRol() == Rol.RECEPCIONISTA) {
            sistema.turnos().quitar(e); // tambien sale de la rotacion de turnos
        }
        sistema.registroEmpleados().eliminar(documento);
        empBuscar.clear();
        terminar("Empleado " + e.getNombre() + " eliminado");
    }

    /** Muestra los recorridos inorden, preorden y postorden del arbol de empleados. */
    @FXML
    public void verRecorridosEmpleados() {
        empRecorridos.setText("Inorden: " + documentos(sistema.registroEmpleados().todos())
                + "\nPreorden: " + documentos(sistema.registroEmpleados().preorden())
                + "\nPostorden: " + documentos(sistema.registroEmpleados().postorden()));
    }

    // ================== apartamentos ==================

    /** Boton "Agregar": registra el apartamento en el catalogo (arbol). */
    @FXML
    public void agregarApartamento() {
        try {
            ListaDoble<String> caracteristicas = new ListaDoble<>();
            for (String c : aptCaracteristicas.getText().split(",")) {
                if (!c.trim().isEmpty()) {
                    caracteristicas.agregarAlFinal(c.trim());
                }
            }
            Apartamento apto = new Apartamento(aptCodigo.getText(), aptNombre.getText(), aptBloque.getText(),
                    entero(aptPiso, "El piso"), entero(aptDormitorios, "Los dormitorios"),
                    entero(aptCapacidad, "La capacidad"), caracteristicas,
                    entero(aptTarifaBaja, "La tarifa baja"), entero(aptTarifaMedia, "La tarifa media"),
                    entero(aptTarifaAlta, "La tarifa alta"));
            sistema.registrarApartamento(apto);
            limpiar(aptCodigo, aptNombre, aptBloque, aptPiso, aptDormitorios, aptCapacidad,
                    aptCaracteristicas, aptTarifaBaja, aptTarifaMedia, aptTarifaAlta);
            terminar("Apartamento " + apto.getCodigo() + " agregado");
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /** Boton "Buscar": busca el apartamento por codigo en el arbol. */
    @FXML
    public void buscarApartamento() {
        Apartamento a = sistema.buscarApartamento(aptBuscar.getText().trim().toUpperCase());
        if (a == null) {
            ventana.mostrarAviso("Buscar apartamento", "No hay ningun apartamento con ese codigo.");
        } else {
            ventana.mostrarAviso("Apartamento encontrado", a.getCodigo() + " - " + a.getNombre()
                    + "\nEstado: " + a.getEstado().getTexto()
                    + "\nBloque " + a.getBloque() + ", piso " + a.getPiso()
                    + "\n" + a.getDormitorios() + " dormitorios, hasta " + a.getCapacidad() + " personas"
                    + "\nReservas activas: " + a.getReservas().activas().tamanio());
        }
    }

    /** Boton "Eliminar": solo se puede si no tiene reservas activas. */
    @FXML
    public void eliminarApartamento() {
        String codigo = aptBuscar.getText().trim().toUpperCase();
        try {
            if (sistema.catalogo().eliminar(codigo)) {
                aptBuscar.clear();
                terminar("Apartamento " + codigo + " eliminado");
            } else {
                ventana.mostrarError("No hay ningun apartamento con ese codigo.");
            }
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage()); // por ejemplo, si tiene reservas activas
        }
    }

    /** Deja el apartamento fuera de servicio con el motivo escrito. */
    @FXML
    public void ponerFueraDeServicio() {
        String codigo = aptBuscar.getText().trim().toUpperCase();
        try {
            sistema.ponerFueraDeServicio(codigo, aptMotivo.getText());
            aptMotivo.clear();
            terminar("El apartamento " + codigo + " quedo fuera de servicio");
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /** Saca el apartamento de fuera de servicio. */
    @FXML
    public void reactivarApartamento() {
        String codigo = aptBuscar.getText().trim().toUpperCase();
        try {
            sistema.reactivarApartamento(codigo); // queda pendiente de preparacion y entra a la cola
            terminar("El apartamento " + codigo + " volvio y quedo en la cola de preparacion");
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /** Muestra los recorridos inorden, preorden y postorden del catalogo. */
    @FXML
    public void verRecorridosApartamentos() {
        aptRecorridos.setText("Inorden: " + codigos(sistema.catalogo().todos())
                + "\nPreorden: " + codigos(sistema.catalogo().preorden())
                + "\nPostorden: " + codigos(sistema.catalogo().postorden()));
    }

    // ================== huespedes ==================

    /** Boton "Agregar": registra el huesped en el arbol. */
    @FXML
    public void agregarHuesped() {
        try {
            Huesped huesped = new Huesped(hueDocumento.getText(), hueNombre.getText(), hueNacimiento.getValue(),
                    hueTelefono.getText(), hueCorreo.getText());
            sistema.registrarHuesped(huesped);
            limpiar(hueDocumento, hueNombre, hueTelefono, hueCorreo);
            hueNacimiento.setValue(null);
            terminar("Huesped " + huesped.getNombre() + " agregado");
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /** Boton "Buscar": busca el huesped por documento en el arbol. */
    @FXML
    public void buscarHuesped() {
        Huesped h = sistema.buscarHuesped(hueBuscar.getText().trim());
        if (h == null) {
            ventana.mostrarAviso("Buscar huesped", "No hay ningun huesped con ese documento.");
        } else {
            ventana.mostrarAviso("Huesped encontrado", h.getNombre() + "\nDocumento: " + h.getDocumento()
                    + "\nNacimiento: " + h.getFechaNacimiento() + "\nTelefono: " + h.getTelefono()
                    + "\nCorreo: " + h.getCorreo());
        }
    }

    /** Boton "Eliminar": quita el huesped del arbol (no se deja si tiene reservas activas). */
    @FXML
    public void eliminarHuesped() {
        String documento = hueBuscar.getText().trim();
        try {
            if (sistema.eliminarHuesped(documento)) {
                hueBuscar.clear();
                terminar("Huesped " + documento + " eliminado");
            } else {
                ventana.mostrarError("No hay ningun huesped con ese documento.");
            }
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /** Muestra los recorridos inorden, preorden y postorden del arbol de huespedes. */
    @FXML
    public void verRecorridosHuespedes() {
        hueRecorridos.setText("Inorden: " + documentosHuespedes(sistema.huespedes())
                + "\nPreorden: " + documentosHuespedes(sistema.registroHuespedes().preorden())
                + "\nPostorden: " + documentosHuespedes(sistema.registroHuespedes().postorden()));
    }

    // ================== temporadas y tarifas ==================

    /** Agrega un periodo de temporada (no se puede cruzar con otro). */
    @FXML
    public void definirTemporada() {
        try {
            if (tempTipo.getValue() == null) {
                throw new IllegalArgumentException("Escoge la temporada.");
            }
            sistema.definirTemporada(Temporada.valueOf(tempTipo.getValue()), tempDesde.getValue(), tempHasta.getValue());
            tempDesde.setValue(null);
            tempHasta.setValue(null);
            terminar("Temporada definida");
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage()); // por ejemplo, si se cruza con otro periodo
        }
    }

    /** Cambia la tarifa por persona por noche del apartamento en esa temporada. */
    @FXML
    public void cambiarTarifa() {
        try {
            if (tarApartamento.getValue() == null || tarTemporada.getValue() == null) {
                throw new IllegalArgumentException("Escoge el apartamento y la temporada.");
            }
            sistema.cambiarTarifa(tarApartamento.getValue(), Temporada.valueOf(tarTemporada.getValue()),
                    entero(tarValor, "La tarifa"));
            tarValor.clear();
            terminar("Tarifa cambiada");
        } catch (RuntimeException e) {
            ventana.mostrarError(e.getMessage());
        }
    }

    /**
     * Una fila por periodo, cada una con su boton Quitar.
     */
    private void llenarTemporadas() {
        tempListado.getChildren().clear();
        ListaDoble<PeriodoTemporada> periodos = sistema.temporadas().periodos();
        if (periodos.estaVacia()) {
            tempListado.getChildren().add(renglon("No hay periodos. Todas las noches son temporada BAJA."));
            return;
        }
        for (PeriodoTemporada p : periodos) {
            Label texto = new Label(p.toString());
            texto.setPrefWidth(300);
            Button quitar = new Button("Quitar");
            quitar.getStyleClass().add("boton-secundario");
            quitar.setOnAction(evento -> {
                sistema.temporadas().quitarPeriodo(p);
                terminar("Periodo quitado: " + p);
            });
            HBox fila = new HBox(10, texto, quitar);
            fila.getStyleClass().add("fila");
            tempListado.getChildren().add(fila);
        }
    }

    private void mostrarTarifasActuales() {
        String codigo = tarApartamento.getValue();
        Apartamento a = codigo == null ? null : sistema.buscarApartamento(codigo);
        tarActuales.setText(a == null ? "Escoge un apartamento para ver sus tarifas."
                : "Tarifas de " + a.getCodigo() + " por persona por noche:  Baja $ " + a.getTarifaBaja()
                + "  -  Media $ " + a.getTarifaMedia() + "  -  Alta $ " + a.getTarifaAlta());
    }

    // ================== ayudantes ==================

    /**
     * Despues de cada cambio: mensaje abajo y todas las pantallas al dia.
     */
    private void terminar(String mensaje) {
        ventana.mostrarMensaje(mensaje);
        ventana.refrescarTodo();
    }

    private String datosArbol(int cantidad, int altura) {
        return cantidad + " guardados en el arbol  -  altura " + altura;
    }

    /**
     * Lee un numero entero de un campo. Si no es un numero da un error con el nombre del campo.
     */
    private int entero(TextField campo, String nombre) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombre + " debe ser un numero entero, sin puntos.");
        }
    }

    private void limpiar(TextField... campos) {
        for (TextField campo : campos) {
            campo.clear();
        }
    }

    private Label renglon(String texto) {
        Label renglon = new Label(texto);
        renglon.getStyleClass().add("fila");
        return renglon;
    }

    private String documentos(ListaDoble<Empleado> lista) {
        String texto = "";
        for (Empleado e : lista) {
            texto += (texto.isEmpty() ? "" : ", ") + e.getDocumento();
        }
        return texto.isEmpty() ? "(vacio)" : texto;
    }

    private String codigos(ListaDoble<Apartamento> lista) {
        String texto = "";
        for (Apartamento a : lista) {
            texto += (texto.isEmpty() ? "" : ", ") + a.getCodigo();
        }
        return texto.isEmpty() ? "(vacio)" : texto;
    }

    private String documentosHuespedes(ListaDoble<Huesped> lista) {
        String texto = "";
        for (Huesped h : lista) {
            texto += (texto.isEmpty() ? "" : ", ") + h.getDocumento();
        }
        return texto.isEmpty() ? "(vacio)" : texto;
    }
}
