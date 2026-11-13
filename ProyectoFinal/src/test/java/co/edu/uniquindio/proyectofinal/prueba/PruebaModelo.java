package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.comparadores.ComparadoresApartamento;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.FechasInvalidasException;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.Canal;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoReserva;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.Huesped;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import co.edu.uniquindio.proyectofinal.modelo.Temporada;
import co.edu.uniquindio.proyectofinal.util.Prueba;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pruebas del modelo: estados, apartamento, huesped, ocupante, estancia, reserva, arbol de reservas y comparadores. */
public class PruebaModelo {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    private static LocalDate f(int mes, int dia) {
        return LocalDate.of(2026, mes, dia);
    }

    static Apartamento apto(String codigo, int dormitorios, int capacidad, long tarifaBaja) {
        return new Apartamento(codigo, "Apto " + codigo, "A", 1, dormitorios, capacidad, null,
                tarifaBaja, tarifaBaja + 20000, tarifaBaja + 50000);
    }

    static Huesped huesped(String documento) {
        return new Huesped(documento, "Huesped " + documento, LocalDate.of(1990, 5, 20), "3000000", "h@correo.com");
    }

    static ListaDoble<Ocupante> adultos(int cuantos) {
        ListaDoble<Ocupante> lista = new ListaDoble<>();
        for (int i = 1; i <= cuantos; i++) {
            lista.agregarAlFinal(new Ocupante("Adulto " + i, LocalDate.of(1985, 1, i)));
        }
        return lista;
    }

    static Reserva reserva(String codigo, Apartamento a, LocalDate entrada, LocalDate salida) {
        Reserva r = new Reserva(codigo, huesped("100"), a, new Estancia(entrada, salida), adultos(2),
                Canal.DIRECTA, null, 0, LocalDateTime.of(2026, 1, 1, 8, 0));
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
        return r;
    }

    public static void ejecutar() {
        Prueba.titulo("Modelo: estados");
        Prueba.verificar(EstadoApartamento.PREPARADO.puedeCambiarA(EstadoApartamento.OCUPADO), "Preparado -> Ocupado");
        Prueba.verificar(EstadoApartamento.OCUPADO.puedeCambiarA(EstadoApartamento.PENDIENTE_PREPARACION), "Ocupado -> Pendiente");
        Prueba.verificar(EstadoApartamento.PENDIENTE_PREPARACION.puedeCambiarA(EstadoApartamento.EN_PREPARACION), "Pendiente -> En preparacion");
        Prueba.verificar(EstadoApartamento.EN_PREPARACION.puedeCambiarA(EstadoApartamento.PREPARADO), "En preparacion -> Preparado");
        Prueba.verificar(!EstadoApartamento.PREPARADO.puedeCambiarA(EstadoApartamento.PENDIENTE_PREPARACION), "Preparado -> Pendiente no se puede");
        Prueba.verificar(!EstadoApartamento.OCUPADO.puedeCambiarA(EstadoApartamento.FUERA_DE_SERVICIO), "Ocupado -> Fuera de servicio no se puede");
        Prueba.verificar(EstadoApartamento.FUERA_DE_SERVICIO.puedeCambiarA(EstadoApartamento.PENDIENTE_PREPARACION), "Fuera de servicio -> Pendiente");
        Prueba.verificar(EstadoReserva.PENDIENTE.puedeCambiarA(EstadoReserva.CONFIRMADA), "Pendiente -> Confirmada");
        Prueba.verificar(EstadoReserva.CONFIRMADA.puedeCambiarA(EstadoReserva.NO_SHOW), "Confirmada -> No-show");
        Prueba.verificar(!EstadoReserva.FINALIZADA.puedeCambiarA(EstadoReserva.CONFIRMADA), "Finalizada -> Confirmada no se puede");
        Prueba.verificar(EstadoReserva.EN_CURSO.esActiva() && !EstadoReserva.CANCELADA.esActiva(), "esActiva");

        Prueba.titulo("Modelo: Apartamento");
        Apartamento a = apto("A101", 2, 4, 80000);
        Prueba.verificar(a.getEstado() == EstadoApartamento.PREPARADO, "arranca Preparado");
        Prueba.esperarExcepcion(TransicionEstadoInvalidaException.class,
                () -> a.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION), "Preparado a Pendiente da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class, () -> a.ponerFueraDeServicio(""), "fuera de servicio sin motivo da error");
        a.ponerFueraDeServicio("Gotera en el bano");
        Prueba.verificar(a.getEstado() == EstadoApartamento.FUERA_DE_SERVICIO
                && a.getMotivoFueraServicio().equals("Gotera en el bano"), "fuera de servicio guarda el motivo");
        a.cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        Prueba.verificar(a.getMotivoFueraServicio() == null, "al salir de fuera de servicio se borra el motivo");
        Prueba.verificar(a.tarifaPara(Temporada.BAJA) == 80000 && a.tarifaPara(Temporada.ALTA) == 130000, "tarifaPara");
        a.cambiarTarifa(Temporada.MEDIA, 95000);
        Prueba.verificar(a.tarifaPara(Temporada.MEDIA) == 95000, "cambiarTarifa");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new Apartamento("X", "x", "A", 1, 1, 0, null, 1, 1, 1), "capacidad 0 da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new Apartamento("X", "x", "A", 1, 1, 2, null, 0, 1, 1), "precio 0 da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new Apartamento(" ", "x", "A", 1, 1, 2, null, 1, 1, 1), "codigo vacio da error");
        Prueba.verificar(a.compareTo(apto("A102", 1, 2, 1000)) < 0 && a.equals(Apartamento.soloCodigo("A101")),
                "compareTo y equals por codigo");

