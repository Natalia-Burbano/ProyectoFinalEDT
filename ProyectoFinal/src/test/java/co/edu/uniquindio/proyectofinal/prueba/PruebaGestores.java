package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.Canal;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.modelo.SolicitudExterna;
import co.edu.uniquindio.proyectofinal.modelo.TareaPreparacion;
import co.edu.uniquindio.proyectofinal.servicio.CanalExterno;
import co.edu.uniquindio.proyectofinal.servicio.GestorFolio;
import co.edu.uniquindio.proyectofinal.servicio.GestorPreparacion;
import co.edu.uniquindio.proyectofinal.servicio.VencimientoReservas;
import co.edu.uniquindio.proyectofinal.util.Prueba;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Pruebas de GestorFolio, GestorPreparacion, CanalExterno y VencimientoReservas.
 */
public class PruebaGestores {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /**
     * Casos de prueba de los gestores.
     */
    public static void ejecutar() {
        probarGestorFolio();
        probarPreparacion();
        probarCanalExterno();
        probarVencimientos();
    }

    // ------------------ GestorFolio ------------------

    private static void probarGestorFolio() {
        Prueba.titulo("GestorFolio");
        LocalDateTime hora = LocalDateTime.of(2026, 3, 10, 9, 0);
        GestorFolio gestor = new GestorFolio();

        gestor.abrirFolio("R0002");
        gestor.abrirFolio("R0001");
        Prueba.verificar(gestor.cantidad() == 2, "se abrieron 2 folios");
        Prueba.esperarExcepcion(IllegalStateException.class,
                () -> gestor.abrirFolio("R0001"), "abrir un folio repetido da error");

        gestor.registrarCargo("R0001", "Hospedaje", 300000, hora);
        gestor.registrarPago("R0001", "Anticipo", 90000, hora);
        Prueba.verificar(gestor.saldo("R0001") == 210000, "el saldo de R0001 es 210.000");
        Prueba.verificar(gestor.totalPagado("R0001") == 90000, "lo pagado de R0001 es 90.000");
        gestor.deshacerUltima("R0001", hora);
        Prueba.verificar(gestor.saldo("R0001") == 300000, "deshacer desde el gestor deja saldo 300.000");

        Prueba.verificar(gestor.buscar("R0009") == null, "buscar un folio que no existe da null");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> gestor.registrarCargo("R0009", "Algo", 1000, hora), "un cargo a un folio que no existe da error");

