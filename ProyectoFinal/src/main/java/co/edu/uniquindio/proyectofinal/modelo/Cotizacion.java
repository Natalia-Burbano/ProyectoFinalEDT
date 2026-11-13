package co.edu.uniquindio.proyectofinal.modelo;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;

/**
 * Resultado de cotizar una estancia: el desglose noche por noche, el total y el anticipo del 30 %.
 */
public class Cotizacion {

    /** Porcentaje del anticipo que se paga por el portal. */
    public static final int PORCENTAJE_ANTICIPO = 30;

    private final ListaDoble<NocheLiquidada> desglose;
    private final long total;

    /**
     * Crea la cotizacion con su desglose y el total.
     */
    public Cotizacion(ListaDoble<NocheLiquidada> desglose, long total) {
        if (desglose == null) {
            throw new IllegalArgumentException("El desglose es obligatorio");
        }
        this.desglose = desglose;
        this.total = total;
    }

    public ListaDoble<NocheLiquidada> getDesglose() {
        return desglose;
    }

    public long getTotal() {
        return total;
    }

    /**
     * El 30 % del total (en pesos, sin decimales).
     */
    public long getAnticipo() {
        return total * PORCENTAJE_ANTICIPO / 100;
    }

    @Override
    public String toString() {
        return "Total " + total + " (anticipo " + getAnticipo() + ")";
    }
}
