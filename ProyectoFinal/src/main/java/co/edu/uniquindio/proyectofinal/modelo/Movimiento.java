package co.edu.uniquindio.proyectofinal.modelo;

import java.time.LocalDateTime;

/**
 * Un movimiento del folio: un cargo o un pago.
 * No tiene setters porque un movimiento nunca se cambia ni se borra.
 * Si hay que corregir algo se registra otro movimiento contrario (una correccion).
 */
public class Movimiento {

    private final int numero;              // consecutivo dentro del folio (1, 2, 3...)
    private final TipoMovimiento tipo;     // CARGO o PAGO
    private final String concepto;         // por ejemplo "Hospedaje" o "Anticipo"
    private final long valor;              // en pesos
    private final LocalDateTime fechaHora; // cuando se registro
    private final boolean esCorreccion;    // true si corrige o devuelve algo
    private final int corrigeA;            // numero del movimiento que corrige, 0 si no corrige ninguno

    /**
     * Crea un movimiento. Solo una correccion puede tener valor negativo.
     */
    public Movimiento(int numero, TipoMovimiento tipo, String concepto, long valor,
                      LocalDateTime fechaHora, boolean esCorreccion, int corrigeA) {
        if (tipo == null || fechaHora == null) {
            throw new IllegalArgumentException("El tipo y la fecha del movimiento son obligatorios");
        }
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto del movimiento es obligatorio");
        }
        if (valor == 0) {
            throw new IllegalArgumentException("El valor del movimiento no puede ser 0");
        }
        if (valor < 0 && !esCorreccion) {
            throw new IllegalArgumentException("Solo una correccion puede tener valor negativo");
        }

        this.numero = numero;
        this.tipo = tipo;
        this.concepto = concepto;
        this.valor = valor;
        this.fechaHora = fechaHora;
        this.esCorreccion = esCorreccion;
        this.corrigeA = corrigeA;
    }

    public int getNumero() {
        return numero;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public String getConcepto() {
        return concepto;
    }

    public long getValor() {
        return valor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public boolean isEsCorreccion() {
        return esCorreccion;
    }

    public int getCorrigeA() {
        return corrigeA;
    }

    /**
     * Muestra el movimiento, por ejemplo: #1 CARGO Hospedaje 300000
     */
    @Override
    public String toString() {
        return "#" + numero + " " + tipo + " " + concepto + " " + valor;
    }
}