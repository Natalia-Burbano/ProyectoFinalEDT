package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.CapacidadExcedidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.servicio.AsignadorGrupos;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/** Pruebas del AsignadorGrupos. */
public class PruebaAsignadorGrupos {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /**
     * Crea un apartamento solo con codigo y capacidad.
     * IMPORTANTE: ajusta esta linea al constructor real de Apartamento de Juan Manuel.
     */
    private static Apartamento crear(String codigo, int capacidad) {
        return new Apartamento(codigo, "Apto " + codigo, "A", 1, 1, capacidad,
                new ListaDoble<String>(), 50000, 60000, 70000);
    }

    /** Casos de prueba del asignador. */
    public static void ejecutar() {
        Prueba.titulo("AsignadorGrupos");

        // capacidades 2, 4, 6 y 8
        ListaDoble<Apartamento> disponibles = new ListaDoble<>();
        disponibles.agregarAlFinal(crear("A1", 2));
        disponibles.agregarAlFinal(crear("A2", 4));
        disponibles.agregarAlFinal(crear("A3", 6));
        disponibles.agregarAlFinal(crear("A4", 8));

        AsignadorGrupos asignador = new AsignadorGrupos();

        // 12 personas: 2 apartamentos
        ListaDoble<Apartamento> doce = asignador.mejorCombinacion(disponibles, 12);
        Prueba.verificar(doce.tamanio() == 2, "12 personas usan 2 apartamentos");
        int suma = 0;
        for (Apartamento a : doce) {
            suma += a.getCapacidad();
        }
        Prueba.verificar(suma >= 12, "la capacidad de la combinacion alcanza para 12");

        // 3 personas: 1 apartamento
        Prueba.verificar(asignador.mejorCombinacion(disponibles, 3).tamanio() == 1,
                "3 personas usan 1 apartamento");

        // 8 personas: 1 apartamento (el de 8)
        Prueba.verificar(asignador.mejorCombinacion(disponibles, 8).tamanio() == 1,
                "8 personas caben en 1 apartamento");

        // 20 personas: hacen falta los 4
        Prueba.verificar(asignador.mejorCombinacion(disponibles, 20).tamanio() == 4,
                "20 personas usan los 4 apartamentos");

        // 30 personas: no caben
        Prueba.esperarExcepcion(CapacidadExcedidaException.class,
                () -> asignador.mejorCombinacion(disponibles, 30),
                "30 personas dan CapacidadExcedidaException");

        // la lista original no se daña
        Prueba.verificar(disponibles.tamanio() == 4, "la lista de disponibles queda igual");

        // sin disponibles
        Prueba.esperarExcepcion(CapacidadExcedidaException.class,
                () -> asignador.mejorCombinacion(new ListaDoble<Apartamento>(), 3),
                "sin apartamentos disponibles da CapacidadExcedidaException");

        // personas invalidas
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> asignador.mejorCombinacion(disponibles, 0), "0 personas da error");
    }
}