        Prueba.verificar(gestor.todos().obtener(0).getCodigoReserva().equals("R0001"),
                "todos() sale ordenado por codigo (R0001 primero)");
    }

    // ------------------ GestorPreparacion ------------------

    private static void probarPreparacion() {
        Prueba.titulo("GestorPreparacion");
        LocalDateTime ahora = LocalDateTime.of(2026, 3, 10, 8, 0);
        GestorPreparacion gestor = new GestorPreparacion();

        Apartamento a = pendiente("A");
        Apartamento b = pendiente("B");
        Apartamento c = pendiente("C");

        gestor.agregar(a, true, LocalTime.of(15, 0), ahora);   // entrada hoy a las 15:00
        gestor.agregar(b, false, null, ahora);                 // sin entrada hoy
        gestor.agregar(c, true, LocalTime.of(11, 0), ahora);   // entrada hoy a las 11:00

        Prueba.verificar(textoCola(gestor).equals("C A B"), "A hoy 15:00, B sin entrada y C hoy 11:00 salen C, A, B");
        Prueba.verificar(gestor.siguiente().getCodigoApartamento().equals("C"), "siguiente() mira a C sin sacarlo");
        Prueba.verificar(gestor.cantidadPendientes() == 3, "siguiente() no saca nada");

        Prueba.esperarExcepcion(IllegalStateException.class,
                () -> gestor.agregar(a, false, null, ahora), "agregar un apartamento que ya esta en la cola da error");
        Apartamento listo = new Apartamento("D", "Apto D", "A", 1, 1, 2, null, 50000, 60000, 70000);
        Prueba.esperarExcepcion(TransicionEstadoInvalidaException.class,
                () -> gestor.agregar(listo, false, null, ahora), "un apartamento que no esta pendiente no entra a la cola");

        // un apartamento sin entrada hoy recibe una reserva para hoy
        Prueba.verificar(gestor.subirAPrioridad1("B", LocalTime.of(10, 0)), "subirAPrioridad1(B) devuelve true");
        Prueba.verificar(textoCola(gestor).equals("B C A"), "B con llegada a las 10:00 queda primero: B C A");
        Prueba.verificar(!gestor.subirAPrioridad1("Z", LocalTime.of(10, 0)), "subir uno que no esta en la cola da false");

        // preparar y terminar
        Apartamento primero = gestor.iniciarSiguiente(ahora.plusMinutes(10));
        Prueba.verificar(primero.getCodigo().equals("B") && primero.getEstado() == EstadoApartamento.EN_PREPARACION,
                "iniciarSiguiente saca a B y lo deja EN_PREPARACION");
        gestor.terminar("B", ahora.plusMinutes(40));
        Prueba.verificar(b.getEstado() == EstadoApartamento.PREPARADO, "terminar deja a B PREPARADO");
        Prueba.verificar(gestor.tiempoPromedioMinutos() == 40.0, "B tardo 40 minutos desde que quedo pendiente");

        gestor.iniciarSiguiente(ahora);
        gestor.terminar("C", ahora.plusMinutes(60));
        Prueba.verificar(gestor.tiempoPromedioMinutos() == 50.0, "el promedio de 40 y 60 minutos es 50");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> gestor.terminar("A", ahora), "terminar uno que no se esta preparando da error");

        gestor.iniciarSiguiente(ahora);
        Prueba.esperarExcepcion(EstructuraVaciaException.class,
                () -> gestor.iniciarSiguiente(ahora), "iniciar con la cola vacia da error");
    }

    // ------------------ CanalExterno ------------------

    private static void probarCanalExterno() {
        Prueba.titulo("CanalExterno");
        CanalExterno canal = new CanalExterno();
        LocalDateTime hora = LocalDateTime.of(2026, 3, 1, 9, 0);

        SolicitudExterna s1 = solicitud("111", hora);
        SolicitudExterna s2 = solicitud("222", hora.plusMinutes(1));
        SolicitudExterna s3 = solicitud("333", hora.plusMinutes(2));
        canal.recibir(s1);
        canal.recibir(s2);
        canal.recibir(s3);

        Prueba.verificar(canal.pendientes().tamanio() == 3 && canal.haySolicitudes(), "hay 3 solicitudes pendientes");
        Prueba.verificar(canal.siguiente() == s1, "la primera que sale es la primera que llego");
        Prueba.verificar(canal.siguiente() == s2, "la segunda que sale es la segunda que llego");
        Prueba.verificar(canal.siguiente() == s3, "la tercera que sale es la tercera que llego");
        Prueba.verificar(!canal.haySolicitudes(), "ya no quedan solicitudes");
        Prueba.esperarExcepcion(EstructuraVaciaException.class, canal::siguiente, "pedir la siguiente con la cola vacia da error");

        canal.registrarConflicto(s2, "Choca con la reserva R0001", hora);
        Prueba.verificar(canal.cantidadConflictos() == 1, "se registro 1 conflicto");
        Prueba.verificar(canal.conflictos().obtener(0).getSolicitud() == s2, "el conflicto guarda la solicitud que choco");
    }

    // ------------------ VencimientoReservas ------------------

    private static void probarVencimientos() {
        Prueba.titulo("VencimientoReservas");
        VencimientoReservas vencimientos = new VencimientoReservas();

        Reserva r8 = reservaPendiente("R0001", LocalDateTime.of(2026, 3, 1, 8, 0));
        Reserva r9 = reservaPendiente("R0002", LocalDateTime.of(2026, 3, 1, 9, 0));
        vencimientos.agregar(r9);
        vencimientos.agregar(r8);

        Prueba.verificar(vencimientos.proximaAVencer() == r8, "la proxima a vencer es la creada a las 8:00");

        ListaDoble<Reserva> vencidas = vencimientos.vencidas(LocalDateTime.of(2026, 3, 2, 8, 30));
        Prueba.verificar(vencidas.tamanio() == 1 && vencidas.obtener(0) == r8,
                "al otro dia a las 8:30 solo vence la de las 8:00");
        Prueba.verificar(vencimientos.cantidad() == 1, "la de las 9:00 sigue esperando");

        Prueba.verificar(vencimientos.quitar(r9), "quitar la de las 9:00 (pago el anticipo) devuelve true");
        Prueba.verificar(vencimientos.proximaAVencer() == null, "sin reservas, proximaAVencer da null");

        Reserva confirmada = reservaPendiente("R0003", LocalDateTime.of(2026, 3, 1, 10, 0));
        confirmada.cambiarEstado(EstadoReserva.CONFIRMADA);
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> vencimientos.agregar(confirmada), "una reserva confirmada no entra a vencimientos");
    }

    // ------------------ ayudantes ------------------

    /** Crea un apartamento y lo lleva a PENDIENTE_PREPARACION (como despues de un check-out). */
    private static Apartamento pendiente(String codigo) {
        Apartamento apto = new Apartamento(codigo, "Apto " + codigo, "A", 1, 1, 2, null, 50000, 60000, 70000);
        apto.cambiarEstado(EstadoApartamento.OCUPADO);
        apto.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        return apto;
    }

    /** Muestra el orden de la cola de preparacion, por ejemplo "C A B". */
    private static String textoCola(GestorPreparacion gestor) {
        String texto = "";
        for (TareaPreparacion t : gestor.pendientesEnOrden()) {
            texto += t.getCodigoApartamento() + " ";
        }
        return texto.trim();
    }

    private static ListaDoble<Ocupante> unaPersona() {
        ListaDoble<Ocupante> ocupantes = new ListaDoble<>();
        ocupantes.agregarAlFinal(new Ocupante("Ana", LocalDate.of(1990, 1, 1)));
        return ocupantes;
    }

    private static SolicitudExterna solicitud(String documento, LocalDateTime recibida) {
        return new SolicitudExterna("Booking", documento, "A101", LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 3), unaPersona(), LocalTime.of(15, 0), recibida);
    }

    private static Reserva reservaPendiente(String codigo, LocalDateTime creadaEn) {
        Huesped titular = new Huesped("100", "Ana", LocalDate.of(1990, 1, 1), "3000000", "ana@correo.com");
        Apartamento apto = new Apartamento("A101", "Apto 101", "A", 1, 1, 2, null, 50000, 60000, 70000);
        Estancia estancia = new Estancia(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3));
        return new Reserva(codigo, titular, apto, estancia, unaPersona(), Canal.PORTAL,
                LocalTime.of(15, 0), 100000, creadaEn);
    }
}
