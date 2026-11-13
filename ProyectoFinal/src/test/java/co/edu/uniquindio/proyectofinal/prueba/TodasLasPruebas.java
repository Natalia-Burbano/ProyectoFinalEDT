package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.util.Prueba;

/**
 * Corre las pruebas de todo el proyecto y al final muestra el resumen.
 * Al cerrar cada etapa se agregan aqui las pruebas nuevas.
 */
public class TodasLasPruebas {

    public static void main(String[] args) {
        // Etapa 1: estructuras
        PruebaListaDoble.ejecutar();
        PruebaPilaCola.ejecutar();
        PruebaColaPrioridad.ejecutar();
        PruebaArbol.ejecutar();
        PruebaConjunto.ejecutar();
        PruebaListaCircular.ejecutar();
        PruebaGrafo.ejecutar();

        // Etapa 2: modelo
        PruebaFolio.ejecutar();
        PruebaTemporadas.ejecutar();
        PruebaModelo.ejecutar();
        PruebaNodoMapa.ejecutar();
        PruebaEmpleado.ejecutar();

        // Etapa 3: servicios
        PruebaCotizador.ejecutar();
        PruebaGestores.ejecutar();
        PruebaReservas.ejecutar();
        PruebaMapaEdificio.ejecutar();
        PruebaAsignadorGrupos.ejecutar();
        PruebaTurnosYEmpleados.ejecutar();

        // Etapa 4: se agregan al cerrar la etapa

        Prueba.resumen();
    }
}