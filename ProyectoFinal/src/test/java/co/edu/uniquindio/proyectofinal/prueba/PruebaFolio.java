package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import co.edu.uniquindio.proyectofinal.modelo.Folio;
import co.edu.uniquindio.proyectofinal.modelo.Movimiento;
import co.edu.uniquindio.proyectofinal.modelo.TipoMovimiento;
import co.edu.uniquindio.proyectofinal.util.Prueba;

import java.time.LocalDateTime;

/**
 * Pruebas del Folio y sus movimientos.
 */
public class PruebaFolio {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /**
     * Casos de prueba del folio.
     */
    public static void ejecutar() {
        Prueba.titulo("Folio");

        LocalDateTime hora = LocalDateTime.of(2026, 3, 10, 15, 0);
        Folio folio = new Folio("R0001");
        Prueba.verificar(folio.saldo() == 0 && folio.cantidadMovimientos() == 0, "un folio nuevo tiene saldo 0 y sin movimientos");

        // cargo y pago
        folio.registrarCargo("Hospedaje", 300000, hora);
        folio.registrarPago("Anticipo", 90000, hora);
        Prueba.verificar(folio.totalCargos() == 300000, "total de cargos 300.000");
        Prueba.verificar(folio.totalPagos() == 90000, "total de pagos 90.000");
        Prueba.verificar(folio.saldo() == 210000, "cargo de 300.000 y pago de 90.000 dejan saldo 210.000");

        // deshacer el pago
        Movimiento correccion = folio.deshacerUltima(hora.plusMinutes(5));
        Prueba.verificar(folio.cantidadMovimientos() == 3, "deshacer el pago deja 3 movimientos (no borra nada)");
        Prueba.verificar(folio.saldo() == 300000, "deshacer el pago deja saldo 300.000");
        Prueba.verificar(correccion.getTipo() == TipoMovimiento.PAGO && correccion.getValor() == -90000,
                "la correccion es un PAGO de -90.000");
        Prueba.verificar(correccion.isEsCorreccion() && correccion.getCorrigeA() == 2,
                "la correccion queda marcada y dice que corrige al movimiento 2");

        // deshacer el cargo
        folio.deshacerUltima(hora.plusMinutes(6));
        Prueba.verificar(folio.cantidadMovimientos() == 4, "deshacer el cargo deja 4 movimientos");
        Prueba.verificar(folio.saldo() == 0, "deshacer el cargo deja saldo 0");

        // ya no hay nada que deshacer: las correcciones no se apilan
        Prueba.esperarExcepcion(EstructuraVaciaException.class,
                () -> folio.deshacerUltima(hora), "deshacer sin operaciones da error (la correccion no se deshace)");

        // valores invalidos
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> folio.registrarCargo("Nada", 0, hora), "un cargo de valor 0 da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> folio.registrarPago("Nada", -500, hora), "un pago negativo da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new Movimiento(1, TipoMovimiento.CARGO, "Raro", -100, hora, false, 0),
                "un movimiento negativo que no es correccion da error");

        // el for-each da los movimientos en orden
        String numeros = "";
        for (Movimiento m : folio) {
            numeros += m.getNumero() + " ";
        }
        Prueba.verificar(numeros.trim().equals("1 2 3 4"), "el for-each del folio da los movimientos en orden: 1 2 3 4");

        // devolucion por cancelacion
        Folio otro = new Folio("R0002");
        otro.registrarCargo("Hospedaje", 200000, hora);
        otro.registrarPago("Anticipo", 60000, hora);
        Movimiento devolucion = otro.registrarDevolucion(30000, hora);
        Prueba.verificar(devolucion.getValor() == -30000 && devolucion.getConcepto().equals("Devolucion por cancelacion"),
                "la devolucion es un pago de -30.000 con su concepto");
        Prueba.verificar(otro.totalPagos() == 30000, "despues de devolver 30.000, lo pagado queda en 30.000");
        otro.deshacerUltima(hora);
        Prueba.verificar(otro.totalPagos() == -30000 && otro.cantidadMovimientos() == 4,
                "deshacer despues de la devolucion deshace el anticipo, no la devolucion");

        // orden natural e igualdad por codigo
        Prueba.verificar(new Folio("R0001").compareTo(new Folio("R0002")) < 0, "R0001 va antes que R0002");
        Prueba.verificar(Folio.soloCodigo("R0001").equals(folio), "soloCodigo sirve para buscar el folio (iguales por codigo)");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> new Folio(""), "un folio sin codigo da error");
    }
}