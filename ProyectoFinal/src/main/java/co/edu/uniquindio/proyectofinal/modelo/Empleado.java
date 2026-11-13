package co.edu.uniquindio.proyectofinal.modelo;


/** Un empleado del alojamiento. Se compara por documento. */
public class Empleado implements Comparable<Empleado> {

    private final String documento;
    private final String nombre;
    private final Rol rol;
    private final String telefono;

    public Empleado(String documento, String nombre, Rol rol, String telefono) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento no puede estar vacio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
        this.documento = documento;
        this.nombre = nombre;
        this.rol = rol;
        this.telefono = telefono;
    }

    // Constructor privado: solo lo usa soloDocumento
    private Empleado(String documento) {
        this.documento = documento;
        this.nombre = null;
        this.rol = null;
        this.telefono = null;
    }

    /** Crea un empleado solo con el documento, para buscarlo en el arbol. */
    public static Empleado soloDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento no puede estar vacio");
        }
        return new Empleado(documento);
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public Rol getRol() { return rol; }
    public String getTelefono() { return telefono; }

    @Override
    public int compareTo(Empleado otro) {
        return documento.compareTo(otro.documento);
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Empleado)) {
            return false;
        }
        return documento.equals(((Empleado) otro).documento);
    }

    @Override
    public int hashCode() {
        return documento.hashCode();
    }

    @Override
    public String toString() {
        return documento + " - " + nombre;
    }
}