package org.iclassq.views.components;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.iclassq.enums.DialogType;
import org.iclassq.utils.Fonts;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.logging.Logger;

public class Notification {
    private static StackPane notificationContainer;
    private static final Logger logger = Logger.getLogger(Notification.class.getName());

    public static void initialize(StackPane container) {
        notificationContainer = container;
        logger.info("Inicializando notificacion");
    }

    public static void showConfirmation(String message, Runnable onConfirm) {
        show(
            message,
            DialogType.CONFIRMATION,
            onConfirm,
            null
        );
    }

    public static void showConfirmation(String message, Runnable onConfirm, Runnable onCancel) {
        show(
            message,
            DialogType.CONFIRMATION,
            onConfirm,
            onCancel
        );
    }

    public static void showWarning(String message, Runnable onConfirm) {
        show(
            message,
            DialogType.WARNING,
            onConfirm,
            null
        );
    }

    public static void showDanger(String message, Runnable onConfirm) {
        show(
            message,
            DialogType.DANGER,
            onConfirm,
            null
        );
    }

    private static void show(
            String message,
            DialogType type,
            Runnable onConfirm,
            Runnable onCancel
    ) {
        if (notificationContainer == null) {
            logger.severe("Notificacion no inicializada");
            return;
        }

        atlantafx.base.controls.Notification notification = new atlantafx.base.controls.Notification(
                message,
                new FontIcon(type.getIcon())
        );
        notification.getStyleClass().addAll(Styles.ELEVATED_2, type.getStyleClass());

        VBox wrapper = new VBox(notification);
        wrapper.setMaxWidth(400);
        wrapper.setMaxHeight(150);
        wrapper.setPrefWidth(400);
        wrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);
        wrapper.setAlignment(Pos.CENTER);

        VBox.setVgrow(notification, Priority.NEVER);
        notification.setMaxWidth(450);
        notification.setMaxHeight(100);

        StackPane.setAlignment(notification, Pos.TOP_CENTER);
        StackPane.setMargin(notification, new Insets(20, 20, 0, 20));

        notificationContainer.getChildren().add(notification);

        Button confirmBtn;
        Button cancelBtn = new Button("Cancelar");

        if (type == DialogType.DANGER) {
            confirmBtn = new Button("Eliminar");
            confirmBtn.getStyleClass().add(Styles.DANGER);
        } else {
            confirmBtn = new Button("Confirmar");
            confirmBtn.getStyleClass().add(Styles.ACCENT);
        }

        confirmBtn.setDefaultButton(true);
        cancelBtn.getStyleClass().add(Styles.BUTTON_OUTLINED);

        confirmBtn.setFont(Fonts.semiBold(14));
        cancelBtn.setFont(Fonts.regular(14));

        confirmBtn.setOnAction(evt -> {
            closeNotification(notification);

            if (onConfirm != null) onConfirm.run();
        });

        cancelBtn.setOnAction(evt -> {
            closeNotification(notification);

            if (onCancel != null) onCancel.run();
        });

        notification.setOnClose(evt -> {
            closeNotification(notification);

            if (onCancel != null) onCancel.run();
        });

        notification.setPrimaryActions(confirmBtn, cancelBtn);

        Animations.slideInDown(notification, Duration.millis(300)).playFromStart();
    }

    private static void closeNotification(atlantafx.base.controls.Notification notification) {
        var slideOut = Animations.slideOutUp(notification, Duration.millis(250));
        slideOut.setOnFinished(evt -> notificationContainer.getChildren().remove(notification));
        slideOut.playFromStart();
    }

    public static void confirmDelete(String itemName, Runnable onConfirm) {
        showDanger(
                "¿Eliminar " + itemName + "?",
                onConfirm
        );
    }

    public static void confirmAction(String actionDescription, Runnable onConfirm) {
        showConfirmation(
                "¿" + actionDescription + "?",
                onConfirm
        );
    }

    public static void confirmWithCancel(String message, Runnable onConfirm, Runnable onCancel) {
        showConfirmation(message, onConfirm, onCancel);
    }
}
