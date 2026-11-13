package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.CalendarioTemporadas;
import co.edu.uniquindio.proyectofinal.modelo.Cotizacion;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.NocheLiquidada;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Temporada;

import java.time.LocalDate;
import java.util.Iterator;

/**
 * Calcula cuanto vale una estancia, noche por noche.
 * Se cobra por persona y por noche segun la temporada de cada noche;
 * los menores de 6 anios no pagan.
 */
public class Cotizador {

    /**
     * Cotiza la estancia en el apartamento con esos ocupantes.
     * Si son mas personas que la capacidad da CapacidadExcedidaException.
     */
    public Cotizacion cotizar(Apartamento apartamento, Estancia estancia, ListaDoble<Ocupante> ocupantes,
                              CalendarioTemporadas calendario) {
        if (apartamento == null || estancia == null || calendario == null) {
            throw new IllegalArgumentException("Faltan datos para cotizar");
        }
        if (ocupantes == null || ocupantes.estaVacia()) {
            throw new IllegalArgumentException("Hay que decir quienes se alojan");
        }
        if (ocupantes.tamanio() > apartamento.getCapacidad()) {
            throw new CapacidadExcedidaException("Son " + ocupantes.tamanio() + " personas y el apartamento "
                    + apartamento.getCodigo() + " tiene capacidad para " + apartamento.getCapacidad());
        }

        ListaDoble<NocheLiquidada> desglose = new ListaDoble<>();
        long total = liquidar(estancia.iterator(), apartamento, ocupantes, calendario, desglose);
        return new Cotizacion(desglose, total);
    }

    /**
     * Suma el valor de las noches que quedan en el iterador, de forma recursiva.
     * En cada llamada se cobra una noche y se agrega al desglose.
     */
    private long liquidar(Iterator<LocalDate> noches, Apartamento apartamento, ListaDoble<Ocupante> ocupantes,
                          CalendarioTemporadas calendario, ListaDoble<NocheLiquidada> desglose) {
        // CASO BASE: ya no quedan noches por cobrar
        if (!noches.hasNext()) {
            return 0;
        }

        // CASO RECURSIVO: se cobra esta noche y se suma lo que valen las demas
        LocalDate noche = noches.next();
        Temporada temporada = calendario.temporadaDe(noche);
        long tarifa = apartamento.tarifaPara(temporada);
        int pagan = contarQuienesPagan(ocupantes, noche);

        NocheLiquidada liquidada = new NocheLiquidada(noche, temporada, tarifa, pagan);
        desglose.agregarAlFinal(liquidada);

        return liquidada.getValor() + liquidar(noches, apartamento, ocupantes, calendario, desglose);
    }

    /**
     * Cuenta cuantos ocupantes pagan esa noche (los que ya tienen 6 anios o mas ese dia).
     */
    private int contarQuienesPagan(ListaDoble<Ocupante> ocupantes, LocalDate noche) {
        int pagan = 0;
        for (Ocupante o : ocupantes) {
            if (o.pagaEn(noche)) {
                pagan++;
            }
        }
        return pagan;
    }
}
