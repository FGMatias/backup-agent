package org.iclassq.controller;

import jakarta.annotation.PostConstruct;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import org.iclassq.entity.Task;
import org.iclassq.event.UpdatedEvent;
import org.iclassq.scheduler.BackupTaskScheduler;
import org.iclassq.service.HistoryService;
import org.iclassq.service.TaskService;
import org.iclassq.views.DashboardContent;
import org.iclassq.views.components.Message;
import org.iclassq.views.components.Notification;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Component
public class DashboardController {
    private final DashboardContent view;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final BackupTaskScheduler backupTaskScheduler;

    private static final Logger logger = Logger.getLogger(DashboardController.class.getName());

    public DashboardController(
            DashboardContent view,
            TaskService taskService,
            HistoryService historyService,
            BackupTaskScheduler backupTaskScheduler
    ) {
        this.view = view;
        this.taskService = taskService;
        this.historyService = historyService;
        this.backupTaskScheduler = backupTaskScheduler;
    }

    @PostConstruct
    public void initialize() {
        setupEventHandlers();
        loadInitialData();
    }

    @EventListener
    public void onHistoryUpdated(UpdatedEvent event) {
        logger.info("Dashboard: Evento recibido - " + event.getEventType());

        Platform.runLater(() -> {
            try {
                loadStatistics();
                logger.info("Estadisticas del dashboard actualizadas automaticamente");
            } catch (Exception e) {
                logger.severe("Error al actualizar las estadísticas del dashboard: " + e.getMessage());
            }
        });
    }

    private void setupEventHandlers() {
        view.getBtnExecuteNow().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                handleExecuteNow();
            }
        });
    }

    private void loadInitialData() {
        try {
            long activeTasks = taskService.countActive();
            view.updateScheduledTasksCount(activeTasks);

            long runningTasks = historyService.countByStatus(3);
            view.updateRunningTasksCount(runningTasks);

            long completedToday = historyService.countCompletedToday();
            view.updateCompletedTodayCount(completedToday);

            updateNextExecution();

            long backupsToday = historyService.countByTypeToday(1);
            view.updateBackupsCount(backupsToday);

            long filesMovedToday = historyService.countFilesByTypeToday(3);
            view.updateFilesMovedCount(filesMovedToday);

            String spaceFreed = historyService.getTotalSizeToday();
            view.updateSpaceFreed(spaceFreed);

            long errorsToday = historyService.countByStatus(2);
            view.updateErrorsCount(errorsToday);

            logger.info("Dashboard cargado correctamente");
        } catch (Exception e) {
            logger.severe("Error al cargar dashboard: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            long completedToday = historyService.countCompletedToday();
            view.updateCompletedTodayCount(completedToday);

            long backupsToday = historyService.countByTypeToday(1);
            view.updateBackupsCount(backupsToday);

            long filesMovedToday = historyService.countFilesByTypeToday(3);
            view.updateFilesMovedCount(filesMovedToday);

            String spaceFreed = historyService.getTotalSizeToday();
            view.updateSpaceFreed(spaceFreed);

            long errorsToday = historyService.countByStatus(2);
            view.updateErrorsCount(errorsToday);
        } catch (Exception e) {
            logger.severe("Error al cargar estadísticas: " + e.getMessage());
        }
    }

    private void updateNextExecution() {
        Task nextTask = taskService.findNextScheduledTask();

        if (nextTask == null) {
            view.clearNextExecution();
            view.getBtnExecuteNow().setDisable(true);
            return;
        }

        LocalTime scheduleTime = nextTask.getScheduleTime();
        LocalTime now = LocalTime.now();

        String timeText;

        if (scheduleTime.isAfter(now)) {
            timeText = "HOY A LAS " + scheduleTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            timeText = "MAÑANA A LAS " + scheduleTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        String taskDescription = nextTask.getType() != null
                ? nextTask.getType().getDescription() + " - " + nextTask.getName()
                : nextTask.getName();

        view.updateNextExecution(timeText, taskDescription);
        view.getBtnExecuteNow().setDisable(false);
    }

    private void handleExecuteNow() {
        Task nextTask = taskService.findNextScheduledTask();

        if (nextTask != null) {
            Notification.confirmAction(
                    "Deseas ejecutar la tarea '" + nextTask.getName() + "'",
                    () -> {
                        try {
                            backupTaskScheduler.executeTaskManually(nextTask);

                            Message.showSuccess(
                                    "Tarea en ejecución",
                                    "La tarea '" + nextTask.getName() + "' se está ejecutando"
                            );
                        } catch (Exception e) {
                            logger.severe("Error al ejecutar tarea: " + e.getMessage());
                            Message.showError("Error", "No se pudo ejecutar la tarea");
                        }
                    }
            );
        }
    }
}
