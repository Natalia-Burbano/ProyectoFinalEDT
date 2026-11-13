package co.edu.uniquindio.proyectofinal.servicio;

import co.edu.uniquindio.proyectofinal.estructuras.ArbolBinarioBusqueda;
import co.edu.uniquindio.proyectofinal.estructuras.ListaDoble;
import co.edu.uniquindio.proyectofinal.modelo.Estancia;
import co.edu.uniquindio.proyectofinal.modelo.Reserva;
import java.time.LocalDate;

/**
 * Las reservas de UN apartamento, ordenadas por fecha de entrada.
 * Detecta solapamientos recorriendo el arbol de forma recursiva con poda:
 * no revisa todas las reservas del alojamiento, ni siquiera todas las del apartamento.
 */
public class ArbolReservas extends ArbolBinarioBusqueda<Reserva> {

    /** Dice si la nueva estancia comparte al menos una noche con una reserva activa. */
    public boolean haySolapamiento(Estancia nueva) {
        return reservaQueChoca(nueva) != null;
    }

    /** Devuelve la reserva activa que choca con la estancia (para el mensaje), o null. */
    public Reserva reservaQueChoca(Estancia nueva) {
        if (nueva == null) {
            throw new IllegalArgumentException("La estancia no puede ser nula");
        }
        return buscarChoque(raiz, nueva);
    }

    private Reserva buscarChoque(Nodo<Reserva> nodo, Estancia nueva) {
        // CASO BASE: no hay mas arbol
        if (nodo == null) {
            return null;
        }
        Reserva actual = nodo.valor;
        // PODA: si este nodo empieza el dia de salida de la nueva (o despues), todo lo de la
        // derecha empieza aun mas tarde. Solo puede haber choque por la izquierda.
        if (!actual.getEstancia().getEntrada().isBefore(nueva.getSalida())) {
            return buscarChoque(nodo.izquierdo, nueva);
        }
        // CASO RECURSIVO: se revisa este nodo (solo cuentan las activas) y se baja por los dos lados
        if (actual.estaActiva() && actual.getEstancia().seCruzaCon(nueva)) {
            return actual;
        }
        Reserva porIzquierda = buscarChoque(nodo.izquierdo, nueva);
        if (porIzquierda != null) {
            return porIzquierda;
        }
        return buscarChoque(nodo.derecho, nueva);
    }

    /** Dice si alguna reserva activa ocupa esa noche. La usa el calendario. */
    public boolean ocupadaLaNoche(LocalDate noche) {
        return reservaLaNoche(noche) != null;
    }

    /** La reserva activa que ocupa esa noche, o null si esta libre. */
    public Reserva reservaLaNoche(LocalDate noche) {
        if (noche == null) {
            throw new IllegalArgumentException("La noche no puede ser nula");
        }
        return buscarNoche(raiz, noche);
    }

    private Reserva buscarNoche(Nodo<Reserva> nodo, LocalDate noche) {
        // CASO BASE
        if (nodo == null) {
            return null;
        }
        Reserva actual = nodo.valor;
        // PODA: si este nodo empieza despues de la noche, la derecha tampoco sirve
        if (actual.getEstancia().getEntrada().isAfter(noche)) {
            return buscarNoche(nodo.izquierdo, noche);
        }
        // CASO RECURSIVO
        if (actual.estaActiva() && actual.getEstancia().contiene(noche)) {
            return actual;
        }
        Reserva porIzquierda = buscarNoche(nodo.izquierdo, noche);
        if (porIzquierda != null) {
            return porIzquierda;
        }
        return buscarNoche(nodo.derecho, noche);
    }

    /** Las reservas activas, de la que entra primero a la que entra ultima. */
    public ListaDoble<Reserva> activas() {
        ListaDoble<Reserva> resultado = new ListaDoble<>();
        recolectarActivas(raiz, resultado);
        return resultado;
    }

    private void recolectarActivas(Nodo<Reserva> nodo, ListaDoble<Reserva> resultado) {
        if (nodo == null) { // CASO BASE
            return;
        }
        recolectarActivas(nodo.izquierdo, resultado);
        if (nodo.valor.estaActiva()) {
            resultado.agregarAlFinal(nodo.valor);
        }
        recolectarActivas(nodo.derecho, resultado);
    }
}
