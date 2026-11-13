package co.edu.uniquindio.proyectofinal.modelo;

/**
 * Un punto del mapa del edificio (apartamento, recepcion, escalera o aseo).
 * x, y es la posicion para dibujarlo en el mapa.
 */
public class NodoMapa {

    private final String id;
    private final TipoNodo tipo;
    private final String nombre;
    private final String bloque;
    private final int piso;
    private final double x;
    private final double y;
    private final String codigoApartamento;   // solo si el tipo es APARTAMENTO

    public NodoMapa(String id, TipoNodo tipo, String nombre, String bloque,
                    int piso, double x, double y, String codigoApartamento) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del nodo no puede estar vacio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo del nodo no puede ser nulo");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del nodo no puede estar vacio");
        }
        if (tipo == TipoNodo.APARTAMENTO
                && (codigoApartamento == null || codigoApartamento.isBlank())) {
            throw new IllegalArgumentException("Un nodo de tipo APARTAMENTO necesita su codigo");
        }
        this.id = id;
        this.tipo = tipo;
        this.nombre = nombre;
        this.bloque = bloque;
        this.piso = piso;
        this.x = x;
        this.y = y;
        // Si no es un apartamento, no guarda codigo
        this.codigoApartamento = (tipo == TipoNodo.APARTAMENTO) ? codigoApartamento : null;
    }

    public String getId() { return id; }
    public TipoNodo getTipo() { return tipo; }
    public String getNombre() { return nombre; }
    public String getBloque() { return bloque; }
    public int getPiso() { return piso; }
    public double getX() { return x; }
    public double getY() { return y; }
    public String getCodigoApartamento() { return codigoApartamento; }

    /** Iguales si tienen el mismo id. */
    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof NodoMapa)) {
            return false;
        }
        return id.equals(((NodoMapa) otro).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** Devuelve el nombre (asi se ve en las rutas y en los combos). */
    @Override
    public String toString() {
        return nombre;
    }
}