package co.edu.uniquindio.proyectofinal.prueba;

import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.EstructuraVaciaException;
import co.edu.uniquindio.proyectofinal.modelo.Empleado;
import co.edu.uniquindio.proyectofinal.modelo.Rol;
import co.edu.uniquindio.proyectofinal.servicio.RegistroEmpleados;
import co.edu.uniquindio.proyectofinal.servicio.TurnosRecepcion;
import co.edu.uniquindio.proyectofinal.util.Prueba;

/** Pruebas de TurnosRecepcion y RegistroEmpleados. */
public class PruebaTurnosYEmpleados {

    public static void main(String[] args) {
        ejecutar();
        Prueba.resumen();
    }

    /** Casos de prueba de turnos y registro de empleados. */
    public static void ejecutar() {
        Empleado r1 = new Empleado("300", "Carolina", Rol.RECEPCIONISTA, "3101110001");
        Empleado r2 = new Empleado("100", "Mauricio", Rol.RECEPCIONISTA, "3101110002");
        Empleado r3 = new Empleado("200", "Natalia", Rol.RECEPCIONISTA, "3101110003");
        Empleado admin = new Empleado("400", "Gustavo", Rol.ADMINISTRADOR, "3101110006");

        // ---------- turnos ----------
        Prueba.titulo("TurnosRecepcion");
        TurnosRecepcion turnos = new TurnosRecepcion();
        Prueba.esperarExcepcion(EstructuraVaciaException.class, () -> turnos.turnoActual(),
                "sin recepcionistas, turnoActual da EstructuraVaciaException");

        turnos.agregarRecepcionista(r1);
        turnos.agregarRecepcionista(r2);
        turnos.agregarRecepcionista(r3);
        Prueba.verificar(turnos.turnoActual().equals(r1), "el primer turno es del primero que se agrego");
        Prueba.verificar(turnos.pasarTurno().equals(r2), "pasarTurno 1 da el segundo");
        Prueba.verificar(turnos.pasarTurno().equals(r3), "pasarTurno 2 da el tercero");
        Prueba.verificar(turnos.pasarTurno().equals(r1), "pasarTurno 3 vuelve al primero");
        Prueba.verificar(turnos.recepcionistas().tamanio() == 3, "hay 3 recepcionistas");

        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> turnos.agregarRecepcionista(admin), "un administrador no puede tener turno");
        Prueba.esperarExcepcion(IllegalStateException.class,
                () -> turnos.agregarRecepcionista(r2), "un recepcionista repetido da error");
        Prueba.esperarExcepcion(IllegalArgumentException.class,
                () -> turnos.agregarRecepcionista(null), "agregar null da error");

        Prueba.verificar(turnos.quitar(r1), "quitar el que tiene el turno");
        Prueba.verificar(turnos.turnoActual().equals(r2), "el turno pasa al siguiente");
        Prueba.verificar(!turnos.quitar(admin), "quitar uno que no esta da false");

        // ---------- registro ----------
        Prueba.titulo("RegistroEmpleados");
        RegistroEmpleados registro = new RegistroEmpleados();
        Prueba.verificar(registro.cantidad() == 0, "un registro nuevo esta vacio");

        registro.registrar(r1);   // 300
        registro.registrar(r2);   // 100
        registro.registrar(r3);   // 200
        registro.registrar(admin); // 400
        Prueba.verificar(registro.cantidad() == 4, "hay 4 empleados");

        Prueba.verificar(registro.buscar("200").getNombre().equals("Natalia"), "buscar por documento");
        Prueba.verificar(registro.buscar("999") == null, "buscar uno que no existe da null");
        Prueba.esperarExcepcion(IllegalStateException.class,
                () -> registro.registrar(new Empleado("200", "Otra", Rol.PREPARACION, null)),
                "documento repetido da IllegalStateException");

        ListaDoble<Empleado> todos = registro.todos();
        Prueba.verificar(todos.obtener(0).getDocumento().equals("100")
                        && todos.obtener(1).getDocumento().equals("200")
                        && todos.obtener(2).getDocumento().equals("300")
                        && todos.obtener(3).getDocumento().equals("400"),
                "todos() los devuelve ordenados por documento");
        Prueba.verificar(registro.altura() >= 3, "la altura del arbol es al menos 3");

        Prueba.verificar(registro.eliminar("100"), "eliminar un empleado");
        Prueba.verificar(registro.buscar("100") == null && registro.cantidad() == 3,
                "ya no esta y quedan 3");
        Prueba.verificar(!registro.eliminar("100"), "eliminar uno que no existe da false");
    }
}