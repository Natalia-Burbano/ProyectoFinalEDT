package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ListaCircular;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Empleado;
import co.edu.uniquindio.proyectofinal.modelo.Rol;

/** El turno de recepcion rota entre los recepcionistas (lista circular). */
public class TurnosRecepcion {

    private final ListaCircular<Empleado> turnos = new ListaCircular<>();

    /** Solo acepta empleados con rol RECEPCIONISTA. */
    public void agregarRecepcionista(Empleado empleado) {
        if (empleado == null) {
            throw new IllegalArgumentException("El empleado no puede ser nulo");
        }
        if (empleado.getRol() != Rol.RECEPCIONISTA) {
            throw new IllegalArgumentException("Solo los recepcionistas tienen turno");
        }
        for (Empleado e : turnos) {
            if (e.equals(empleado)) {
                throw new IllegalStateException("Ya esta en los turnos: " + empleado.getDocumento());
            }
        }
        turnos.agregar(empleado);
    }

    /** Quien tiene el turno ahora. Sin recepcionistas: EstructuraVaciaException. */
    public Empleado turnoActual() {
        return turnos.actual();
    }

    /** Pasa el turno al siguiente recepcionista y lo devuelve. */
    public Empleado pasarTurno() {
        return turnos.avanzar();
    }

    public boolean quitar(Empleado empleado) {
        return turnos.eliminar(empleado);
    }

    /** Los recepcionistas, empezando por el que tiene el turno. */
    public ListaDoble<Empleado> recepcionistas() {
        ListaDoble<Empleado> lista = new ListaDoble<>();
        for (Empleado e : turnos) {
            lista.agregarAlFinal(e);
        }
        return lista;
    }
}