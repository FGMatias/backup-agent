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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
        view.setOnSearch(this::applyFilters);
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
        } catch (Exception e) {
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

        } catch (Exception e) {
            Message.showError(
                    "Error",
                    "No se pudieron cargar los tipos de tarea"
            );
        }
    }

    public void loadInitialData() {
        try {
            view.refreshTable(historyService.findAll());
            view.updateExecutionsCount(historyService.count());
            view.updateSuccessfulCount(historyService.countByStatus(1));
            view.updateFailedCount(historyService.countByStatus(2));
            view.updateAvgDurationCount(historyService.getAverageDurationFormatted());
        } catch (Exception e) {
            try {
                Message.showError(
                        "Error",
                        "No se pudo cargar el historial"
                );
            } catch (Exception msgError) {
                msgError.printStackTrace();
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
            e.printStackTrace();
        }
    }

    public void handleClearFilter() {
        view.getStartDatePicker().setValue(LocalDate.now().minusDays(30));
        view.getEndDatePicker().setValue(LocalDate.now());
        view.getStatusFilter().setValue("Todos");
        view.getTypeFilter().setValue("Todos");
        loadInitialData();
    }

    public void applyFilters() {
        try {
            LocalDate startDate = view.getStartDatePicker().getValue();
            LocalDate endDate = view.getEndDatePicker().getValue();
            String selectedStatus = view.getStatusFilter().getValue();
            String selectedType = view.getTypeFilter().getValue();

            if (startDate == null || endDate == null) {
                Message.showWarning(
                        "Fechas requeridas",
                        "Debes seleccionar un rango de fechas"
                );
                return;
            }

            if (startDate.isAfter(endDate)) {
                Message.showWarning(
                        "Fechas inválidas",
                        "La fecha de inicio no puede ser posterior a la fecha de fin"
                );
                return;
            }

            List<History> allHistory = historyService.findAll();
            List<History> filteredHistory = allHistory;

            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            filteredHistory = filteredHistory.stream()
                    .filter(h -> h.getCreatedAt() != null)
                    .filter(h -> !h.getCreatedAt().isBefore(startDateTime) && !h.getCreatedAt().isAfter(endDateTime))
                    .collect(Collectors.toList());

            if (selectedStatus != null && !selectedStatus.equals("Todos")) {
                Integer statusId = statusMap.get(selectedStatus);

                if (statusId != null && statusId != 0) {
                    filteredHistory = filteredHistory.stream()
                            .filter(h -> h.getStatus() != null && h.getStatus().getId() == statusId)
                            .collect(Collectors.toList());
                }
            }

            if (selectedType != null && !selectedType.equals("Todos")) {
                Integer typeId = typesTasksMap.get(selectedType);

                if (typeId != null && typeId != 0) {
                    filteredHistory = filteredHistory.stream()
                            .filter(h -> h.getTypeTask() != null && h.getTypeTask().getId() == typeId)
                            .collect(Collectors.toList());
                }
            }

            view.refreshTable(filteredHistory);

            updateCardsWithFilteredData(filteredHistory);

            if (filteredHistory.isEmpty()) {
                Message.showInformation(
                        "Sin resultados",
                        "No se encontraron registros que coincidan con los filtros"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            Message.showError(
                    "Error",
                    "No se pudieron aplicar los filtros"
            );
        }
    }

    private void updateCardsWithFilteredData(List<History> filteredHistory) {
        try {
            view.updateExecutionsCount(filteredHistory.size());

            long successful = filteredHistory.stream()
                    .filter(h -> h.getStatus() != null && h.getStatus().getId() == 1)
                    .count();
            view.updateSuccessfulCount(successful);

            long failed = filteredHistory.stream()
                    .filter(h -> h.getStatus() != null && h.getStatus().getId() == 2)
                    .count();
            view.updateFailedCount(failed);

            String avgDuration = calculateAverageDuration(filteredHistory);
            view.updateAvgDurationCount(avgDuration);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String calculateAverageDuration(List<History> histories) {
        if (histories.isEmpty()) {
            return "0 s";
        }

        long totalSeconds = 0;
        int validCount = 0;

        for (History h : histories) {
            if (h.getStartTime() != null && h.getEndTime() != null) {
                java.time.Duration duration = java.time.Duration.between(h.getStartTime(), h.getEndTime());
                totalSeconds += duration.getSeconds();
                validCount++;
            }
        }

        if (validCount == 0) {
            return "0 s";
        }

        long avgSeconds = totalSeconds / validCount;

        if (avgSeconds < 60) {
            return avgSeconds + " s";
        } else if (avgSeconds < 3600) {
            long minutes = avgSeconds / 60;
            long secs = avgSeconds % 60;
            return String.format("%d min %d s", minutes, secs);
        } else {
            long hours = avgSeconds / 3600;
            long minutes = (avgSeconds % 3600) / 60;
            return String.format("%d h %d min", hours, minutes);
        }
    }
}