        Prueba.titulo("Modelo: Estancia, Ocupante y Huesped");
        Estancia marzo = new Estancia(f(3, 10), f(3, 12));
        Prueba.verificar(marzo.noches() == 2, "del 10 al 12 de marzo son 2 noches");
        int contadas = 0;
        LocalDate primera = null;
        LocalDate ultima = null;
        for (LocalDate noche : marzo) {
            if (primera == null) {
                primera = noche;
            }
            ultima = noche;
            contadas++;
        }
        Prueba.verificar(contadas == 2 && primera.equals(f(3, 10)) && ultima.equals(f(3, 11)), "el for-each da el 10 y el 11");
        Prueba.verificar(!marzo.contiene(f(3, 12)) && marzo.contiene(f(3, 11)), "la noche de salida no cuenta");
        Prueba.esperarExcepcion(FechasInvalidasException.class, () -> new Estancia(f(3, 10), f(3, 10)), "salida igual a la entrada da error");
        Prueba.esperarExcepcion(FechasInvalidasException.class, () -> new Estancia(f(3, 10), f(3, 9)), "salida antes de la entrada da error");
        Prueba.verificar(marzo.seCruzaCon(new Estancia(f(3, 11), f(3, 13))), "10-12 se cruza con 11-13");
        Prueba.verificar(!marzo.seCruzaCon(new Estancia(f(3, 12), f(3, 14))), "10-12 no se cruza con 12-14");

        Ocupante nino5 = new Ocupante("Nino", LocalDate.of(2021, 6, 1));
        Ocupante nino6 = new Ocupante("Nina", LocalDate.of(2020, 6, 1));
        Prueba.verificar(!nino5.pagaEn(LocalDate.of(2026, 3, 10)), "un nino de 5 anios no paga");
        Prueba.verificar(nino6.pagaEn(LocalDate.of(2026, 7, 1)), "uno de 6 anios si paga");
        Prueba.verificar(!nino6.pagaEn(LocalDate.of(2026, 5, 31)) && nino6.pagaEn(LocalDate.of(2026, 6, 1)),
                "empieza a pagar el dia que cumple 6");

        Huesped h = huesped("100");
        Prueba.verificar(h.agregarFavorito("A101") && !h.agregarFavorito("A101"), "un favorito repetido da false");
        Prueba.verificar(h.getFavoritos().tamanio() == 1, "los favoritos no se repiten");
        Prueba.verificar(h.quitarFavorito("A101") && !h.quitarFavorito("A101"), "quitar favorito");
        Prueba.verificar(h.getRutaFoto() == null, "la foto es opcional");
        Prueba.verificar(h.equals(Huesped.soloDocumento("100")) && h.compareTo(huesped("200")) < 0, "huesped por documento");

