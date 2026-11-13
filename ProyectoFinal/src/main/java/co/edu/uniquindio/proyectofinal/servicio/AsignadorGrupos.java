package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;

/** Reparte un grupo grande en el menor numero de apartamentos (backtracking). */
public class AsignadorGrupos {

    // La mejor combinacion encontrada hasta ahora (null si todavia no hay ninguna)
    private ListaDoble<Apartamento> mejor;

    /**
     * Busca la combinacion de apartamentos que aloja a todas las personas usando
     * el menor numero de apartamentos. Si ninguna alcanza: CapacidadExcedidaException.
     */
    public ListaDoble<Apartamento> mejorCombinacion(ListaDoble<Apartamento> disponibles, int personas) {
        if (disponibles == null) {
            throw new IllegalArgumentException("La lista de disponibles no puede ser nula");
        }
        if (personas <= 0) {
            throw new IllegalArgumentException("Las personas deben ser mas de 0");
        }
        // Pasamos a un arreglo para poder ir apartamento por apartamento con un indice
        Apartamento[] aptos = new Apartamento[disponibles.tamanio()];
        int i = 0;
        for (Apartamento apto : disponibles) {
            aptos[i] = apto;
            i++;
        }

        mejor = null;
        buscar(aptos, 0, 0, personas, new ListaDoble<Apartamento>());

        if (mejor == null) {
            throw new CapacidadExcedidaException(
                    "Ninguna combinacion de apartamentos aloja a " + personas + " personas");
        }
        ListaDoble<Apartamento> resultado = mejor;
        mejor = null;
        return resultado;
    }

    private void buscar(Apartamento[] aptos, int indice, int capacidadActual,
                        int personas, ListaDoble<Apartamento> actual) {
        // PODA: si ya usamos tantos apartamentos como la mejor, esta rama no puede mejorarla
        if (mejor != null && actual.tamanio() >= mejor.tamanio()) {
            return;
        }
        // CASO BASE 1: ya alcanza la capacidad -> guardar una copia como la mejor
        if (capacidadActual >= personas) {
            mejor = actual.copia();
            return;
        }
        // CASO BASE 2: no quedan apartamentos por probar
        if (indice == aptos.length) {
            return;
        }

        // ELEGIR: probar CON este apartamento
        actual.agregarAlFinal(aptos[indice]);
        buscar(aptos, indice + 1, capacidadActual + aptos[indice].getCapacidad(), personas, actual);

        // DESHACER: sacarlo para probar la otra opcion
        actual.eliminarEn(actual.tamanio() - 1);

        // Probar SIN este apartamento
        buscar(aptos, indice + 1, capacidadActual, personas, actual);
    }
}