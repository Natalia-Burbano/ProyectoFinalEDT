package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.Conjunto;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import java.time.LocalDate;
import java.util.Objects;

/** Perfil de un huesped. Se ordena por documento (orden natural). */
public class Huesped implements Comparable<Huesped> {

    private final String documento;
    private String nombre;
    private final LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String rutaFoto; // opcional: puede ser null
    private final Conjunto<String> favoritos;
    private final ListaDoble<Reserva> historial;

    /** Crea el huesped. La foto es opcional (puede ser null). */
    public Huesped(String documento, String nombre, LocalDate fechaNacimiento,
                   String telefono, String correo, String rutaFoto) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento no puede estar vacio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        this.documento = documento.trim();
        this.nombre = nombre.trim();
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.correo = correo;
        this.rutaFoto = (rutaFoto == null || rutaFoto.isBlank()) ? null : rutaFoto;
        this.favoritos = new Conjunto<>();
        this.historial = new ListaDoble<>();
    }

    /** Crea el huesped sin foto. */
    public Huesped(String documento, String nombre, LocalDate fechaNacimiento,
                   String telefono, String correo) {
        this(documento, nombre, fechaNacimiento, telefono, correo, null);
    }

    private Huesped(String documento) {
        this.documento = documento;
        this.fechaNacimiento = null;
        this.favoritos = null;
        this.historial = null;
    }

    /** Crea un huesped solo con el documento, para buscarlo en el arbol. */
    public static Huesped soloDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento no puede estar vacio");
        }
        return new Huesped(documento.trim());
    }

    /** Agrega un apartamento a favoritos. Devuelve false si ya estaba. */
    public boolean agregarFavorito(String codigoApartamento) {
        return favoritos.agregar(codigoApartamento);
    }

    /** Quita un apartamento de favoritos. Devuelve false si no estaba. */
    public boolean quitarFavorito(String codigoApartamento) {
        return favoritos.eliminar(codigoApartamento);
    }

    /** Guarda una estancia en el historial del huesped. */
    public void agregarAlHistorial(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        historial.agregarAlFinal(reserva);
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getRutaFoto() { return rutaFoto; }
    public Conjunto<String> getFavoritos() { return favoritos; }
    public ListaDoble<Reserva> getHistorial() { return historial; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        this.nombre = nombre.trim();
    }

    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setCorreo(String correo) { this.correo = correo; }

    public void setRutaFoto(String rutaFoto) {
        this.rutaFoto = (rutaFoto == null || rutaFoto.isBlank()) ? null : rutaFoto;
    }

    /** Compara por documento (orden natural). */
    @Override
    public int compareTo(Huesped otro) {
        return documento.compareTo(otro.documento);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Huesped otro)) {
            return false;
        }
        return documento.equals(otro.documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }

    @Override
    public String toString() {
        return nombre + " (" + documento + ")";
    }
}
