package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.comparadores.ComparadoresApartamento;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.ReservaSolapadaException;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.modelo.SolicitudExterna;
import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;
import co.edu.uniquindio.proyectofinal.util.Prueba;
import co.edu.uniquindio.proyectofinal.util.Reloj;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Prueba de SistemaAlojamiento con el Reloj fijo. Necesita las clases de Natalia y Gabriel
 * (cotizador, folios, preparacion, canal externo, vencimientos, mapa, turnos y empleados).
 */
public class PruebaSistema {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    private static LocalDate f(int mes, int dia) {
        return LocalDate.of(2026, mes, dia);
    }

    private static ListaDoble<Ocupante> sinAcompanantes() {
        return new ListaDoble<>();
    }

    private static ListaDoble<Ocupante> unAcompanante() {
        ListaDoble<Ocupante> lista = new ListaDoble<>();
        lista.agregarAlFinal(new Ocupante("Acompanante", LocalDate.of(1992, 3, 3)));
        return lista;
    }

    public static void ejecutar() {
        Prueba.titulo("SistemaAlojamiento");
        Reloj.fijar(LocalDateTime.of(2026, 11, 10, 8, 0));
        SistemaAlojamiento sistema = new SistemaAlojamiento();
        sistema.registrarApartamento(PruebaModelo.apto("A101", 2, 4, 80000));
        sistema.registrarApartamento(PruebaModelo.apto("A102", 1, 2, 60000));
        sistema.registrarHuesped(PruebaModelo.huesped("100"));
        sistema.registrarHuesped(PruebaModelo.huesped("200"));

        // Consultas
        Prueba.verificar(sistema.apartamentos().tamanio() == 2 && sistema.huespedes().tamanio() == 2, "registros y consultas");
        ListaDoble<Apartamento> libres = sistema.buscarDisponibles(f(12, 12), f(12, 14), 2, ComparadoresApartamento.POR_PRECIO);
        Prueba.verificar(libres.tamanio() == 2 && libres.obtener(0).getCodigo().equals("A102"), "disponibles ordenados por precio");

        // Flujo completo por portal: titular + 1 acompanante = 2 adultos, 2 noches, temporada baja
        Reserva r1 = sistema.reservarPortal("100", "A101", f(11, 12), f(11, 14), unAcompanante(), LocalTime.of(15, 0));
        Prueba.verificar(r1.getEstado() == EstadoReserva.PENDIENTE, "portal: queda Pendiente");
        Prueba.verificar(r1.personas() == 2, "el titular cuenta como ocupante");
        Prueba.verificar(r1.getValorTotal() == 2 * 2 * 80000, "el total es noches x personas x tarifa (320.000)");
        Prueba.verificar(sistema.folio(r1.getCodigo()).saldo() == 320000, "el folio queda con un cargo por el total");
        Prueba.verificar(sistema.anticipoRequerido(r1.getCodigo()) == 96000, "el anticipo es el 30 %");
        Prueba.esperarExcepcion(IllegalArgumentException.class, () -> sistema.pagarAnticipo(r1.getCodigo(), 50000),
                "un anticipo menor al 30 % da error");
        sistema.pagarAnticipo(r1.getCodigo(), 96000);
        Prueba.verificar(r1.getEstado() == EstadoReserva.CONFIRMADA && sistema.folio(r1.getCodigo()).saldo() == 224000,
                "pagar el anticipo confirma y baja el saldo");

        // Devolucion y cancelacion de otra reserva
        Reserva r2 = sistema.reservarDirecta("200", "A102", f(12, 20), f(12, 22), sinAcompanantes(), null);
        Prueba.verificar(r2.getEstado() == EstadoReserva.CONFIRMADA, "directa: queda Confirmada");
        sistema.registrarPago(r2.getCodigo(), "Pago", 40000);
        Prueba.verificar(sistema.consultarDevolucion(r2.getCodigo()) == 40000, "con 40 dias de anticipacion se devuelve todo");
        Prueba.verificar(sistema.cancelarReserva(r2.getCodigo()) == 40000 && r2.getEstado() == EstadoReserva.CANCELADA,
                "cancelar devuelve lo pagado");
        Prueba.verificar(sistema.buscarDisponibles(f(12, 20), f(12, 22), 1, ComparadoresApartamento.POR_PRECIO).tamanio() == 2,
                "cancelar libera las noches");

        // Check-in, cargos y deshacer
        Reloj.fijar(LocalDateTime.of(2026, 11, 12, 15, 0));
        sistema.checkIn(r1.getCodigo());
        Prueba.verificar(r1.getEstado() == EstadoReserva.EN_CURSO
                && sistema.buscarApartamento("A101").getEstado() == EstadoApartamento.OCUPADO, "check-in: En curso y Ocupado");
        sistema.registrarCargo(r1.getCodigo(), "Minibar", 15000);
        Prueba.verificar(sistema.folio(r1.getCodigo()).saldo() == 239000, "cargo suma al saldo");
        sistema.deshacerUltima(r1.getCodigo());
        Prueba.verificar(sistema.folio(r1.getCodigo()).saldo() == 224000, "deshacer deja el saldo como estaba");

        // Check-out: entra a la cola de preparacion
        Reloj.fijar(LocalDateTime.of(2026, 11, 14, 11, 0));
        sistema.checkOut(r1.getCodigo());
        Prueba.verificar(r1.getEstado() == EstadoReserva.FINALIZADA
                        && sistema.buscarApartamento("A101").getEstado() == EstadoApartamento.PENDIENTE_PREPARACION,
                "check-out: Finalizada y apartamento Pendiente de preparacion");
        Prueba.verificar(sistema.colaPreparacion().tamanio() == 1, "el apartamento entra a la cola de preparacion");
        Prueba.verificar(sistema.historial("100").tamanio() == 1, "la estancia queda en el historial");
        Prueba.esperarExcepcion(TransicionEstadoInvalidaException.class, () -> sistema.checkIn(r1.getCodigo()),
                "no se hace check-in dos veces");

        // Check-out con entrada hoy: prioridad 1
        Reloj.fijar(LocalDateTime.of(2026, 11, 20, 8, 0));
        Reserva r3 = sistema.reservarDirecta("100", "A102", f(11, 20), f(11, 22), sinAcompanantes(), LocalTime.of(14, 0));
        sistema.checkIn(r3.getCodigo());
        Reserva r4 = sistema.reservarDirecta("200", "A102", f(11, 22), f(11, 23), sinAcompanantes(), LocalTime.of(12, 0));
        Reloj.fijar(LocalDateTime.of(2026, 11, 22, 10, 0));
        sistema.checkOut(r3.getCodigo());
        Prueba.verificar(sistema.colaPreparacion().tamanio() >= 1, "el check-out con entrada hoy tambien entra a la cola");

        // Canal externo: conflicto de canal
        Reloj.fijar(LocalDateTime.of(2026, 12, 1, 8, 0));
        sistema.reservarDirecta("100", "A101", f(12, 5), f(12, 8), sinAcompanantes(), null);
        sistema.recibirSolicitudExterna(new SolicitudExterna("PlataformaSim", "200", "A101", f(12, 6), f(12, 9),
                sinAcompanantes(), LocalTime.of(16, 0), Reloj.ahora()));
        String mensaje = sistema.procesarSiguienteSolicitud();
        Prueba.verificar(mensaje.startsWith("Conflicto de canal"), "una solicitud externa que choca queda como conflicto");
        Prueba.verificar(sistema.canalExterno().cantidadConflictos() == 1, "el conflicto queda registrado");

        // Vencimiento a las 24 horas
        Reserva r5 = sistema.reservarPortal("200", "A102", f(12, 10), f(12, 12), sinAcompanantes(), null);
        Reloj.fijar(LocalDateTime.of(2026, 12, 1, 9, 0));
        Prueba.verificar(sistema.revisarVencimientos().tamanio() == 0, "antes de las 24 horas no vence");
        Reloj.fijar(LocalDateTime.of(2026, 12, 2, 8, 30));
        ListaDoble<Reserva> vencidas = sistema.revisarVencimientos();
        Prueba.verificar(vencidas.tamanio() == 1 && r5.getEstado() == EstadoReserva.CANCELADA, "a las 24 horas la pendiente se cancela");
        Prueba.verificar(sistema.buscarDisponibles(f(12, 10), f(12, 12), 1, ComparadoresApartamento.POR_PRECIO).tamanio() == 2,
                "la reserva vencida libera sus noches");

        // Favoritos
        Prueba.verificar(sistema.agregarFavorito("100", "A101") && !sistema.agregarFavorito("100", "A101"), "favoritos sin repetidos");
        Prueba.esperarExcepcion(IllegalArgumentException.class, () -> sistema.agregarFavorito("100", "NOEXISTE"),
                "favorito de un apartamento que no existe da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class, () -> sistema.reservarPortal("999", "A101", f(12, 20), f(12, 22), null, null),
                "un huesped que no existe da error");
        Reloj.usarHoraReal();
    }
}
