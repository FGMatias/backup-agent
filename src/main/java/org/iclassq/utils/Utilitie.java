package org.iclassq.utils;

import javafx.scene.control.Alert;

public class Utilitie {
    public static final int APP_WIDTH = 1024;
    public static final int APP_HEIGHT = 768;

    public static void showAlertDialog(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
