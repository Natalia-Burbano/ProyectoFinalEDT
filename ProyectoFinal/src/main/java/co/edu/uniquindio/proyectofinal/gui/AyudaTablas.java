package co.edu.uniquindio.proyectofinal.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Ayudas para las pantallas: llenar tablas desde nuestras estructuras, llenar columnas,
 * mostrar mensajes y dar formato al dinero. Cualquier pantalla del grupo la puede usar.
 */
public final class AyudaTablas {

    /** Saca el texto de una celda a partir del objeto de la fila. */
    public interface Extractor<S> {
        String valor(S fila);
    }

    private AyudaTablas() {
    }

    /** Pasa cualquier estructura propia (Iterable) a la lista observable que usan las tablas de JavaFX. */
    public static <T> ObservableList<T> aObservable(Iterable<T> origen) {
        ObservableList<T> lista = FXCollections.observableArrayList();
        if (origen != null) {
            for (T elemento : origen) {
                lista.add(elemento);
            }
        }
        return lista;
    }

    /**
     * Pone los datos en la tabla y la obliga a volver a dibujar todas las filas.
     * Sin el refresh(), si la reserva de una fila es "igual" (mismo codigo) a la que habia antes,
     * la tabla no redibuja la fila y se sigue viendo el estado viejo (por ejemplo Pendiente
     * despues de cancelar).
     */
    public static <T> void llenar(TableView<T> tabla, Iterable<T> origen) {
        tabla.setItems(aObservable(origen));
        tabla.refresh();
    }

    /** Dice de donde sale el texto de cada celda de la columna. */
    public static <S> void columna(TableColumn<S, String> columna, Extractor<S> extractor) {
        columna.setCellValueFactory(celda -> new SimpleStringProperty(extractor.valor(celda.getValue())));
    }

    /** Muestra un mensaje informativo. */
    public static void info(String titulo, String texto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }

    /** Muestra un mensaje de error. */
    public static void error(String texto) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("No se pudo");
        alerta.setHeaderText(null);
        alerta.setContentText(texto == null ? "Ocurrio un error" : texto);
        alerta.showAndWait();
    }

    /** Pregunta si o no. Devuelve true si la persona acepta. */
    public static boolean confirmar(String texto) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.OK, ButtonType.CANCEL);
        alerta.setTitle("Confirmar");
        alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /** Da formato de pesos: 1500000 -> $1.500.000 (segun la configuracion regional). */
    public static String dinero(long valor) {
        return "$" + String.format("%,d", valor);
    }
}
