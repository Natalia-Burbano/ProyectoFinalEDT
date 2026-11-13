package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/** Una reserva de un apartamento. Se ordena por fecha de entrada (y por codigo si empatan). */
public class Reserva implements Comparable<Reserva> {

    /** Horas que tiene una reserva pendiente para pagar el anticipo. */
    public static final int HORAS_PARA_VENCER = 24;
    /** Hora de llegada si el huesped no dijo ninguna. */
    public static final LocalTime HORA_POR_DEFECTO = LocalTime.of(15, 0);

    private final String codigo;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final Estancia estancia;
    private final ListaDoble<Ocupante> ocupantes; // incluye al titular
    private final Canal canal;
    private EstadoReserva estado;
    private final LocalDateTime creadaEn;
    private final LocalDateTime venceEn;
    private final LocalTime horaLlegada;
    private long valorTotal;

    /** Crea la reserva en estado PENDIENTE. venceEn = creadaEn + 24 horas. */
    public Reserva(String codigo, Huesped titular, Apartamento apartamento, Estancia estancia,
                   ListaDoble<Ocupante> ocupantes, Canal canal, LocalTime horaLlegada,
                   long valorTotal, LocalDateTime creadaEn) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo de la reserva no puede estar vacio");
        }
        if (titular == null || apartamento == null || estancia == null || canal == null || creadaEn == null) {
            throw new IllegalArgumentException("Faltan datos de la reserva");
        }
        if (ocupantes == null || ocupantes.estaVacia()) {
            throw new IllegalArgumentException("La reserva necesita al menos un ocupante");
        }
        if (valorTotal < 0) {
            throw new IllegalArgumentException("El valor total no puede ser negativo");
        }
        this.codigo = codigo;
        this.titular = titular;
        this.apartamento = apartamento;
        this.estancia = estancia;
        this.ocupantes = ocupantes;
        this.canal = canal;
        this.estado = EstadoReserva.PENDIENTE;
        this.creadaEn = creadaEn;
        this.venceEn = creadaEn.plusHours(HORAS_PARA_VENCER);
        this.horaLlegada = horaLlegada == null ? HORA_POR_DEFECTO : horaLlegada;
        this.valorTotal = valorTotal;
    }

    /** Cambia el estado si el ciclo de vida lo permite; si no, TransicionEstadoInvalidaException. */
    public void cambiarEstado(EstadoReserva nuevo) {
        if (!estado.puedeCambiarA(nuevo)) {
            throw new TransicionEstadoInvalidaException("La reserva " + codigo
                    + " no puede pasar de " + estado + " a " + nuevo);
        }
        estado = nuevo;
    }

    /** Pendiente, Confirmada o En curso. */
    public boolean estaActiva() {
        return estado.esActiva();
    }

    /** Cantidad de personas que se alojan (titular incluido). */
    public int personas() {
        return ocupantes.tamanio();
    }

    public String getCodigo() { return codigo; }
    public Huesped getTitular() { return titular; }
    public Apartamento getApartamento() { return apartamento; }
    public Estancia getEstancia() { return estancia; }
    public ListaDoble<Ocupante> getOcupantes() { return ocupantes; }
    public Canal getCanal() { return canal; }
    public EstadoReserva getEstado() { return estado; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public LocalDateTime getVenceEn() { return venceEn; }
    public LocalTime getHoraLlegada() { return horaLlegada; }
    public long getValorTotal() { return valorTotal; }

    public void setValorTotal(long valorTotal) {
        if (valorTotal < 0) {
            throw new IllegalArgumentException("El valor total no puede ser negativo");
        }
        this.valorTotal = valorTotal;
    }

    /** Compara por fecha de entrada y, si empatan, por codigo. */
    @Override
    public int compareTo(Reserva otra) {
        int c = estancia.getEntrada().compareTo(otra.estancia.getEntrada());
        if (c != 0) {
            return c;
        }
        return codigo.compareTo(otra.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reserva otra)) {
            return false;
        }
        return codigo.equals(otra.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo;
    }
}
