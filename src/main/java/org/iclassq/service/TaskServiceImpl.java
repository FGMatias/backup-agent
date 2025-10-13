package org.iclassq.service;

import jakarta.validation.ValidationException;
import org.iclassq.entity.Task;
import org.iclassq.repository.FrequencyRepository;
import org.iclassq.repository.TaskRepository;
import org.iclassq.repository.TypeTaskRepository;
import org.iclassq.validation.TaskValidator;
import org.iclassq.views.components.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class TaskServiceImpl implements TaskService {
    private final Logger logger = Logger.getLogger(TaskServiceImpl.class.getName());

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Long count() {
        logger.info("Obteniendo cantidad de tareas");
        return taskRepository.count();
    }

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> findByIsActive(Boolean isActive) {
        return taskRepository.findByIsActive(isActive);
    }

    @Override
    public Task findById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Tarea no encontrada"));
    }

    @Override
    @Transactional
    public Task save(Task task) {
        logger.info("Guardando nueva tarea");

        Map<String, String> errors = TaskValidator.validate(task);

        if (!errors.isEmpty()) {
            logger.warning("Errores de validacion: " + errors);
            Message.showValidationErrors("Errores de validación", errors);
        }

        if (taskRepository.existsByNameAndSourcePath(task.getName(), task.getSourcePath())) {
            Message.showError("Error","Ya existe una tarea con ese nombre y ruta");
        }

        Task savedTask = taskRepository.save(task);
        logger.info("Tarea guardada");

        return savedTask;
    }

    @Override
    @Transactional
    public Task update(Task task) {
        logger.info("Editando tarea con id " + task.getId());

        if (!taskRepository.existsById(task.getId())) {
            Message.showValidationInfo("Ups!", "La tarea no existe");
        }

        Map<String, String> errors = TaskValidator.validate(task);

        if (!errors.isEmpty()) {
            logger.warning("Errores de validacion: " + errors);
            Message.showValidationErrors("Errores de validación", errors);
        }

        Task updatedTask = taskRepository.save(task);
        logger.info("Tarea actualizada");

        return updatedTask;
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        logger.info("Eliminando tarea con id: " + id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Tarea no encontrada"));

        if (task.getIsActive() != null && task.getIsActive()) {
            Message.showValidationInfo("Ups!", "No se puede eliminar una tarea activa");
        }

        taskRepository.delete(task);
        logger.info("Tarea eliminada exitosamente");
    }
}
