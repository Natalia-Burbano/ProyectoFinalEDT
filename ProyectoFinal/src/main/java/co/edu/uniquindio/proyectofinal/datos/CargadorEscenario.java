package co.edu.uniquindio.proyectofinal.datos;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.Canal;
import co.edu.uniquindio.proyectofinal.modelo.Empleado;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.NodoMapa;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Rol;
import co.edu.uniquindio.proyectofinal.modelo.SolicitudExterna;
import co.edu.uniquindio.proyectofinal.modelo.Temporada;
import co.edu.uniquindio.proyectofinal.modelo.TipoNodo;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Reloj;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Carga el escenario de prueba desde un archivo plano y lo registra en el sistema. */
public class CargadorEscenario {

    /** Fecha y hora que se fija en el Reloj al cargar desde recursos (debe ser la de la demo). */
    private static final LocalDateTime FECHA_DEMO = LocalDateTime.of(2026, 11, 20, 8, 0);

    private static final String RECURSO = "/escenario.txt";

    private CargadorEscenario() { }

    /** Carga un archivo del disco. Una linea mal escrita da IllegalArgumentException con su numero. */
    public static void cargar(String ruta, SistemaAlojamiento sistema) {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacia");
        }
        exigirSistema(sistema);
        try (BufferedReader lector = Files.newBufferedReader(Path.of(ruta))) {
            leer(lector, sistema);
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo " + ruta + ": " + e.getMessage(), e);
        }
    }

    /** Carga el escenario.txt que va dentro del proyecto (lo usa el boton). Fija el Reloj en la fecha de la demo. */
    public static void cargarDesdeRecursos(SistemaAlojamiento sistema) {
        exigirSistema(sistema);
        InputStream flujo = CargadorEscenario.class.getResourceAsStream(RECURSO);
        if (flujo == null) {
            throw new IllegalArgumentException("No se encontro " + RECURSO + " dentro del proyecto");
        }
        Reloj.fijar(FECHA_DEMO);
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(flujo, StandardCharsets.UTF_8))) {
            leer(lector, sistema);
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer " + RECURSO + ": " + e.getMessage(), e);
        }
    }

    // ---------- lectura ----------

    private static void leer(BufferedReader lector, SistemaAlojamiento sistema) throws IOException {
        String seccion = null;
        int numeroLinea = 0;
        String linea = lector.readLine();
        while (linea != null) {
            numeroLinea++;
            if (numeroLinea == 1 && linea.startsWith("\uFEFF")) {
                linea = linea.substring(1);          // algunos editores ponen una marca al inicio
            }
            linea = linea.trim();
            if (!linea.isEmpty() && !linea.startsWith("//")) {
                if (linea.startsWith("#")) {
                    seccion = linea.toUpperCase();
                } else {
                    try {
                        procesarLinea(seccion, linea.split(";", -1), sistema);
                    } catch (RuntimeException e) {
                        throw new IllegalArgumentException("Linea " + numeroLinea + ": " + e.getMessage(), e);
                    }
                }
            }
            linea = lector.readLine();
        }
    }

    private static void procesarLinea(String seccion, String[] c, SistemaAlojamiento sistema) {
        if (seccion == null) {
            throw new IllegalArgumentException("La linea esta antes de una seccion");
        }
        switch (seccion) {
            case "#TEMPORADAS" -> cargarTemporada(c, sistema);
            case "#APARTAMENTOS" -> cargarApartamento(c, sistema);
            case "#NODOS" -> cargarNodo(c, sistema);
            case "#PASILLOS" -> cargarPasillo(c, sistema);
            case "#HUESPEDES" -> cargarHuesped(c, sistema);
            case "#EMPLEADOS" -> cargarEmpleado(c, sistema);
            case "#RESERVAS" -> cargarReserva(c, sistema);
            default -> throw new IllegalArgumentException("Seccion desconocida: " + seccion);
        }
    }

    // ---------- una funcion por seccion ----------

    private static void cargarTemporada(String[] c, SistemaAlojamiento sistema) {
        exigirCampos(c, 3, "temporada");
        sistema.definirTemporada(Temporada.valueOf(campo(c, 0).toUpperCase()),
                LocalDate.parse(campo(c, 1)), LocalDate.parse(campo(c, 2)));
    }

    private static void cargarApartamento(String[] c, SistemaAlojamiento sistema) {
        exigirCampos(c, 10, "apartamento");
        ListaDoble<String> caracteristicas = new ListaDoble<>();
        if (!c[6].isBlank()) {
            for (String caracteristica : c[6].split(",")) {
                caracteristicas.agregarAlFinal(caracteristica.trim());
            }
        }
        Apartamento apto = new Apartamento(campo(c, 0), campo(c, 1), campo(c, 2),
                Integer.parseInt(campo(c, 3)), Integer.parseInt(campo(c, 4)), Integer.parseInt(campo(c, 5)),
                caracteristicas,
                Long.parseLong(campo(c, 7)), Long.parseLong(campo(c, 8)), Long.parseLong(campo(c, 9)));
        sistema.registrarApartamento(apto);
    }

    private static void cargarNodo(String[] c, SistemaAlojamiento sistema) {
        exigirCampos(c, 8, "nodo");
        NodoMapa nodo = new NodoMapa(campo(c, 0), TipoNodo.valueOf(campo(c, 1).toUpperCase()), campo(c, 2),
                campo(c, 3), Integer.parseInt(campo(c, 4)),
                Double.parseDouble(campo(c, 5)), Double.parseDouble(campo(c, 6)), campo(c, 7));
        sistema.mapa().agregarNodo(nodo);
    }

    private static void cargarPasillo(String[] c, SistemaAlojamiento sistema) {
        exigirCampos(c, 3, "pasillo");
        sistema.mapa().conectar(campo(c, 0), campo(c, 1), Double.parseDouble(campo(c, 2)));
    }

    private static void cargarHuesped(String[] c, SistemaAlojamiento sistema) {
        exigirCampos(c, 5, "huesped");
        sistema.registrarHuesped(new Huesped(campo(c, 0), campo(c, 1),
                LocalDate.parse(campo(c, 2)), campo(c, 3), campo(c, 4)));
    }

    private static void cargarEmpleado(String[] c, SistemaAlojamiento sistema) {
        exigirCampos(c, 4, "empleado");
        sistema.registrarEmpleado(new Empleado(campo(c, 0), campo(c, 1),
                Rol.valueOf(campo(c, 2).toUpperCase()), campo(c, 3)));
    }

    private static void cargarReserva(String[] c, SistemaAlojamiento sistema) {
        exigirCampos(c, 7, "reserva");
        String documento = campo(c, 0);
        String codigoApto = campo(c, 1);
        LocalDate entrada = LocalDate.parse(campo(c, 2));
        LocalDate salida = LocalDate.parse(campo(c, 3));
        Canal canal = Canal.valueOf(campo(c, 4).toUpperCase());
        LocalTime horaLlegada = campo(c, 5).isEmpty() ? null : LocalTime.parse(campo(c, 5));

        Huesped titular = sistema.buscarHuesped(documento);
        if (titular == null) {
            throw new IllegalArgumentException("No existe el huesped " + documento);
        }
        ListaDoble<Ocupante> ocupantes = armarOcupantes(titular, c[6]);

        switch (canal) {
            case PORTAL -> sistema.reservarPortal(documento, codigoApto, entrada, salida, ocupantes, horaLlegada);
            case DIRECTA -> sistema.reservarDirecta(documento, codigoApto, entrada, salida, ocupantes, horaLlegada);
            case EXTERNA -> cargarExterna(sistema, documento, codigoApto, entrada, salida, ocupantes, horaLlegada);
        }
    }

    /** Las externas pasan por la cola del canal externo, como en la vida real. */
    private static void cargarExterna(SistemaAlojamiento sistema, String documento, String codigoApto,
                                      LocalDate entrada, LocalDate salida,
                                      ListaDoble<Ocupante> ocupantes, LocalTime horaLlegada) {
        int conflictosAntes = sistema.canalExterno().cantidadConflictos();
        SolicitudExterna solicitud = new SolicitudExterna("Plataforma simulada", documento, codigoApto,
                entrada, salida, ocupantes, horaLlegada, Reloj.ahora());
        sistema.recibirSolicitudExterna(solicitud);
        String resultado = sistema.procesarSiguienteSolicitud();
        if (sistema.canalExterno().cantidadConflictos() > conflictosAntes) {
            throw new IllegalArgumentException("La solicitud externa choca con otra reserva: " + resultado);
        }
    }

    // ---------- ayudas ----------

    /** El titular va primero (tambien es ocupante) y despues los acompanantes (nombre:fecha separados por |). */
    private static ListaDoble<Ocupante> armarOcupantes(Huesped titular, String texto) {
        ListaDoble<Ocupante> ocupantes = new ListaDoble<>();
        ocupantes.agregarAlFinal(new Ocupante(titular.getNombre(), titular.getFechaNacimiento()));
        if (texto.isBlank()) {
            return ocupantes;
        }
        for (String par : texto.trim().split("\\|")) {
            String[] partes = par.split(":");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Ocupante mal escrito (use nombre:fecha): " + par);
            }
            ocupantes.agregarAlFinal(new Ocupante(partes[0].trim(), LocalDate.parse(partes[1].trim())));
        }
        return ocupantes;
    }

    private static String campo(String[] c, int i) {
        return c[i].trim();
    }

    private static void exigirCampos(String[] c, int esperados, String nombre) {
        if (c.length != esperados) {
            throw new IllegalArgumentException("Se esperaban " + esperados + " campos en " + nombre
                    + " y hay " + c.length);
        }
    }

    private static void exigirSistema(SistemaAlojamiento sistema) {
        if (sistema == null) {
            throw new IllegalArgumentException("El sistema no puede ser nulo");
        }
    }
}