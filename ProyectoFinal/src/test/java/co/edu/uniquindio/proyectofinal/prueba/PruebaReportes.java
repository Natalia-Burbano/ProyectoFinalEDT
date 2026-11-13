package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.datos.CargadorEscenario;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;
import co.edu.uniquindio.proyectofinal.modelo.FilaReporte;
import co.edu.uniquindio.proyectofinal.servicio.Reportes;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Prueba;
import co.edu.uniquindio.proyectofinal.util.Reloj;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pruebas de Reportes, con 2 reservas pequenias calculadas a mano. */
public class PruebaReportes {

    // R0001: A101, 22 al 24 de nov (2 noches baja), directa
    // R0002: A102, 14 al 16 de dic (noche 14 baja, noche 15 alta), portal, con anticipo de 200000
    private static final String ESCENARIO_PEQUENO = """
            #TEMPORADAS
            BAJA;2026-01-01;2026-12-14
            ALTA;2026-12-15;2027-01-15
            #APARTAMENTOS
            A101;Uno;A;1;1;2;Wifi;100000;120000;150000
            A102;Dos;A;1;2;4;Wifi;100000;120000;150000
            #HUESPEDES
            1;Ana Uno;1990-01-01;300;ana@example.com
            2;Beto Dos;1990-01-01;300;beto@example.com
            #RESERVAS
            1;A101;2026-11-22;2026-11-24;DIRECTA;;
            2;A102;2026-12-14;2026-12-16;PORTAL;;Hijo Dos:2000-01-01
            """;

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    private static double valorDe(ListaDoble<FilaReporte> filas, String etiqueta) {
        for (FilaReporte fila : filas) {
            if (fila.getEtiqueta().equals(etiqueta)) {
                return fila.getValor();
            }
        }
        return -1;
    }

    private static boolean casi(double a, double b) {
        return Math.abs(a - b) < 0.01;
    }

    /** Casos de prueba de los reportes. */
    public static void ejecutar() {
        Prueba.titulo("Reportes");

        Reloj.fijar(LocalDateTime.of(2026, 11, 20, 8, 0));
        SistemaAlojamiento sistema = new SistemaAlojamiento();
        CargadorEscenario.cargar(PruebaCargadorEscenario.escribirTemporal(ESCENARIO_PEQUENO), sistema);
        sistema.pagarAnticipo("R0002", 200000);
        Reportes reportes = new Reportes(sistema);

        // ocupacion por apartamento: 10 noches (del 20 al 30 de nov)
        ListaDoble<FilaReporte> noviembre =
                reportes.ocupacionPorApartamento(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 30));
        Prueba.verificar(noviembre.tamanio() == 2, "una fila por apartamento");
        Prueba.verificar(casi(valorDe(noviembre, "A101"), 20), "A101 ocupa 2 de 10 noches = 20 %");
        Prueba.verificar(casi(valorDe(noviembre, "A102"), 0), "A102 ocupa 0 de 10 noches = 0 %");

        ListaDoble<FilaReporte> diciembre =
                reportes.ocupacionPorApartamento(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 20));
        Prueba.verificar(casi(valorDe(diciembre, "A102"), 20), "en diciembre A102 ocupa 2 de 10 noches = 20 %");
        Prueba.verificar(casi(valorDe(diciembre, "A101"), 0), "en diciembre A101 ocupa 0 noches");

        // ocupacion por temporada: 3 noches bajas, 0 medias y 1 alta
        ListaDoble<FilaReporte> porTemporada = reportes.ocupacionPorTemporada();
        Prueba.verificar(casi(valorDe(porTemporada, "Baja"), 3), "3 noches ocupadas en temporada baja");
        Prueba.verificar(casi(valorDe(porTemporada, "Media"), 0), "0 noches ocupadas en temporada media");
        Prueba.verificar(casi(valorDe(porTemporada, "Alta"), 1), "1 noche ocupada en temporada alta");

        // ingresos: el anticipo de 200000 se pago el 20 de noviembre
        ListaDoble<FilaReporte> ingresosDia =
                reportes.ingresosPorPeriodo(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 20));
        Prueba.verificar(casi(valorDe(ingresosDia, "Pagos recibidos"), 200000), "pagos del 20 de nov: 200000");
        Prueba.verificar(casi(valorDe(ingresosDia, "Ingresos netos"), 200000), "ingresos netos del 20 de nov: 200000");
        ListaDoble<FilaReporte> ingresosDiciembre =
                reportes.ingresosPorPeriodo(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));
        Prueba.verificar(casi(valorDe(ingresosDiciembre, "Ingresos netos"), 0), "sin pagos en diciembre: 0");

        // reservas por canal
        ListaDoble<FilaReporte> porCanal = reportes.reservasPorCanal();
        Prueba.verificar(casi(valorDe(porCanal, "Portal"), 1), "1 reserva por portal");
        Prueba.verificar(casi(valorDe(porCanal, "Directa"), 1), "1 reserva directa");
        Prueba.verificar(casi(valorDe(porCanal, "Externa"), 0), "0 reservas externas");
        Prueba.verificar(casi(valorDe(porCanal, "Canceladas"), 0), "0 canceladas");

        // conflictos y preparacion
        Prueba.verificar(casi(valorDe(reportes.conflictosDeCanal(), "Total de conflictos"), 0),
                "sin conflictos de canal");
        Prueba.verificar(reportes.tiempoPromedioPreparacion().tamanio() == 1,
                "el reporte de preparacion tiene una fila");

        // errores
        Prueba.esperarExcepcion(FechasInvalidasException.class,
                () -> reportes.ocupacionPorApartamento(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1)),
                "ocupacion con hasta antes de desde da FechasInvalidasException");
        Prueba.esperarExcepcion(FechasInvalidasException.class,
                () -> reportes.ingresosPorPeriodo(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1)),
                "ingresos con hasta antes de desde da FechasInvalidasException");

        // con el escenario completo: 4 portal, 4 directas y 2 externas
        SistemaAlojamiento completo = new SistemaAlojamiento();
        CargadorEscenario.cargarDesdeRecursos(completo);
        ListaDoble<FilaReporte> canalesCompleto = new Reportes(completo).reservasPorCanal();
        Prueba.verificar(casi(valorDe(canalesCompleto, "Portal"), 4), "escenario completo: 4 por portal");
        Prueba.verificar(casi(valorDe(canalesCompleto, "Directa"), 4), "escenario completo: 4 directas");
        Prueba.verificar(casi(valorDe(canalesCompleto, "Externa"), 2), "escenario completo: 2 externas");

        Reloj.usarHoraReal();
    }
}