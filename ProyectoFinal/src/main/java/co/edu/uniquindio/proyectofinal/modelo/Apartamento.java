package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.proyectofinal.servicio.ArbolReservas;
import java.util.Objects;

/** Un apartamento del alojamiento. Se ordena por codigo (orden natural). */
public class Apartamento implements Comparable<Apartamento> {

    private final String codigo;
    private String nombre;
    private String bloque;
    private int piso;
    private int dormitorios;
    private int capacidad;
    private final ListaDoble<String> caracteristicas;
    private long tarifaBaja;
    private long tarifaMedia;
    private long tarifaAlta;
    private EstadoApartamento estado;
    private String motivoFueraServicio;
    private final ArbolReservas reservas;
    private int numeroReservas;

    /** Crea el apartamento validando los datos. Arranca PREPARADO. */
    public Apartamento(String codigo, String nombre, String bloque, int piso, int dormitorios,
                       int capacidad, ListaDoble<String> caracteristicas,
                       long tarifaBaja, long tarifaMedia, long tarifaAlta) {
        this.codigo = textoObligatorio(codigo, "El codigo");
        this.nombre = textoObligatorio(nombre, "El nombre");
        this.bloque = textoObligatorio(bloque, "El bloque");
        if (piso < 0) {
            throw new IllegalArgumentException("El piso no puede ser negativo");
        }
        if (dormitorios <= 0) {
            throw new IllegalArgumentException("Los dormitorios deben ser mayor a 0");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a 0");
        }
        validarTarifa(tarifaBaja);
        validarTarifa(tarifaMedia);
        validarTarifa(tarifaAlta);
        this.piso = piso;
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.caracteristicas = caracteristicas == null ? new ListaDoble<>() : caracteristicas;
        this.tarifaBaja = tarifaBaja;
        this.tarifaMedia = tarifaMedia;
        this.tarifaAlta = tarifaAlta;
        this.estado = EstadoApartamento.PREPARADO;
        this.reservas = new ArbolReservas();
        this.numeroReservas = 0;
    }

    /** Constructor privado: solo para armar un apartamento "de busqueda" con el codigo. */
    private Apartamento(String codigo) {
        this.codigo = codigo;
        this.caracteristicas = null;
        this.reservas = null;
    }

    /** Crea un apartamento solo con el codigo, para buscarlo en el arbol. */
    public static Apartamento soloCodigo(String codigo) {
        return new Apartamento(textoObligatorio(codigo, "El codigo"));
    }

    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacio");
        }
        return valor.trim();
    }

    private static void validarTarifa(long tarifa) {
        if (tarifa <= 0) {
            throw new IllegalArgumentException("La tarifa debe ser mayor a 0");
        }
    }

    /** Tarifa por persona por noche de esa temporada. */
    public long tarifaPara(Temporada temporada) {
        if (temporada == null) {
            throw new IllegalArgumentException("La temporada no puede ser nula");
        }
        return switch (temporada) {
            case BAJA -> tarifaBaja;
            case MEDIA -> tarifaMedia;
            case ALTA -> tarifaAlta;
        };
    }

    /** Cambia la tarifa de una temporada (debe ser mayor a 0). */
    public void cambiarTarifa(Temporada temporada, long valor) {
        if (temporada == null) {
            throw new IllegalArgumentException("La temporada no puede ser nula");
        }
        validarTarifa(valor);
        switch (temporada) {
            case BAJA -> tarifaBaja = valor;
            case MEDIA -> tarifaMedia = valor;
            case ALTA -> tarifaAlta = valor;
        }
    }

    /** Cambia el estado si la transicion esta permitida; si no, TransicionEstadoInvalidaException. */
    public void cambiarEstado(EstadoApartamento nuevo) {
        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {
            throw new IllegalArgumentException("Para dejarlo fuera de servicio use ponerFueraDeServicio(motivo)");
        }
        if (!estado.puedeCambiarA(nuevo)) {
            throw new TransicionEstadoInvalidaException("El apartamento " + codigo
                    + " no puede pasar de " + estado + " a " + nuevo);
        }
        if (estado == EstadoApartamento.FUERA_DE_SERVICIO) {
            motivoFueraServicio = null;
        }
        estado = nuevo;
    }

    /** Lo deja fuera de servicio. El motivo es obligatorio. */
    public void ponerFueraDeServicio(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio para dejarlo fuera de servicio");
        }
        if (!estado.puedeCambiarA(EstadoApartamento.FUERA_DE_SERVICIO)) {
            throw new TransicionEstadoInvalidaException("El apartamento " + codigo
                    + " no puede pasar de " + estado + " a " + EstadoApartamento.FUERA_DE_SERVICIO);
        }
        estado = EstadoApartamento.FUERA_DE_SERVICIO;
        motivoFueraServicio = motivo.trim();
    }

    /** Suma una reserva al contador (la llama GestorReservas al crear). */
    public void sumarReserva() {
        numeroReservas++;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getBloque() { return bloque; }
    public int getPiso() { return piso; }
    public int getDormitorios() { return dormitorios; }
    public int getCapacidad() { return capacidad; }
    public ListaDoble<String> getCaracteristicas() { return caracteristicas; }
    public long getTarifaBaja() { return tarifaBaja; }
    public long getTarifaMedia() { return tarifaMedia; }
    public long getTarifaAlta() { return tarifaAlta; }
    public EstadoApartamento getEstado() { return estado; }
    public String getMotivoFueraServicio() { return motivoFueraServicio; }
    public ArbolReservas getReservas() { return reservas; }
    public int getNumeroReservas() { return numeroReservas; }

    /** Compara por codigo (orden natural). */
    @Override
    public int compareTo(Apartamento otro) {
        return codigo.compareTo(otro.codigo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Apartamento otro)) {
            return false;
        }
        return codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
