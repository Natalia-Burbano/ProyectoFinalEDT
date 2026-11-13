package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Una solicitud de reserva que manda la plataforma de terceros (simulada).
 * Llegan a una cola y se procesan en orden de llegada.
 */
public class SolicitudExterna {

    private final String plataforma;
    private final String documentoHuesped;
    private final String codigoApartamento;
    private final LocalDate entrada;
    private final LocalDate salida;
    private final ListaDoble<Ocupante> ocupantes;
    private final LocalTime horaLlegada;
    private final LocalDateTime recibidaEn;

    /**
     * Crea la solicitud. Todos los datos son obligatorios menos la hora de llegada
     * y los acompanantes (si viene solo el titular, la lista queda vacia).
     */
    public SolicitudExterna(String plataforma, String documentoHuesped, String codigoApartamento,
                            LocalDate entrada, LocalDate salida, ListaDoble<Ocupante> ocupantes,
                            LocalTime horaLlegada, LocalDateTime recibidaEn) {
        if (plataforma == null || plataforma.isBlank() || documentoHuesped == null || documentoHuesped.isBlank()
                || codigoApartamento == null || codigoApartamento.isBlank()) {
            throw new IllegalArgumentException("La plataforma, el documento y el apartamento son obligatorios");
        }
        if (entrada == null || salida == null || recibidaEn == null) {
            throw new IllegalArgumentException("Las fechas de la solicitud son obligatorias");
        }
        this.plataforma = plataforma;
        this.documentoHuesped = documentoHuesped;
        this.codigoApartamento = codigoApartamento;
        this.entrada = entrada;
        this.salida = salida;
        // son los acompanantes: el titular lo agrega el sistema solo, asi que la lista puede venir vacia
        this.ocupantes = ocupantes == null ? new ListaDoble<>() : ocupantes;
        this.horaLlegada = horaLlegada;
        this.recibidaEn = recibidaEn;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public String getDocumentoHuesped() {
        return documentoHuesped;
    }

    public String getCodigoApartamento() {
        return codigoApartamento;
    }

    public LocalDate getEntrada() {
        return entrada;
    }

    public LocalDate getSalida() {
        return salida;
    }

    public ListaDoble<Ocupante> getOcupantes() {
        return ocupantes;
    }

    public LocalTime getHoraLlegada() {
        return horaLlegada;
    }

    public LocalDateTime getRecibidaEn() {
        return recibidaEn;
    }

    @Override
    public String toString() {
        return plataforma + ": " + codigoApartamento + " del " + entrada + " al " + salida
                + " (" + documentoHuesped + ")";
    }
}
