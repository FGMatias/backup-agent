package org.iclassq.controller;

import jakarta.annotation.PostConstruct;
import org.iclassq.entity.Task;
import org.iclassq.scheduler.BackupTaskScheduler;
import org.iclassq.service.FrequencyService;
import org.iclassq.service.TaskService;
import org.iclassq.service.TypeTaskService;
import org.iclassq.views.TaskContent;
import org.iclassq.views.components.Message;
import org.iclassq.views.components.Notification;
import org.iclassq.views.dialogs.TaskFormDialog;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class TaskController {
    private final TaskContent view;
    private final TaskService taskService;
    private final TypeTaskService typeTaskService;
    private final FrequencyService frequencyService;
    private final BackupTaskScheduler backupTaskScheduler;

    private Map<String, Integer> statusMap = new HashMap<>();

    private static final Logger logger = Logger.getLogger(TaskController.class.getName());

    public TaskController(
            TaskContent view,
            TaskService taskService,
            TypeTaskService typeTaskService,
            FrequencyService frequencyService,
            BackupTaskScheduler backupTaskScheduler
    ) {
        this.view = view;
        this.taskService = taskService;
        this.typeTaskService = typeTaskService;
        this.frequencyService = frequencyService;
        this.backupTaskScheduler = backupTaskScheduler;
    }

    @PostConstruct
    private void initialize() {
        setupEventHandlers();
        loadStatus();
        loadInitialData();
    }

    private void setupEventHandlers() {
        view.setOnAdd(this::handleAdd);
        view.setOnEdit(this::handleEdit);
        view.setOnDelete(this::handleDelete);
        view.setOnExecute(this::handleExecute);
        view.setOnStatusChange(this::handleStatusChange);
        view.setOnRefresh(this::loadInitialData);
        view.setOnSearch(this::applyFilters);
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
        Notification.confirmDelete(
                "la tarea '" + task.getName() + "'",
                () -> {
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
        );
    }

    public void handleExecute(Task task) {
        Notification.confirmAction(
                "Deseas ejecutar la tarea '" + task.getName() + "'",
                () -> {
                    try {
                        backupTaskScheduler.executeTaskManually(task);

                        Message.showSuccess(
                                "Tarea en ejecución",
                                "La tarea '" + task.getName() + "' se está ejecutando"
                        );
                    } catch (Exception e) {
                        logger.severe("Error al ejecutar tarea: " + e.getMessage());
                        Message.showError("Error", "No se pudo ejecutar la tarea");
                    }
                }
        );
    }

    public void handleStatusChange(Task task, Boolean newIsActive) {
        Boolean originalState = task.getIsActive();

        String message = newIsActive
                ? "Deseas activar la tarea '" + task.getName() + "'"
                : "Deseas desactivar la tarea '" + task.getName() + "'";

        Notification.confirmWithCancel(
                message,
                () -> {
                    try {
                        task.setIsActive(newIsActive);
                        taskService.update(task);

                        Message.showSuccess(
                                "Estado actualizado",
                                String.format("'%s' ahora está %s",
                                        task.getName(),
                                        newIsActive ? "Activo" : "Inactivo"
                                )
                        );

                        view.refreshTable(taskService.findAll());
                    } catch (Exception e) {
                        logger.severe("Error al cambiar estado: " + e.getMessage());
                        Message.showError(
                                "Error",
                                "No se pudo actualizar el estado"
                        );
                        task.setIsActive(originalState);
                        view.refreshTable(taskService.findAll());
                    }
                },
                () -> {
                    task.setIsActive(originalState);
                    view.refreshTable(taskService.findAll());
                }
        );
    }

    public void applyFilters() {
        try {
            String searchText = view.getSearchField().getText();
            String selectedStatus = view.getCboStatus().getValue();

            List<Task> allTasks = taskService.findAll();
            List<Task> filteredTasks = allTasks;

            if (searchText != null && !searchText.trim().isEmpty()) {
                String search = searchText.trim().toLowerCase();

                filteredTasks = filteredTasks.stream()
                        .filter(task ->
                                task.getName().toLowerCase().contains(search) ||
                                (task.getType() != null && task.getType().getDescription().toLowerCase().contains(search)) ||
                                (task.getDatabaseName() != null && task.getDatabaseName().toLowerCase().contains(search))
                        )
                        .collect(Collectors.toList());
            }

            if (selectedStatus != null && !selectedStatus.equals("Todos")) {
                Integer statusId = statusMap.get(selectedStatus);

                if (statusId != null && statusId != 0) {
                    boolean isActive = statusId == 1;

                    filteredTasks = filteredTasks.stream()
                            .filter(task -> task.getIsActive() != null && task.getIsActive() == isActive)
                            .collect(Collectors.toList());
                }
            }

            view.refreshTable(filteredTasks);
            view.updateTaskCount((long) filteredTasks.size());
        } catch (Exception e) {
            logger.severe("Error al aplicar los filtros: " + e.getMessage());
            e.printStackTrace();
            Message.showError(
                    "Error",
                    "No se pudieron aplicar los filtros"
            );
        }
    }

    public void loadInitialData() {
        try {
            view.refreshTable(taskService.findAll());
            view.updateTaskCount(taskService.count());

            view.getSearchField().clear();
            view.getCboStatus().setValue("Todos");
        } catch (Exception e) {
            logger.severe("Error al cargar las tareas: " + e.getMessage());
            try {
                Message.showError("Error", "No se pudieron cargar las tareas");
            } catch (Exception msgError) {
                logger.warning("No se pudo mostrar mensaje de error: " + msgError.getMessage());
            }
        }
    }
}
