package org.iclassq.scheduler;

import org.iclassq.entity.Task;
import org.iclassq.executor.TaskExecutor;
import org.iclassq.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

@Component
public class TaskScheduler {
    private static final Logger logger = Logger.getLogger(TaskScheduler.class.getName());

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Scheduled(cron = "0 * * * * *")
    public void checkScheduledTasks() {
        logger.fine("Verificando tareas programadas");

        try {
            List<Task> activeTasks = taskService.findByIsActive(true);

            LocalTime now = LocalTime.now();
            LocalTime currentMinute = LocalTime.of(now.getHour(), now.getMinute());

            for (Task task : activeTasks) {
                if (shouldExecuteTask(task, currentMinute)) {
                    logger.info("Tarea programada encontrada: " + task.getName());

                    new Thread(() -> {
                        try {
                            taskExecutor.executeTask(task);
                        } catch (Exception e) {
                            logger.severe("Error al ejecutar la tarea: " + e.getMessage());
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            logger.severe("Error en el scheduler: " + e.getMessage());
        }
    }

    private boolean shouldExecuteTask(Task task, LocalTime currentTime) {
        if (task.getFrequency().getId() == 1) {
            return false;
        }

        if (task.getScheduleTime() == null) {
            return false;
        }

        LocalTime taskTime = LocalTime.of(
                task.getScheduleTime().getHour(),
                task.getScheduleTime().getMinute()
        );

        return taskTime.equals(currentTime);
    }

    public void executeTaskManually(Task task) {
        logger.info("Ejecución manual de la tarea: " + task.getName());

        new Thread(() -> {
            try {
                taskExecutor.executeTask(task);
            } catch (Exception e) {
                logger.severe("Error al ejecutar tarea manualmente: " + e.getMessage());
            }
        }).start();
    }
}
