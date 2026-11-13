package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;

/** Un bloque del edificio con los apartamentos (nodos del mapa) que tiene. */
public class GrupoBloque {

    private final String bloque;
    private final ListaDoble<NodoMapa> apartamentos = new ListaDoble<>();

    public GrupoBloque(String bloque) {
        this.bloque = bloque;
    }

    public void agregar(NodoMapa apartamento) {
        apartamentos.agregarAlFinal(apartamento);
    }

    public String getBloque() { return bloque; }
    public ListaDoble<NodoMapa> getApartamentos() { return apartamentos; }
    public int cantidad() { return apartamentos.tamanio(); }

    @Override
    public String toString() {
        return "Bloque " + bloque + ": " + apartamentos;
    }
}