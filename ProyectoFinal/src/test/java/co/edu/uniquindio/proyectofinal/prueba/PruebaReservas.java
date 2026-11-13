package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.comparadores.ComparadoresApartamento;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;
import co.edu.uniquindio.proyectofinal.excepciones.ReservaSolapadaException;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.Canal;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.servicio.CatalogoApartamentos;
import co.edu.uniquindio.proyectofinal.servicio.GestorReservas;
import co.edu.uniquindio.proyectofinal.servicio.RegistroHuespedes;
import co.edu.uniquindio.proyectofinal.util.Prueba;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pruebas de CatalogoApartamentos, RegistroHuespedes y GestorReservas. */
public class PruebaReservas {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    private static LocalDate f(int mes, int dia) {
        return LocalDate.of(2026, mes, dia);
    }

    public static void ejecutar() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 10, 8, 0);

        Prueba.titulo("CatalogoApartamentos y RegistroHuespedes");
        CatalogoApartamentos catalogo = new CatalogoApartamentos();
        Apartamento a101 = PruebaModelo.apto("A101", 2, 4, 80000);
        Apartamento a102 = PruebaModelo.apto("A102", 1, 2, 60000);
        Apartamento b201 = PruebaModelo.apto("B201", 3, 8, 120000);
        catalogo.agregar(a101);
        catalogo.agregar(a102);
        catalogo.agregar(b201);
        Prueba.verificar(catalogo.cantidad() == 3 && catalogo.buscar("A102") == a102, "agregar y buscar por codigo");
        Prueba.verificar(catalogo.buscar("Z999") == null, "buscar uno que no existe da null");
        Prueba.esperarExcepcion(IllegalStateException.class, () -> catalogo.agregar(PruebaModelo.apto("A101", 1, 1, 1000)),
                "codigo repetido da IllegalStateException");
        Prueba.verificar(catalogo.todos().toString().equals("[A101 - Apto A101 <-> A102 - Apto A102 <-> B201 - Apto B201]"),
                "todos salen ordenados por codigo");
        Prueba.verificar(catalogo.altura() == 3, "altura del arbol del catalogo (A101, A102, B201 en cadena)");

        RegistroHuespedes registro = new RegistroHuespedes();
        Huesped h1 = PruebaModelo.huesped("100");
        Huesped h2 = PruebaModelo.huesped("200");
        registro.registrar(h1);
        registro.registrar(h2);
        Prueba.verificar(registro.cantidad() == 2 && registro.buscar("200") == h2, "registrar y buscar huesped por documento");
        Prueba.esperarExcepcion(IllegalStateException.class, () -> registro.registrar(PruebaModelo.huesped("100")),
                "documento repetido da IllegalStateException");
        Prueba.verificar(registro.eliminar("100") && registro.buscar("100") == null && !registro.eliminar("100"), "eliminar huesped");

        Prueba.titulo("GestorReservas: crear");
        GestorReservas gestor = new GestorReservas();
        Reserva portal = gestor.crear(h2, a101, new Estancia(f(12, 10), f(12, 12)), PruebaModelo.adultos(2),
                Canal.PORTAL, null, 320000, ahora);
        Prueba.verificar(portal.getEstado() == EstadoReserva.PENDIENTE && portal.getCodigo().equals("R0001"),
                "una de portal queda Pendiente (R0001)");
        Reserva directa = gestor.crear(h2, a102, new Estancia(f(12, 10), f(12, 12)), PruebaModelo.adultos(2),
                Canal.DIRECTA, null, 0, ahora);
        Prueba.verificar(directa.getEstado() == EstadoReserva.CONFIRMADA && directa.getCodigo().equals("R0002"),
                "una directa queda Confirmada (R0002)");
        Prueba.verificar(a101.getNumeroReservas() == 1, "suma numeroReservas");
        Prueba.esperarExcepcion(ReservaSolapadaException.class,
                () -> gestor.crear(h2, a101, new Estancia(f(12, 11), f(12, 13)), PruebaModelo.adultos(2), Canal.DIRECTA, null, 0, ahora),
                "un choque da ReservaSolapadaException");
        Prueba.verificar(gestor.crear(h2, a101, new Estancia(f(12, 12), f(12, 14)), PruebaModelo.adultos(2),
                Canal.DIRECTA, null, 0, ahora) != null, "el dia de salida otro puede entrar");
        Prueba.esperarExcepcion(CapacidadExcedidaException.class,
                () -> gestor.crear(h2, a102, new Estancia(f(12, 28), f(12, 30)), PruebaModelo.adultos(3), Canal.DIRECTA, null, 0, ahora),
                "3 personas en capacidad 2 da error");
        Prueba.esperarExcepcion(FechasInvalidasException.class,
                () -> gestor.crear(h2, a101, new Estancia(f(11, 1), f(11, 3)), PruebaModelo.adultos(2), Canal.DIRECTA, null, 0, ahora),
                "una fecha pasada da FechasInvalidasException");
        Prueba.verificar(gestor.crear(h2, a101, new Estancia(f(11, 10), f(11, 11)), PruebaModelo.adultos(1),
                Canal.DIRECTA, null, 0, ahora) != null, "entrar hoy mismo si se puede");
        Apartamento fuera = PruebaModelo.apto("F1", 1, 2, 50000);
        fuera.ponerFueraDeServicio("Remodelacion");
        Prueba.esperarExcepcion(IllegalStateException.class,
                () -> gestor.crear(h2, fuera, new Estancia(f(12, 1), f(12, 3)), PruebaModelo.adultos(1), Canal.DIRECTA, null, 0, ahora),
                "un apartamento fuera de servicio no se reserva");
        Prueba.verificar(gestor.buscar("R0001") == portal && gestor.buscar("R9999") == null, "buscar reserva por codigo");
        Prueba.verificar(gestor.reservasDe(h2).tamanio() == gestor.todas().tamanio(), "reservasDe el titular");

        Prueba.titulo("GestorReservas: cancelar y no-show");
        gestor.cancelar(portal);
        Prueba.verificar(portal.getEstado() == EstadoReserva.CANCELADA, "cancelar deja la reserva Cancelada");
        Prueba.verificar(!a101.getReservas().haySolapamiento(new Estancia(f(12, 10), f(12, 12))),
                "cancelar libera las noches");
        Prueba.verificar(gestor.crear(h2, a101, new Estancia(f(12, 10), f(12, 12)), PruebaModelo.adultos(2),
                Canal.DIRECTA, null, 0, ahora) != null, "se puede volver a reservar lo mismo");
        Prueba.esperarExcepcion(TransicionEstadoInvalidaException.class, () -> gestor.cancelar(portal), "cancelar dos veces da error");
        Reserva paraNoShow = gestor.crear(h2, b201, new Estancia(f(12, 1), f(12, 3)), PruebaModelo.adultos(1),
                Canal.DIRECTA, null, 0, ahora);
        gestor.marcarNoShow(paraNoShow);
        Prueba.verificar(paraNoShow.getEstado() == EstadoReserva.NO_SHOW, "marcarNoShow");

        Prueba.titulo("GestorReservas: check-in y check-out");
        Reserva hoy = gestor.crear(h2, b201, new Estancia(f(11, 10), f(11, 12)), PruebaModelo.adultos(2),
                Canal.DIRECTA, null, 0, ahora);
        b201.cambiarEstado(EstadoApartamento.OCUPADO); // simulamos que esta ocupado por otro grupo
        b201.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        Prueba.esperarExcepcion(TransicionEstadoInvalidaException.class, () -> gestor.checkIn(hoy, f(11, 10)),
                "check-in con el apartamento pendiente de preparacion da error");
        Prueba.verificar(gestor.tieneEntradaHoy(b201, f(11, 10)) == hoy, "tieneEntradaHoy encuentra la reserva");
        Prueba.verificar(gestor.tieneEntradaHoy(b201, f(11, 11)) == null, "tieneEntradaHoy da null otro dia");
        b201.cambiarEstado(EstadoApartamento.EN_PREPARACION);
        b201.cambiarEstado(EstadoApartamento.PREPARADO);
        Prueba.esperarExcepcion(FechasInvalidasException.class, () -> gestor.checkIn(hoy, f(11, 9)),
                "check-in otro dia que no es la entrada da error");
        gestor.checkIn(hoy, f(11, 10));
        Prueba.verificar(hoy.getEstado() == EstadoReserva.EN_CURSO && b201.getEstado() == EstadoApartamento.OCUPADO,
                "check-in: reserva En curso y apartamento Ocupado");
        gestor.checkOut(hoy);
        Prueba.verificar(hoy.getEstado() == EstadoReserva.FINALIZADA
                && b201.getEstado() == EstadoApartamento.PENDIENTE_PREPARACION, "check-out: Finalizada y apartamento Pendiente");
        Prueba.verificar(h2.getHistorial().tamanio() == 1 && h2.getHistorial().obtener(0) == hoy, "check-out llena el historial");

        Prueba.titulo("GestorReservas: devolucion");
        Reserva dev = gestor.crear(h2, a102, new Estancia(f(12, 20), f(12, 22)), PruebaModelo.adultos(2),
                Canal.PORTAL, null, 100000, ahora);
        Prueba.verificar(gestor.calcularDevolucion(dev, 30000, f(12, 10)) == 30000, "10 dias antes devuelve todo");
        Prueba.verificar(gestor.calcularDevolucion(dev, 30000, f(12, 16)) == 15000, "4 dias antes devuelve la mitad");
        Prueba.verificar(gestor.calcularDevolucion(dev, 30000, f(12, 19)) == 0, "1 dia antes no devuelve nada");
        Prueba.verificar(gestor.calcularDevolucion(dev, 0, f(12, 1)) == 0, "sin pagos no hay devolucion");

        Prueba.titulo("Disponibilidad ordenada");
        Estancia pedida = new Estancia(f(12, 10), f(12, 12));
        ListaDoble<Apartamento> libres = catalogo.disponiblesOrdenados(pedida, 2, ComparadoresApartamento.POR_PRECIO);
        // A101 (ocupada 10-12 por la nueva directa) y A102 (directa 10-12) estan tomados: solo B201
        Prueba.verificar(libres.tamanio() == 1 && libres.obtener(0) == b201, "solo queda B201 libre");
        Estancia otra = new Estancia(f(2, 1), f(2, 3));
        ListaDoble<Apartamento> todos = catalogo.disponiblesOrdenados(otra, 2, ComparadoresApartamento.POR_PRECIO);
        Prueba.verificar(todos.tamanio() == 3 && todos.obtener(0) == a102 && todos.obtener(2) == b201,
                "disponibles ordenados por precio");
        Prueba.verificar(catalogo.disponibles(otra, 5).tamanio() == 1, "5 personas solo caben en B201");
        Prueba.esperarExcepcion(IllegalStateException.class, () -> catalogo.eliminar("A101"),
                "no se elimina un apartamento con reservas activas");
        Apartamento sinReservas = PruebaModelo.apto("Z1", 1, 2, 1000);
        catalogo.agregar(sinReservas);
        Prueba.verificar(catalogo.eliminar("Z1") && catalogo.buscar("Z1") == null, "se elimina el que no tiene reservas activas");
    }
}
