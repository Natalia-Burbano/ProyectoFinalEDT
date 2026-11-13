package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Folio;
import co.edu.uniquindio.proyectofinal.modelo.Movimiento;

import java.time.LocalDateTime;

/**
 * Guarda los folios de todas las reservas en el arbol binario (ordenados por codigo de reserva).
 * Las operaciones las hace cada folio; aqui solo se busca el folio y se le pasa la orden.
 */
public class GestorFolio {

    private final ArbolBinarioBusqueda<Folio> folios;

    /** Crea el gestor sin folios. */
    public GestorFolio() {
        folios = new ArbolBinarioBusqueda<>();
    }

    /**
     * Crea el folio de una reserva. Si ya existe da IllegalStateException.
     */
    public Folio abrirFolio(String codigoReserva) {
        Folio nuevo = new Folio(codigoReserva);
        if (!folios.insertar(nuevo)) {
            throw new IllegalStateException("La reserva " + codigoReserva + " ya tiene folio");
        }
        return nuevo;
    }

    /**
     * Busca el folio de una reserva. Si no existe devuelve null.
     */
    public Folio buscar(String codigoReserva) {
        return folios.buscar(Folio.soloCodigo(codigoReserva));
    }

    /** Registra un cargo en el folio de la reserva. */
    public Movimiento registrarCargo(String codigoReserva, String concepto, long valor, LocalDateTime fechaHora) {
        return folioObligatorio(codigoReserva).registrarCargo(concepto, valor, fechaHora);
    }

    /** Registra un pago en el folio de la reserva. */
    public Movimiento registrarPago(String codigoReserva, String concepto, long valor, LocalDateTime fechaHora) {
        return folioObligatorio(codigoReserva).registrarPago(concepto, valor, fechaHora);
    }

    /** Deshace la ultima operacion del folio (registra el movimiento contrario). */
    public Movimiento deshacerUltima(String codigoReserva, LocalDateTime fechaHora) {
        return folioObligatorio(codigoReserva).deshacerUltima(fechaHora);
    }

    /** Registra la devolucion por cancelacion en el folio. */
    public Movimiento registrarDevolucion(String codigoReserva, long valor, LocalDateTime fechaHora) {
        return folioObligatorio(codigoReserva).registrarDevolucion(valor, fechaHora);
    }

    /**
     * Lo que falta por pagar de esa reserva.
     */
    public long saldo(String codigoReserva) {
        return folioObligatorio(codigoReserva).saldo();
    }

    /**
     * Lo que ya pago esa reserva (descontando correcciones y devoluciones).
     */
    public long totalPagado(String codigoReserva) {
        return folioObligatorio(codigoReserva).totalPagos();
    }

    /**
     * Todos los folios ordenados por codigo de reserva.
     */
    public ListaDoble<Folio> todos() {
        return folios.inorden();
    }

    /** Cuantos folios hay. */
    public int cantidad() {
        return folios.tamanio();
    }

    /**
     * Busca el folio y si no existe da IllegalArgumentException.
     */
    private Folio folioObligatorio(String codigoReserva) {
        Folio folio = buscar(codigoReserva);
        if (folio == null) {
            throw new IllegalArgumentException("La reserva " + codigoReserva + " no tiene folio");
        }
        return folio;
    }
}