        Prueba.titulo("Modelo: Reserva");
        Reserva r = new Reserva("R1", h, a, marzo, adultos(2), Canal.PORTAL, null, 0, LocalDateTime.of(2026, 3, 1, 8, 0));
        Prueba.verificar(r.getEstado() == EstadoReserva.PENDIENTE, "nace Pendiente");
        Prueba.verificar(r.getVenceEn().equals(LocalDateTime.of(2026, 3, 2, 8, 0)), "vence 24 horas despues");
        Prueba.verificar(r.personas() == 2, "personas");
        Prueba.esperarExcepcion(TransicionEstadoInvalidaException.class,
                () -> r.cambiarEstado(EstadoReserva.FINALIZADA), "Pendiente a Finalizada da error");
        r.cambiarEstado(EstadoReserva.CANCELADA);
        Prueba.verificar(!r.estaActiva(), "cancelada no esta activa");
        Reserva x = reserva("R2", a, f(3, 10), f(3, 12));
        Reserva y = reserva("R3", a, f(3, 20), f(3, 22));
        Prueba.verificar(x.compareTo(y) < 0, "Reserva se compara por fecha de entrada");

        Prueba.titulo("ArbolReservas (solapamientos)");
        Apartamento b = apto("B201", 1, 4, 70000);
        b.getReservas().insertar(reserva("R10", b, f(1, 1), f(1, 5)));
        b.getReservas().insertar(reserva("R11", b, f(1, 10), f(1, 15)));
        b.getReservas().insertar(reserva("R12", b, f(1, 20), f(1, 22)));
        Prueba.verificar(b.getReservas().haySolapamiento(new Estancia(f(1, 14), f(1, 16))), "14-16 choca");
        Prueba.verificar(b.getReservas().reservaQueChoca(new Estancia(f(1, 14), f(1, 16))).getCodigo().equals("R11"),
                "reservaQueChoca devuelve R11");
        Prueba.verificar(!b.getReservas().haySolapamiento(new Estancia(f(1, 5), f(1, 10))), "05-10 no choca");
        Prueba.verificar(b.getReservas().haySolapamiento(new Estancia(f(1, 4), f(1, 6))), "04-06 choca con la primera");
        Prueba.verificar(b.getReservas().haySolapamiento(new Estancia(f(1, 21), f(1, 30))), "21-30 choca con la ultima");
        Reserva cancelada = reserva("R13", b, f(1, 3), f(1, 4));
        b.getReservas().insertar(cancelada);
        Prueba.verificar(b.getReservas().haySolapamiento(new Estancia(f(1, 3), f(1, 4))), "mientras esta activa la tercera noche choca");
        cancelada.cambiarEstado(EstadoReserva.CANCELADA);
        Prueba.verificar(b.getReservas().activas().tamanio() == 3, "activas ignora la cancelada");
        Prueba.verificar(b.getReservas().ocupadaLaNoche(f(1, 12)) && !b.getReservas().ocupadaLaNoche(f(1, 15)),
                "ocupadaLaNoche (la noche de salida queda libre)");
        Prueba.verificar(b.getReservas().reservaLaNoche(f(1, 21)).getCodigo().equals("R12"), "reservaLaNoche");

        Prueba.titulo("ComparadoresApartamento");
        Apartamento c1 = apto("C1", 1, 2, 90000);
        Apartamento c2 = apto("C2", 3, 6, 60000);
        Apartamento c3 = apto("C3", 2, 4, 75000);
        c3.sumarReserva();
        c3.sumarReserva();
        c1.sumarReserva();
        ListaDoble<Apartamento> lista = new ListaDoble<>();
        lista.agregarAlFinal(c1);
        lista.agregarAlFinal(c2);
        lista.agregarAlFinal(c3);
        lista.ordenar(ComparadoresApartamento.POR_PRECIO);
        Prueba.verificar(lista.toString().equals("[C2 - Apto C2 <-> C3 - Apto C3 <-> C1 - Apto C1]"), "por precio: menor primero");
        lista.ordenar(ComparadoresApartamento.POR_CAPACIDAD);
        Prueba.verificar(lista.obtener(0) == c2 && lista.obtener(2) == c1, "por capacidad: mayor primero");
        lista.ordenar(ComparadoresApartamento.POR_DORMITORIOS);
        Prueba.verificar(lista.obtener(0) == c2 && lista.obtener(1) == c3, "por dormitorios: mayor primero");
        lista.ordenar(ComparadoresApartamento.POR_NUMERO_RESERVAS);
        Prueba.verificar(lista.obtener(0) == c3 && lista.obtener(1) == c1 && lista.obtener(2) == c2, "por numero de reservas");
        Apartamento d1 = apto("D1", 1, 2, 50000);
        Apartamento d2 = apto("D2", 1, 2, 50000);
        Prueba.verificar(ComparadoresApartamento.POR_PRECIO.compare(d1, d2) < 0, "empate: desempata por codigo");
    }
}
