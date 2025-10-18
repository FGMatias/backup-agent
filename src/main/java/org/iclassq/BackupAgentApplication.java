package org.iclassq;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.iclassq.utils.Fonts;
import org.iclassq.utils.ViewNavigator;
import org.iclassq.views.MainView;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
@EnableScheduling
public class BackupAgentApplication extends Application {
    private ConfigurableApplicationContext context;
    private Stage primaryStage;
    private TrayIcon trayIcon;
    private SystemTray systemTray;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        System.setProperty("java.awt.headless", "false");

        SpringApplication app = new SpringApplication(BackupAgentApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        context = app.run();

        Fonts.loadFonts();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
        Platform.setImplicitExit(false);

        stage.setTitle("Agent Backup");
        ViewNavigator.setMainStage(stage);

        stage.setOnCloseRequest(evt -> {
            evt.consume();
            stage.hide();
            showTrayNotification(
                    "Backup Agent sigue ejecutándose en segundo plano",
                    "Haz click derecho en el ícono para ver las opciones"
            );
        });

        MainView view = context.getBean(MainView.class);
        view.show();

        setupSystemTray();
        stage.show();
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("System Tray no está soportado en este sistema");
            return;
        }

        try {
            systemTray = SystemTray.getSystemTray();
            Image trayIconImage = createTrayIcon();

            PopupMenu popup = new PopupMenu();

            MenuItem openItem = new MenuItem("Abrir Backup Agent");
            openItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            openItem.addActionListener(evt -> Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
                primaryStage.requestFocus();
            }));

            MenuItem statusItem = new MenuItem("Estado: Activo");
            statusItem.setEnabled(false);
            statusItem.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            MenuItem separator = new MenuItem("-");

            MenuItem exitItem = new MenuItem("Salir");
            exitItem.setFont(new Font("Segoe UI", Font.BOLD, 12));
            exitItem.addActionListener(evt -> {
                int confirm = showConfirmDialog();
                if (confirm == 0) exitApplication();
            });

            popup.add(openItem);
            popup.add(statusItem);
            popup.add(separator);
            popup.add(exitItem);

            trayIcon = new TrayIcon(trayIconImage, "Backup Agent - Ejecutándose", popup);
            trayIcon.setImageAutoSize(true);

            trayIcon.addActionListener(evt -> Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
            }));

            systemTray.add(trayIcon);

        } catch (AWTException e) {
            System.err.println("Error al configurar System Tray: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Image createTrayIcon() {
        try {
            InputStream is = getClass().getResourceAsStream("/icon/backup.png");

            if (is == null) {
                return createDefaultIcon();
            }

            BufferedImage originalImage = ImageIO.read(is);
            is.close();

            if (originalImage == null) {
                return createDefaultIcon();
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            if (originalWidth != 32 || originalHeight != 32) {
                BufferedImage resizedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = resizedImage.createGraphics();

                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g.drawImage(originalImage, 0, 0, 32, 32, null);
                g.dispose();

                return resizedImage;
            }

            return originalImage;

        } catch (IOException e) {
            System.err.println("Error al cargar icono: " + e.getMessage());
            return createDefaultIcon();
        }
    }

    private Image createDefaultIcon() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(33, 150, 243));
        g.fillOval(2, 2, 28, 28);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("BA", 9, 21);

        g.dispose();

        return image;
    }

    private void showTrayNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    private int showConfirmDialog() {
        return JOptionPane.showConfirmDialog(
                null,
                "¿Estás seguro de que deseas salir?\nLas tareas programadas dejarán de ejecutarse.",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void exitApplication() {
        if (trayIcon != null) {
            trayIcon.displayMessage(
                    "Backup Agent",
                    "Aplicación cerrada",
                    TrayIcon.MessageType.INFO
            );
        }

        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
        }

        if (context != null) {
            context.close();
        }

        Platform.exit();
        System.exit(0);
    }

    @Override
    public void stop() throws Exception {
        exitApplication();
    }
}