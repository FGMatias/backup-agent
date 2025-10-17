package org.iclassq.views;

import atlantafx.base.theme.Styles;
import jakarta.annotation.PostConstruct;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.iclassq.controller.DashboardController;
import org.iclassq.entity.Task;
import org.iclassq.utils.Fonts;
import org.iclassq.utils.Utilitie;
import org.iclassq.views.components.Card;
import org.iclassq.views.components.Grid;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class DashboardContent {
    private BorderPane root;
    private VBox mainContent;
    private Label executionTime;
    private Label executionTask;
    private Button btnExecuteNow;

    private Card scheduleTaskCard;
    private Card runningTaskCard;
    private Card completedTodayCard;
    private Card backupCard;
    private Card fileCard;
    private Card spaceCard;
    private Card errorCard;

    @PostConstruct
    public void initialize() {
        buildContent();
    }

    private void buildContent() {
        mainContent = new VBox(25);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.getStyleClass().add(Styles.BG_DEFAULT);

        VBox contentContainer = new VBox(25);
//        contentContainer.setMaxWidth(Utilitie.APP_WIDTH - 20);
        contentContainer.setAlignment(Pos.TOP_CENTER);

        Grid quickSummary = createQuickSummarySection();
        VBox executionCard = createExecutionCard();
        VBox statsSection = createStatsSection();

        contentContainer.getChildren().addAll(
            quickSummary,
            executionCard,
            statsSection
        );

        mainContent.getChildren().add(contentContainer);
    }

    public ScrollPane getContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edge-to-edge");
        scrollPane.setContent(mainContent);
        return scrollPane;
    }

    private Grid createQuickSummarySection() {
        scheduleTaskCard = new Card("Tareas Programadas", "0");
        runningTaskCard = new Card("Ejecutándose", "0");
        completedTodayCard = new Card("Completadas Hoy", "0");

        return new Grid(
                3,
                scheduleTaskCard,
                runningTaskCard,
                completedTodayCard
        );
    }

    private VBox createExecutionCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle(
                "-fx-background-color: -color-accent-8; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        Label subTitle = new Label("Próxima Ejecución");
        subTitle.getStyleClass().add(Styles.TEXT_MUTED);
        subTitle.setFont(Fonts.regular(16));

        VBox executionInfo = new VBox(10);
        executionInfo.setAlignment(Pos.CENTER);
        executionInfo.setPadding(new Insets(20));
        executionInfo.setStyle(
                "-fx-background-color: -color-accent-7; " +
                "-fx-background-radius: 8;"
        );

        executionTime = new Label("CARGANDO...");
        executionTime.setFont(Fonts.bold(32));
        executionTime.setWrapText(true);
        executionTime.setMaxWidth(450);
        executionTime.setAlignment(Pos.CENTER);

        executionTask = new Label("Obteniendo próxima tarea...");
        executionTask.setFont(Fonts.regular(16));
        executionTask.setWrapText(true);
        executionTask.setMaxWidth(450);
        executionTask.setAlignment(Pos.CENTER);
        executionTask.getStyleClass().add(Styles.TEXT_MUTED);

        executionInfo.getChildren().addAll(executionTime, executionTask);

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        btnExecuteNow = new Button("Ejecutar Ahora");
        btnExecuteNow.setPadding(new Insets(10, 20, 10, 20));
        btnExecuteNow.setMnemonicParsing(true);
        btnExecuteNow.setStyle(
                "-fx-background-color: -color-light; " +
                "-fx-text-fill: -color-accent-5; " +
                "-fx-font-size: 20px; " +
                "-fx-font-weight: bold;"
        );
        btnExecuteNow.setDisable(true);

        buttons.getChildren().addAll(btnExecuteNow);

        card.getChildren().addAll(subTitle, executionInfo, buttons);
        return card;
    }

    private VBox createStatsSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("Estadísticas de Hoy");
        title.setFont(Fonts.bold(28));
        title.getStyleClass().add(Styles.TITLE_3);

        backupCard = new Card("Backups", "0");
        fileCard = new Card("Archivos Movidos", "0");
        spaceCard = new Card("Espacio Liberado", "0");
        errorCard = new Card("Errores", "0", true);

        Grid grid = new Grid(
                4,
                backupCard,
                fileCard,
                spaceCard,
                errorCard
        );

        section.getChildren().addAll(title, grid);
        return section;
    }

    public void updateScheduledTasksCount(long count) {
        scheduleTaskCard.updateValue(String.valueOf(count));
    }

    public void updateRunningTasksCount(long count) {
        runningTaskCard.updateValue(String.valueOf(count));
    }

    public void updateCompletedTodayCount(long count) {
        completedTodayCard.updateValue(String.valueOf(count));
    }

    public void updateNextExecution(String time, String taskName) {
        executionTime.setText(time);
        executionTask.setText(taskName);
    }

    public void clearNextExecution() {
        executionTime.setText("SIN TAREAS PROGRAMADAS");
        executionTask.setText("No hay tareas activas en este momento");
    }

    public void updateBackupsCount(long count) {
        backupCard.updateValue(String.valueOf(count));
    }

    public void updateFilesMovedCount(long count) {
        fileCard.updateValue(String.valueOf(count));
    }

    public void updateSpaceFreed(String space) {
        spaceCard.updateValue(space);
    }

    public void updateErrorsCount(long count) {
        errorCard.updateValue(String.valueOf(count));
    }

    public BorderPane getRoot() {
        return root;
    }

    public void setRoot(BorderPane root) {
        this.root = root;
    }

    public VBox getMainContent() {
        return mainContent;
    }

    public void setMainContent(VBox mainContent) {
        this.mainContent = mainContent;
    }

    public Label getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Label executionTime) {
        this.executionTime = executionTime;
    }

    public Label getExecutionTask() {
        return executionTask;
    }

    public void setExecutionTask(Label executionTask) {
        this.executionTask = executionTask;
    }

    public Button getBtnExecuteNow() {
        return btnExecuteNow;
    }

    public void setBtnExecuteNow(Button btnExecuteNow) {
        this.btnExecuteNow = btnExecuteNow;
    }

}
