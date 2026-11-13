module proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;

    exports co.edu.uniquindio.proyectofinal.excepciones;
    exports co.edu.uniquindio.proyectofinal.estructuras;
    exports co.edu.uniquindio.proyectofinal.modelo;

    // App es la que arranca JavaFX
    exports co.edu.uniquindio.proyectofinal.app;
    // JavaFX necesita entrar a los controladores de las pantallas y a los .fxml
    opens co.edu.uniquindio.proyectofinal.gui;
}
