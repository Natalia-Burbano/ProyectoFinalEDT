package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.estructuras.Pila;
import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;

import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * El folio es la cuenta de una reserva: guarda sus cargos y sus pagos.
 * Los movimientos nunca se borran. Deshacer no quita nada: registra el movimiento contrario.
 */
public class Folio implements Comparable<Folio>, Iterable<Movimiento> {

    private final String codigoReserva;
    private final ListaDoble<Movimiento> movimientos; // todos, en el orden en que se registraron
    private final Pila<Movimiento> operaciones;       // cargos y pagos que se pueden deshacer
    private int consecutivo;                          // para numerar los movimientos

    /**
     * Crea el folio vacio de una reserva.
     */
    public Folio(String codigoReserva) {
        if (codigoReserva == null || codigoReserva.isBlank()) {
            throw new IllegalArgumentException("El codigo de la reserva es obligatorio");
        }
        this.codigoReserva = codigoReserva;
        this.movimientos = new ListaDoble<>();
        this.operaciones = new Pila<>();
        this.consecutivo = 0;
    }

    /**
     * Crea un folio solo con el codigo. Sirve para buscarlo en el arbol.
     */
    public static Folio soloCodigo(String codigoReserva) {
        return new Folio(codigoReserva);
    }

    /**
     * Registra un cargo. El valor debe ser mayor que 0. Se guarda en la lista y se apila.
     */
    public Movimiento registrarCargo(String concepto, long valor, LocalDateTime fechaHora) {
        return registrarOperacion(TipoMovimiento.CARGO, concepto, valor, fechaHora);
    }

    /**
     * Registra un pago. El valor debe ser mayor que 0. Se guarda en la lista y se apila.
     */
    public Movimiento registrarPago(String concepto, long valor, LocalDateTime fechaHora) {
        return registrarOperacion(TipoMovimiento.PAGO, concepto, valor, fechaHora);
    }

    /**
     * Deshace la ultima operacion: la desapila y registra el movimiento contrario
     * (mismo tipo y valor negativo). La correccion no se apila, asi que deshacer no se deshace.
     */
    public Movimiento deshacerUltima(LocalDateTime fechaHora) {
        if (operaciones.estaVacia()) {
            throw new EstructuraVaciaException("No hay operaciones para deshacer");
        }

        Movimiento ultimo = operaciones.desapilar();
        consecutivo++;
        Movimiento contrario = new Movimiento(consecutivo, ultimo.getTipo(),
                "Correccion de: " + ultimo.getConcepto(), -ultimo.getValor(),
                fechaHora, true, ultimo.getNumero());
        movimientos.agregarAlFinal(contrario);
        return contrario;
    }

    /**
     * Registra una devolucion al cancelar: un pago negativo con el concepto "Devolucion por cancelacion".
     * Se marca como correccion porque baja lo pagado, y no se apila (no se puede deshacer).
     */
    public Movimiento registrarDevolucion(long valor, LocalDateTime fechaHora) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El valor a devolver debe ser mayor que 0");
        }

        consecutivo++;
        Movimiento devolucion = new Movimiento(consecutivo, TipoMovimiento.PAGO,
                "Devolucion por cancelacion", -valor, fechaHora, true, 0);
        movimientos.agregarAlFinal(devolucion);
        return devolucion;
    }

    /**
     * Suma de todos los cargos (las correcciones de cargos restan solas porque son negativas).
     */
    public long totalCargos() {
        return sumar(TipoMovimiento.CARGO);
    }

    /**
     * Suma de todos los pagos (las correcciones y devoluciones restan solas porque son negativas).
     */
    public long totalPagos() {
        return sumar(TipoMovimiento.PAGO);
    }

    /**
     * Lo que falta por pagar: cargos menos pagos.
     */
    public long saldo() {
        return totalCargos() - totalPagos();
    }

    /**
     * Cuantos movimientos tiene el folio, contando las correcciones.
     */
    public int cantidadMovimientos() {
        return movimientos.tamanio();
    }

    public String getCodigoReserva() {
        return codigoReserva;
    }

    /**
     * Recorre los movimientos en el orden en que se registraron.
     */
    @Override
    public Iterator<Movimiento> iterator() {
        return movimientos.iterator();
    }

    /**
     * Orden natural: por codigo de la reserva.
     */
    @Override
    public int compareTo(Folio otro) {
        return this.codigoReserva.compareTo(otro.codigoReserva);
    }

    /**
     * Dos folios son iguales si son de la misma reserva.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Folio)) {
            return false;
        }
        Folio otro = (Folio) o;
        return codigoReserva.equals(otro.codigoReserva);
    }

    @Override
    public int hashCode() {
        return codigoReserva.hashCode();
    }

    @Override
    public String toString() {
        return "Folio " + codigoReserva + " (saldo " + saldo() + ")";
    }

    // ------------------ metodos privados de ayuda ------------------

    /**
     * Crea el cargo o pago, lo guarda en la lista y lo apila para poder deshacerlo.
     */
    private Movimiento registrarOperacion(TipoMovimiento tipo, String concepto, long valor, LocalDateTime fechaHora) {
        if (valor <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor que 0");
        }

        consecutivo++;
        Movimiento movimiento = new Movimiento(consecutivo, tipo, concepto, valor, fechaHora, false, 0);
        movimientos.agregarAlFinal(movimiento);
        operaciones.apilar(movimiento);
        return movimiento;
    }

    /**
     * Suma los valores de los movimientos de un tipo.
     */
    private long sumar(TipoMovimiento tipo) {
        long total = 0;
        for (Movimiento m : movimientos) {
            if (m.getTipo() == tipo) {
                total += m.getValor();
            }
        }
        return total;
    }
}