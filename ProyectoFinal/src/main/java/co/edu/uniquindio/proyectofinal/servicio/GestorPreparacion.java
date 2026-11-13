package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.comparadores.ComparadoresPreparacion;
import co.edu.uniquindio.proyectofinal.estructuras.ColaPrioridad;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.excepciones.TransicionEstadoInvalidaException;
import co.edu.uniquindio.proyectofinal.modelo.Apartamento;
import co.edu.uniquindio.proyectofinal.modelo.EstadoApartamento;
import co.edu.uniquindio.proyectofinal.modelo.TareaPreparacion;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Maneja la cola de preparacion de apartamentos.
 * Primero salen los que tienen una entrada hoy (por hora de llegada) y despues los demas.
 */
public class GestorPreparacion {

    private final ColaPrioridad<TareaPreparacion> cola;        // los que esperan a ser preparados
    private final ListaDoble<TareaPreparacion> enPreparacion;  // los que el personal esta preparando
    private long minutosTotales;                               // suma de los tiempos de preparacion
    private int preparadosTerminados;                          // cuantos se han terminado

    /** Crea la cola de preparacion vacia. */
    public GestorPreparacion() {
        cola = new ColaPrioridad<>(ComparadoresPreparacion.PREPARACION);
        enPreparacion = new ListaDoble<>();
        minutosTotales = 0;
        preparadosTerminados = 0;
    }

    /**
     * Mete el apartamento a la cola. Tiene que estar PENDIENTE_PREPARACION y no puede estar ya en la cola.
     */
    public void agregar(Apartamento apartamento, boolean entradaHoy, LocalTime horaLlegada, LocalDateTime ahora) {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento no puede ser nulo");
        }
        if (apartamento.getEstado() != EstadoApartamento.PENDIENTE_PREPARACION) {
            throw new TransicionEstadoInvalidaException("El apartamento " + apartamento.getCodigo()
                    + " no esta pendiente de preparacion (esta " + apartamento.getEstado() + ")");
        }
        if (estaEnCola(apartamento.getCodigo())) {
            throw new IllegalStateException("El apartamento " + apartamento.getCodigo() + " ya esta en la cola");
        }
        cola.insertar(new TareaPreparacion(apartamento, entradaHoy, horaLlegada, ahora));
    }

    /**
     * Mira la siguiente tarea sin sacarla. Si la cola esta vacia da EstructuraVaciaException.
     */
    public TareaPreparacion siguiente() {
        return cola.verPrimero();
    }

    /**
     * Saca la siguiente tarea y pasa su apartamento a EN_PREPARACION.
     */
    public Apartamento iniciarSiguiente(LocalDateTime ahora) {
        TareaPreparacion tarea = cola.extraer();
        tarea.getApartamento().cambiarEstado(EstadoApartamento.EN_PREPARACION);
        enPreparacion.agregarAlFinal(tarea);
        return tarea.getApartamento();
    }

    /**
     * Termina la preparacion: el apartamento pasa a PREPARADO y se suman los minutos
     * desde que quedo pendiente hasta ahora.
     */
    public void terminar(String codigoApartamento, LocalDateTime ahora) {
        TareaPreparacion tarea = buscarEnPreparacion(codigoApartamento);
        if (tarea == null) {
            throw new IllegalArgumentException("El apartamento " + codigoApartamento + " no se esta preparando");
        }

        tarea.getApartamento().cambiarEstado(EstadoApartamento.PREPARADO);
        enPreparacion.eliminar(tarea);
        minutosTotales += Duration.between(tarea.getPendienteDesde(), ahora).toMinutes();
        preparadosTerminados++;
    }

    /**
     * Si llega una reserva para hoy y el apartamento esta en la cola, lo saca y lo vuelve a meter
     * con entrada hoy (prioridad 1). Devuelve false si no estaba en la cola.
     */
    public boolean subirAPrioridad1(String codigoApartamento, LocalTime horaLlegada) {
        TareaPreparacion vieja = buscarEnCola(codigoApartamento);
        if (vieja == null) {
            return false;
        }

        cola.eliminar(vieja);
        cola.insertar(new TareaPreparacion(vieja.getApartamento(), true, horaLlegada, vieja.getPendienteDesde()));
        return true;
    }

    /**
     * Dice si el apartamento esta esperando en la cola.
     */
    public boolean estaEnCola(String codigoApartamento) {
        return buscarEnCola(codigoApartamento) != null;
    }

    /**
     * Las tareas en el orden en que se van a atender, sin sacarlas de la cola.
     */
    public ListaDoble<TareaPreparacion> pendientesEnOrden() {
        return cola.enOrden();
    }

    /**
     * Los apartamentos que se estan preparando en este momento.
     */
    public ListaDoble<TareaPreparacion> enPreparacion() {
        return enPreparacion.copia();
    }

    /** Cuantos apartamentos esperan en la cola. */
    public int cantidadPendientes() {
        return cola.tamanio();
    }

    /**
     * Promedio de minutos que tarda un apartamento desde que queda pendiente hasta que queda preparado.
     * Si todavia no se ha terminado ninguno, da 0.
     */
    public double tiempoPromedioMinutos() {
        if (preparadosTerminados == 0) {
            return 0;
        }
        return (double) minutosTotales / preparadosTerminados;
    }

    // ------------------ metodos privados de ayuda ------------------

    private TareaPreparacion buscarEnCola(String codigoApartamento) {
        for (TareaPreparacion t : cola) {
            if (t.getCodigoApartamento().equals(codigoApartamento)) {
                return t;
            }
        }
        return null;
    }

    private TareaPreparacion buscarEnPreparacion(String codigoApartamento) {
        for (TareaPreparacion t : enPreparacion) {
            if (t.getCodigoApartamento().equals(codigoApartamento)) {
                return t;
            }
        }
        return null;
    }
}
