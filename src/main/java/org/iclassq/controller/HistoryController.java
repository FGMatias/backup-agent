package org.iclassq.controller;

import jakarta.annotation.PostConstruct;
import org.iclassq.entity.ExecutionStatus;
import org.iclassq.entity.History;
import org.iclassq.entity.TypeTask;
import org.iclassq.service.ExecutionStatusService;
import org.iclassq.service.HistoryService;
import org.iclassq.service.TypeTaskService;
import org.iclassq.views.HistoryContent;
import org.iclassq.views.components.Message;
import org.iclassq.views.components.Notification;
import org.iclassq.views.dialogs.HistoryDialog;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class HistoryController {
    private final HistoryContent view;
    private final HistoryService historyService;
    private final ExecutionStatusService executionStatusService;
    private final TypeTaskService typeTaskService;

    private Map<String, Integer> statusMap = new HashMap<>();
    private Map<String, Integer> typesTasksMap = new HashMap<>();
    private static final Logger logger = Logger.getLogger(HistoryController.class.getName());

    public HistoryController(
            HistoryContent view,
            HistoryService historyService,
            ExecutionStatusService executionStatusService,
            TypeTaskService typeTaskService
    ) {
        this.view = view;
        this.historyService = historyService;
        this.executionStatusService = executionStatusService;
        this.typeTaskService = typeTaskService;
    }

    @PostConstruct
    private void initialize() {
        setupEventHandlers();
        loadStatus();
        loadTypesTasks();
        loadInitialData();
    }

    private void setupEventHandlers() {
        view.setOnRefresh(this::loadInitialData);
        view.setOnClearHistory(this::handleClearHistory);
//        view.setOnSearch(this::applyFilters);
        view.setOnClearFilters(this::handleClearFilter);
//        view.setOnCancel(this::handleCancel);
        view.setOnShowDetails(this::handleShowDetails);
    }

    private void loadStatus() {
        try {
            view.getStatusFilter().getItems().clear();
            statusMap.clear();

            view.getStatusFilter().getItems().add("Todos");
            statusMap.put("Todos", 0);

            List<ExecutionStatus> list = executionStatusService.findAll();

            for (ExecutionStatus status : list) {
                String description = status.getDescription();
                view.getStatusFilter().getItems().add(description);
                statusMap.put(description, status.getId());
            }

            view.getStatusFilter().setValue("Todos");

            logger.info("Estados cargados: " + list.size());
        } catch (Exception e) {
            logger.severe("Error al cargar los estados: " + e.getMessage());
            Message.showError(
                    "Error",
                    "No se pudieron cargar los estados"
            );
        }
    }

    private void loadTypesTasks() {
        try {
            view.getTypeFilter().getItems().clear();
            typesTasksMap.clear();

            view.getTypeFilter().getItems().add("Todos");
            typesTasksMap.put("Todos", 0);

            List<TypeTask> list = typeTaskService.findAll();

            for (TypeTask types : list) {
                String description = types.getDescription();
                view.getTypeFilter().getItems().add(description);
                typesTasksMap.put(description, types.getId());
            }

            view.getTypeFilter().setValue("Todos");

            logger.info("Tipos de tarea cargados: " + list.size());
        } catch (Exception e) {
            logger.severe("Error al cargar los tipos de tarea: " + e.getMessage());
            Message.showError(
                    "Error",
                    "No se pudieron cargar los tipos de tarea"
            );
        }
    }

    public void loadInitialData() {
        try {
            view.refreshTable(historyService.findAll());
//            view.updateExecutionsCount(historyService.countExecutions());
            view.updateSuccessfulCount(historyService.countByStatus(1));
            view.updateFailedCount(historyService.countByStatus(2));
            view.updateAvgDurationCount(historyService.averageDuration());
        } catch (Exception e) {
            logger.severe("Error al cargar el historial: " + e.getMessage());
            try {
                Message.showError(
                        "Error",
                        "No se pudo cargar el historial"
                );
            } catch (Exception msgError) {
                logger.warning("No se pudo mostrar el mensaje de error: " + msgError.getMessage());
            }
        }
    }

    public void handleClearHistory() {
        Notification.confirmDelete(
                "el historial",
                () -> {
                    try {
                        historyService.delete();

                        Message.showSuccess(
                                "Historial eliminado",
                                "El historial se eliminó correctamente"
                        );
                    } catch (Exception e) {
                        logger.severe("Error al eliminar el historial: " + e.getMessage());
                        Message.showError(
                                "Error",
                                "No se pudo eliminar el historial"
                        );
                    }
                }
        );
    }

    public void handleShowDetails(History history) {
        try {
            HistoryDialog dialog = new HistoryDialog(history);

            dialog.showAndWait();
        } catch (Exception e) {
            logger.severe("Error al abrir el modal de detalles: " + e.getMessage());
        }
    }

    public void handleClearFilter() {
        view.getStartDatePicker().setValue(LocalDate.now().minusDays(30));
        view.getEndDatePicker().setValue(LocalDate.now());
        view.getStatusFilter().setValue("Todos");
        view.getTypeFilter().setValue("Todos");
    }
}
