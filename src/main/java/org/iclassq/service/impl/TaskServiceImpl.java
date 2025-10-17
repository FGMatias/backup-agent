package org.iclassq.service.impl;

import jakarta.validation.ValidationException;
import org.iclassq.entity.Task;
import org.iclassq.repository.TaskRepository;
import org.iclassq.service.TaskService;
import org.iclassq.validation.TaskValidator;
import org.iclassq.views.components.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
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
    public Long countActive() {
        return taskRepository.countByIsActive(true);
    }

    @Override
    public Task findNextScheduledTask() {
        try {
            List<Task> activeTasks = taskRepository.findByIsActive(true);

            if (activeTasks.isEmpty()) return null;

            LocalTime now = LocalTime.now();

            Task nextTask = activeTasks.stream()
                    .filter(task -> task.getScheduleTime() != null)
                    .filter(task -> task.getFrequency() != null && task.getFrequency().getId() != 1)
                    .min((t1, t2) -> {
                        LocalTime time1 = t1.getScheduleTime();
                        LocalTime time2 = t2.getScheduleTime();

                        boolean t1PassedToday = time1.isBefore(now);
                        boolean t2PassedToday = time2.isBefore(now);

                        if (t1PassedToday && !t2PassedToday) {
                            return 1;
                        } else if (!t1PassedToday && t2PassedToday) {
                            return -1;
                        } else {
                            return time1.compareTo(time2);
                        }
                    })
                    .orElse(null);

            return nextTask;
        } catch (Exception e) {
            logger.severe("Error al buscar la próxima tarea: " + e.getMessage());
            return null;
        }
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
