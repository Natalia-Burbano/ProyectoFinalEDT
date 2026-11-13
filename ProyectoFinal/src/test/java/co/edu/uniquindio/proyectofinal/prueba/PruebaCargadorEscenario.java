package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.datos.CargadorEscenario;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Prueba;
import co.edu.uniquindio.proyectofinal.util.Reloj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Pruebas del CargadorEscenario. */
public class PruebaCargadorEscenario {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /** Escribe un archivo temporal con ese contenido y devuelve su ruta. */
    static String escribirTemporal(String contenido) {
        try {
            Path archivo = Files.createTempFile("escenario", ".txt");
            Files.writeString(archivo, contenido);
            archivo.toFile().deleteOnExit();
            return archivo.toString();
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el archivo temporal", e);
        }
    }

    /** Intenta cargar ese contenido y devuelve el mensaje del error (o null si cargo bien). */
    private static String mensajeDeError(String contenido) {
        SistemaAlojamiento sistema = new SistemaAlojamiento();
        try {
            CargadorEscenario.cargar(escribirTemporal(contenido), sistema);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        return null;
    }

    private static int contarReservas(SistemaAlojamiento sistema) {
        int total = 0;
        for (Huesped huesped : sistema.huespedes()) {
            total += sistema.reservasDe(huesped.getDocumento()).tamanio();
        }
        return total;
    }

    /** Casos de prueba del cargador. */
    public static void ejecutar() {
        Prueba.titulo("CargadorEscenario");

        // el escenario completo de resources
        SistemaAlojamiento sistema = new SistemaAlojamiento();
        CargadorEscenario.cargarDesdeRecursos(sistema);
        Prueba.verificar(sistema.apartamentos().tamanio() == 12, "carga 12 apartamentos");
        Prueba.verificar(sistema.huespedes().tamanio() == 12, "carga 12 huespedes");
        Prueba.verificar(sistema.empleados().tamanio() == 6, "carga 6 empleados");
        Prueba.verificar(sistema.mapa().nodos().tamanio() == 17, "carga 17 nodos del mapa");
        Prueba.verificar(sistema.mapa().esConexo(), "los pasillos conectan todo el edificio");
        Prueba.verificar(contarReservas(sistema) == 10, "carga 10 reservas");
        Prueba.verificar(sistema.buscarApartamento("A302") != null, "encuentra el apartamento A302");
        Prueba.verificar(sistema.buscarHuesped("1000000001") != null, "encuentra al huesped 1000000001");
        Prueba.verificar(sistema.mapa().buscarNodo("REC") != null, "encuentra el nodo de recepcion");
        Reloj.usarHoraReal();

        // comentarios, lineas vacias y marca de inicio de archivo
        SistemaAlojamiento conComentarios = new SistemaAlojamiento();
        CargadorEscenario.cargar(escribirTemporal(
                        "\uFEFF// un comentario\n\n#TEMPORADAS\n// otro comentario\nALTA;2026-12-15;2027-01-15\n"),
                conComentarios);
        Prueba.verificar(conComentarios.temporadas().periodos().tamanio() == 1,
                "ignora comentarios y lineas vacias y carga la temporada");

        // errores con el numero de linea
        String faltaCampo = mensajeDeError("// comentario\n#HUESPEDES\n1;Ana;2000-01-01;300\n");
        Prueba.verificar(faltaCampo != null && faltaCampo.contains("Linea 3"),
                "una linea con un campo de menos da error con su numero (Linea 3)");

        String fechaMala = mensajeDeError("#TEMPORADAS\nBAJA;hola;2026-12-14\n");
        Prueba.verificar(fechaMala != null && fechaMala.contains("Linea 2"),
                "una fecha mal escrita da error con su numero (Linea 2)");

        String sinSeccion = mensajeDeError("BAJA;2026-01-01;2026-12-14\n");
        Prueba.verificar(sinSeccion != null && sinSeccion.contains("Linea 1"),
                "una linea antes de cualquier seccion da error (Linea 1)");

        String seccionRara = mensajeDeError("#PLANETAS\nTierra;1\n");
        Prueba.verificar(seccionRara != null && seccionRara.contains("Linea 2"),
                "una seccion desconocida da error (Linea 2)");

        String repetido = mensajeDeError("#HUESPEDES\n1;Ana;2000-01-01;300;a@x.com\n1;Ana;2000-01-01;300;a@x.com\n");
        Prueba.verificar(repetido != null && repetido.contains("Linea 3"),
                "un huesped repetido da error con su numero (Linea 3)");

        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> CargadorEscenario.cargar("no-existe.txt", new SistemaAlojamiento()),
                "un archivo que no existe da IllegalArgumentException");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> CargadorEscenario.cargar("", new SistemaAlojamiento()),
                "una ruta vacia da error");
    }
}