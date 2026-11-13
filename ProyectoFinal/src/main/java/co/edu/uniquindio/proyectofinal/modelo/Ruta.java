package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;

/** Un camino por el edificio: los nodos en orden y la distancia total en metros. */
public class Ruta {

    private final ListaDoble<NodoMapa> nodos;
    private final double distancia;

    public Ruta(ListaDoble<NodoMapa> nodos, double distancia) {
        if (nodos == null) {
            throw new IllegalArgumentException("Los nodos de la ruta no pueden ser nulos");
        }
        this.nodos = nodos;
        this.distancia = distancia;
    }

    public ListaDoble<NodoMapa> getNodos() { return nodos; }
    public double getDistancia() { return distancia; }

    /** Formato: REC -> ESC-A -> A201 (14 m) */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean primero = true;
        for (NodoMapa nodo : nodos) {
            if (!primero) {
                sb.append(" -> ");
            }
            sb.append(nodo.getId());
            primero = false;
        }
        sb.append(" (");
        if (distancia == Math.rint(distancia)) {
            sb.append((long) distancia);          // 14 en vez de 14.0
        } else {
            sb.append(distancia);
        }
        return sb.append(" m)").toString();
    }
}
