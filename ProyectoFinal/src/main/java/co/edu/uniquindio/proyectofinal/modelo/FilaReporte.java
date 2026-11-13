package co.edu.uniquindio.proyectofinal.modelo;

/** Una fila de un reporte: una etiqueta y un valor (para las tablas y graficos). */
public class FilaReporte {

    private final String etiqueta;
    private final double valor;

    public FilaReporte(String etiqueta, double valor) {
        if (etiqueta == null) {
            throw new IllegalArgumentException("La etiqueta no puede ser nula");
        }
        this.etiqueta = etiqueta;
        this.valor = valor;
    }

    public String getEtiqueta() { return etiqueta; }
    public double getValor() { return valor; }

    /** El valor para mostrar: sin decimales si es entero, con uno si no. */
    public String getValorTexto() {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(Math.round(valor * 10) / 10.0);
    }

    @Override
    public String toString() {
        return etiqueta + ": " + getValorTexto();
    }
}