package org.iclassq.controller;

import atlantafx.base.controls.Notification;
import jakarta.annotation.PostConstruct;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.iclassq.entity.Task;
import org.iclassq.service.FrequencyService;
import org.iclassq.service.TaskService;
import org.iclassq.service.TypeTaskService;
import org.iclassq.views.TaskContent;
import org.iclassq.views.components.Message;
import org.iclassq.views.dialogs.TaskFormDialog;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class TaskController {
    private final TaskContent view;
    private final TaskService taskService;
    private final TypeTaskService typeTaskService;
    private final FrequencyService frequencyService;

    private Map<String, Integer> statusMap = new HashMap<>();

    private static final Logger logger = Logger.getLogger(TaskController.class.getName());

    public TaskController(
            TaskContent view,
            TaskService taskService,
            TypeTaskService typeTaskService,
            FrequencyService frequencyService
    ) {
        this.view = view;
        this.taskService = taskService;
        this.typeTaskService = typeTaskService;
        this.frequencyService = frequencyService;
    }

    @PostConstruct
    private void init() {
        setupEventHandlers();
        loadStatus();
        loadInitialData();
    }

    private void setupEventHandlers() {
        view.getBtnAddTask().setOnAction(evt -> handleAdd());
        view.getBtnRefresh().setOnAction(evt -> loadInitialData());
        view.getBtnSearch().setOnMouseClicked(evt -> applyFilters());
        view.getBtnEdit().setOnMouseClicked(evt -> handleEdit());
        view.getBtnDelete().setOnMouseClicked(evt -> handleDelete());
    }

    private void loadStatus() {
        try {
            view.getCboStatus().getItems().clear();
            statusMap.clear();

            view.getCboStatus().getItems().add("Todos");
            view.getCboStatus().getItems().add("Activo");
            view.getCboStatus().getItems().add("Inactivo");

            statusMap.put("Todos", 0);
            statusMap.put("Activo", 1);
            statusMap.put("Inactivo", 2);

            view.getCboStatus().setValue("Todos");
        } catch (Exception e) {
            logger.severe("Error al cargar los estados: " + e.getMessage());
        }
    }

    public void handleAdd() {
        try {
            TaskFormDialog dialog = new TaskFormDialog();

            dialog.setTypeOptions(typeTaskService.findAll());
            dialog.setFrequencyOptions(frequencyService.findAll());

            Optional<Task> result = dialog.showAndWait();

            logger.info("Result present: " + result.isPresent());

            result.ifPresent(task -> {
                try {
                    Task savedTask = taskService.save(task);

                    view.refreshTable(taskService.findAll());
                    view.updateTaskCount(taskService.count());

                    Message.showSuccess(
                            "Tarea creada",
                            "La tarea '" + savedTask.getName() + "' se creó correctamente"
                    );

                    logger.info("Tarea creada correctamente: " + savedTask.getId());
                } catch (Exception e) {
                    logger.severe("Error al guardar la tarea: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.severe("Error al abrir el modal: " + e.getMessage());
            Message.showError(
                    "Error!",
                    "No se pudo abrir el formulario de tarea"
            );
        }
    }

    public void handleEdit(Task task) {
        try {
            TaskFormDialog dialog = new TaskFormDialog(task);

            dialog.setTypeOptions(typeTaskService.findAll());
            dialog.setFrequencyOptions(frequencyService.findAll());

            Optional<Task> result = dialog.showAndWait();

            result.ifPresent(updatedTask -> {
                try {
                    taskService.update(updatedTask);
                    view.refreshTable(taskService.findAll());

                    Message.showSuccess(
                            "Tarea actualizada",
                            "Los cambios se guardaron correctamente"
                    );

                    logger.info("Tarea actualizada: " + updatedTask.getId());
                } catch (Exception e) {
                    logger.severe("Error al actualizar la tarea: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.severe("Error al abrir el modal de edicion: " + e.getMessage());
            Message.showError(
                    "Error",
                    "No se pudo abrir el formulario de edición"
            );
        }
    }

    public void handleDelete(Task task) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmar eliminación");
        confirmation.setHeaderText("¿Eliminar esta tarea?");
        confirmation.setContentText("Tarea: " + task.getName());

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                taskService.delete(task.getId());
                view.refreshTable(taskService.findAll());
                view.updateTaskCount(taskService.count());

                Message.showSuccess(
                        "Tarea eliminada",
                        "La tarea se eliminó correctamente"
                );
            } catch (Exception e) {
                logger.severe("Error al eliminar: " + e.getMessage());
                Message.showError(
                        "Error",
                        "No se pudo eliminar la tarea"
                );
            }
        }
    }

    public void handleStatusChange(Task task, Boolean isActive) {
        try {
            task.setIsActive(isActive);
            taskService.update(task);

            logger.info(String.format(
                    "Tarea '%s' -> %s",
                    task.getName(),
                    isActive ? "Activo" : "Inactiva"
            ));

            Message.showSuccess(
                    "Estado actualizado",
                    String.format("'%s' ahora está %s",
                            task.getName(),
                            isActive ? "Activo" : "Inactivo"
                    )
            );

            view.refreshTable(taskService.findAll());
        } catch (Exception e) {
            logger.severe("Error al cambiar estado: " + e.getMessage());
            Message.showError("Error", "No se pudo actualizar el estado");
            view.refreshTable(taskService.findAll());
        }
    }

    private void applyFilters() {
        String selectedStatus = view.getCboStatus().getValue();

        if (selectedStatus != null) {
            Integer statusId = statusMap.get(selectedStatus);

            if (statusId == 0) {
                view.refreshTable(taskService.findAll());
                view.updateTaskCount(taskService.count());
                logger.info("Mostrando todas las tareas");
            } else {
                boolean isActive = statusId == 1;
                view.refreshTable(taskService.findByIsActive(isActive));
                view.updateTaskCount((long) taskService.findByIsActive(isActive).size());
                logger.info("Filtrar tareas por estado: " + statusId + " - " + selectedStatus);
            }
        }
    }

    private void loadInitialData() {
        try {
            view.refreshTable(taskService.findAll());
            view.updateTaskCount(taskService.count());
        } catch (Exception e) {
            logger.severe("Error al cargar las tareas: " + e.getMessage());
            Message.showError(
                    "Error",
                    "No se pudieron cargar las tareas"
            );
        }
    }
}
