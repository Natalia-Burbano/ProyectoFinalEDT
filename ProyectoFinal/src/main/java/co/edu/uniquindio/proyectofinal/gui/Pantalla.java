package co.edu.uniquindio.proyectofinal.gui;

import co.edu.uniquindio.proyectofinal.servicio.SistemaAlojamiento;

/**
 * La implementan los controladores de todas las pantallas.
 * La ventana principal la usa para pasarles el sistema y para pedirles que se actualicen.
 */
public interface Pantalla {

    /**
     * Se llama una sola vez, apenas se carga la pantalla.
     * Aqui la pantalla guarda el sistema y la ventana para usarlos despues.
     */
    void iniciar(SistemaAlojamiento sistema, VentanaPrincipal ventana);

    /**
     * Se llama cada vez que la pantalla se muestra o cuando algo cambio,
     * para que vuelva a leer los datos del sistema.
     */
    void refrescar();
}
