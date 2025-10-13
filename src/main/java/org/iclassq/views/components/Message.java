package org.iclassq.views.components;

import atlantafx.base.util.Animations;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.iclassq.enums.MessageType;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.Map;
import java.util.logging.Logger;

public class Message {
    private static StackPane notificationContainer;
    private static final double DEFAULT_DURATION = 3.0;
    private static final Logger logger = Logger.getLogger(Message.class.getName());

    public static void initialize(StackPane container) {
        notificationContainer = container;
        notificationContainer.setPickOnBounds(false);
    }

    public static void showSuccess(String title, String content) {
        show(
            title,
            content,
            Material2OutlinedAL.CHECK_CIRCLE_OUTLINE,
            MessageType.SUCCESS
        );
    }

    public static void showInformation(String title, String content) {
        show(
            title,
            content,
            Material2OutlinedAL.HELP_OUTLINE,
            MessageType.INFORMATION
        );
    }

    public static void showWarning(String title, String content) {
        show(
            title,
            content,
            Material2OutlinedMZ.OUTLINED_FLAG,
            MessageType.WARNING
        );
    }

    public static void showError(String title, String content) {
        show(
            title,
            content,
            Material2OutlinedAL.ERROR_OUTLINE,
            MessageType.ERROR
        );
    }

    public static void showValidationErrors(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) return;

        if (errors.size() == 1) {
            Map.Entry<String, String> entry = errors.entrySet()
                                                    .iterator()
                                                    .next();
            showWarning("Error de validación", entry.getValue());
            return;
        }

        StringBuilder message = new StringBuilder();
        int count = 0;

        for (String error : errors.values()) {
            if (count > 0) {
                message.append("\n");
            }

            message.append("- ").append(error);
            count++;

            if (count >= 5) {
                int remaining = errors.size() - 5;
                if (remaining > 0) {
                    message.append("\n... y ").append(remaining).append(" error(es) más");
                }
                break;
            }
        }

        showWarning("Errores de Validación (" + errors.size() + ")", message.toString());
    }

    public static void showValidationErrors(String title, Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) return;

        if (errors.size() == 1) {
            Map.Entry<String, String> entry = errors.entrySet()
                                                    .iterator()
                                                    .next();
            showWarning(title, entry.getValue());
            return;
        }

        StringBuilder message = new StringBuilder();
        int count = 0;

        for (String error : errors.values()) {
            if (count > 0) {
                message.append("\n");
            }

            message.append("- ").append(error);
            count++;

            if (count >= 5) {
                int remaining = errors.size() - 5;
                if (remaining > 0) {
                    message.append("\n... y ").append(remaining).append(" más");
                }
                break;
            }
        }

        showWarning(title, message.toString());
    }

    public static void showValidationInfo(String title, String content) {
        showInformation(title, content);
    }

    private static void show(
            String title,
            String content,
            Ikon icon,
            MessageType messageType
    ) {
        if (notificationContainer == null) {
            logger.severe("Container de notificacion no inicializado");
        }

        atlantafx.base.controls.Message message = new atlantafx.base.controls.Message(
                title,
                content,
                new FontIcon(icon)
        );
        message.getStyleClass().add(messageType.getStyleClass());

        StackPane.setAlignment(message, Pos.TOP_RIGHT);
        StackPane.setMargin(message, new Insets(20,20,0,0));

        notificationContainer.getChildren().add(message);

        Animations.flash(message).playFromStart();
        message.setOnClose(evt -> {
            Animations.flash(message).playFromStart();

            PauseTransition removeDelay = new PauseTransition(Duration.seconds(DEFAULT_DURATION));
            removeDelay.setOnFinished(e -> notificationContainer.getChildren().remove(message));
            removeDelay.play();
        });
        message.setMaxWidth(400);
    }
}
