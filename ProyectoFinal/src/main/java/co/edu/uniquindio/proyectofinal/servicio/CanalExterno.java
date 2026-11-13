package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.Cola;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.ConflictoCanal;
import co.edu.uniquindio.proyectofinal.modelo.SolicitudExterna;

import java.time.LocalDateTime;

/**
 * Canal externo: las solicitudes de la plataforma de terceros llegan a una cola (FIFO)
 * y se procesan una a una en el orden en que llegaron.
 * Las que chocan con una reserva quedan guardadas como conflictos.
 */
public class CanalExterno {

    private final Cola<SolicitudExterna> solicitudes;
    private final ListaDoble<ConflictoCanal> conflictos;

    /** Crea el canal sin solicitudes ni conflictos. */
    public CanalExterno() {
        solicitudes = new Cola<>();
        conflictos = new ListaDoble<>();
    }

    /**
     * Recibe una solicitud y la pone al final de la cola.
     */
    public void recibir(SolicitudExterna solicitud) {
        solicitudes.encolar(solicitud);
    }

    /**
     * Dice si quedan solicitudes por procesar.
     */
    public boolean haySolicitudes() {
        return !solicitudes.estaVacia();
    }

    /**
     * Saca la siguiente solicitud (la que llego primero). Vacia: EstructuraVaciaException.
     */
    public SolicitudExterna siguiente() {
        return solicitudes.desencolar();
    }

    /**
     * Las solicitudes que faltan, en orden de llegada, sin sacarlas de la cola.
     */
    public ListaDoble<SolicitudExterna> pendientes() {
        ListaDoble<SolicitudExterna> lista = new ListaDoble<>();
        for (SolicitudExterna s : solicitudes) {
            lista.agregarAlFinal(s);
        }
        return lista;
    }

    /**
     * Guarda una solicitud rechazada con su motivo.
     */
    public ConflictoCanal registrarConflicto(SolicitudExterna solicitud, String motivo, LocalDateTime fechaHora) {
        ConflictoCanal conflicto = new ConflictoCanal(solicitud, motivo, fechaHora);
        conflictos.agregarAlFinal(conflicto);
        return conflicto;
    }

    /**
     * Copia de los conflictos registrados, en el orden en que pasaron.
     */
    public ListaDoble<ConflictoCanal> conflictos() {
        return conflictos.copia();
    }

    /** Cuantas solicitudes se han rechazado. */
    public int cantidadConflictos() {
        return conflictos.tamanio();
    }

    /** Cuantas solicitudes faltan por procesar. */
    public int cantidadPendientes() {
        return solicitudes.tamanio();
    }
}
