package org.iclassq.controller;

import jakarta.annotation.PostConstruct;
import org.iclassq.service.TaskService;
import org.iclassq.views.TaskContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class TaskController {
    private static final Logger logger = Logger.getLogger(TaskController.class.getName());
    private TaskContent view;

    private Map<String, Integer> statusMap = new HashMap<>();

    @Autowired
    private TaskService taskService;

    public TaskController(TaskContent view) {
        this.view = view;
    }

    @PostConstruct
    private void init() {
        setupEventHandlers();
        loadStatus();
        countTasks();
    }

    private void setupEventHandlers() {
        view.getBtnSearch().setOnMouseClicked(evt -> applyFilters());
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

    private void countTasks() {
        try {
            Long count = taskService.count();
            String result = count.toString().concat(" tareas");

            view.getTotalTasksLabel().setText(result);

            logger.info("Total de tareas: " + result);
        } catch (Exception e) {
            logger.severe("Error al obtener la cantidad de tareas: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void applyFilters() {
        String selectedStatus = view.getCboStatus().getValue();

        if (selectedStatus != null) {
            Integer statusId = statusMap.get(selectedStatus);

            if (statusId == 0) {
                logger.info("Traer todas las tareas");
            } else {
                logger.info("Filtrar tareas por estado: " + statusId + " - " + selectedStatus);
            }
        }
    }
}
