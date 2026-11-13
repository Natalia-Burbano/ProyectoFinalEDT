package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.CalendarioTemporadas;
import co.edu.uniquindio.proyectofinal.modelo.Cotizacion;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.NocheLiquidada;
import co.edu.uniquindio.proyectofinal.modelo.Ocupante;
import co.edu.uniquindio.proyectofinal.modelo.Temporada;
import co.edu.uniquindio.proyectofinal.servicio.Cotizador;
import co.edu.uniquindio.proyectofinal.util.Prueba;

import java.time.LocalDate;

/**
 * Pruebas del Cotizador (cobro recursivo noche por noche).
 */
public class PruebaCotizador {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /**
     * Casos de prueba de la cotizacion.
     */
    public static void ejecutar() {
        Prueba.titulo("Cotizador");

        Cotizador cotizador = new Cotizador();
        // tarifas por persona por noche: baja 80.000, media 100.000, alta 150.000
        Apartamento apto = new Apartamento("A101", "Apto 101", "A", 1, 2, 4, null, 80000, 100000, 150000);
        CalendarioTemporadas calendario = new CalendarioTemporadas();
        calendario.definirPeriodo(Temporada.ALTA, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15));

        // 3 personas: dos adultos y un nino de 4 anios
        ListaDoble<Ocupante> familia = new ListaDoble<>();
        familia.agregarAlFinal(new Ocupante("Ana", LocalDate.of(1990, 1, 1)));
        familia.agregarAlFinal(new Ocupante("Luis", LocalDate.of(1988, 6, 1)));
        familia.agregarAlFinal(new Ocupante("Sofi", LocalDate.of(2021, 8, 1)));

        // del 10 al 12 de marzo: 2 noches en temporada baja
        Estancia marzo = new Estancia(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12));
        Cotizacion c1 = cotizador.cotizar(apto, marzo, familia, calendario);
        Prueba.verificar(c1.getDesglose().tamanio() == 2, "del 10 al 12 de marzo son 2 noches");
        Prueba.verificar(c1.getTotal() == 320000, "2 noches x 2 que pagan x 80.000 = 320.000 (el de 4 anios no paga)");
        Prueba.verificar(c1.getAnticipo() == 96000, "el anticipo del 30 % es 96.000");
        NocheLiquidada primera = c1.getDesglose().obtener(0);
        Prueba.verificar(primera.getFecha().equals(LocalDate.of(2026, 3, 10)) && primera.getPersonasQuePagan() == 2,
                "la primera noche del desglose es el 10 de marzo con 2 que pagan");

        // estancia que cruza de baja a alta: del 13 al 17 de diciembre
        // noches 13 y 14 son BAJA, noches 15 y 16 son ALTA
        ListaDoble<Ocupante> uno = new ListaDoble<>();
        uno.agregarAlFinal(new Ocupante("Pedro", LocalDate.of(1980, 2, 2)));
        Estancia diciembre = new Estancia(LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 17));
        Cotizacion c2 = cotizador.cotizar(apto, diciembre, uno, calendario);
        Prueba.verificar(c2.getDesglose().obtener(1).getTemporada() == Temporada.BAJA
                        && c2.getDesglose().obtener(2).getTemporada() == Temporada.ALTA,
                "la noche del 14 es BAJA y la del 15 es ALTA");
        Prueba.verificar(c2.getTotal() == 80000 + 80000 + 150000 + 150000,
                "cruzar de baja a alta cobra cada noche con su tarifa: 460.000");

        // un nino que cumple 6 durante la estancia: nace el 11 de marzo de 2020
        ListaDoble<Ocupante> cumple = new ListaDoble<>();
        cumple.agregarAlFinal(new Ocupante("Marta",LocalDate.of(1992, 4, 4)));
        cumple.agregarAlFinal(new Ocupante("Tomas", LocalDate.of(2020, 3, 11)));
        Cotizacion c3 = cotizador.cotizar(apto, marzo, cumple, calendario);
        Prueba.verificar(c3.getDesglose().obtener(0).getPersonasQuePagan() == 1,
                "la noche del 10 (todavia tiene 5) paga solo 1 persona");
        Prueba.verificar(c3.getDesglose().obtener(1).getPersonasQuePagan() == 2,
                "la noche del 11 (ya cumplio 6) pagan 2 personas");
        Prueba.verificar(c3.getTotal() == 80000 + 160000, "el nino empieza a pagar desde la noche de su cumpleanios");

        // capacidad excedida: 5 personas en un apartamento para 4
        ListaDoble<Ocupante> muchos = new ListaDoble<>();
        for (int i = 1; i <= 5; i++) {
            muchos.agregarAlFinal(new Ocupante("Persona " + i, LocalDate.of(1990, 1, i)));
        }
        Prueba.esperarExcepcion(CapacidadExcedidaException.class,
                () -> cotizador.cotizar(apto, marzo, muchos, calendario), "5 personas en capacidad 4 da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> cotizador.cotizar(apto, marzo, new ListaDoble<>(), calendario), "cotizar sin ocupantes da error");
    }
}
