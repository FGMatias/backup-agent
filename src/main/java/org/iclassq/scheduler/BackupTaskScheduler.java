package org.iclassq.scheduler;

import org.iclassq.entity.Task;
import org.iclassq.executor.TaskExecutor;
import org.iclassq.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class BackupTaskScheduler {
    private static final Logger logger = Logger.getLogger(BackupTaskScheduler.class.getName());

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskExecutor taskExecutor;

    private final Map<Integer, LocalDateTime> lastExecutionMap = new HashMap<>();

    @Scheduled(cron = "0 * * * * *")
    public void checkScheduledTasks() {
        logger.fine("Verificando tareas programadas");

        try {
            List<Task> activeTasks = taskService.findByIsActive(true);

            LocalDateTime now = LocalDateTime.now();
            LocalTime currentMinute = LocalTime.of(now.getHour(), now.getMinute());
            LocalDate today = now.toLocalDate();

            for (Task task : activeTasks) {
                if (shouldExecuteTask(task, currentMinute, today, now)) {
                    logger.info("Tarea programada encontrada: " + task.getName());

                    lastExecutionMap.put(task.getId(), now);
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

    private boolean shouldExecuteTask(
            Task task,
            LocalTime currentTime,
            LocalDate today,
            LocalDateTime now
    ) {
        if (task.getFrequency() == null) return false;

        int frequencyId = task.getFrequency().getId();

        if (frequencyId == 1) return false;

        if (task.getScheduleTime() == null) return false;

        LocalTime taskTime = LocalTime.of(
                task.getScheduleTime().getHour(),
                task.getScheduleTime().getMinute()
        );

        if (!taskTime.equals(currentTime)) return false;

        LocalDateTime lastExecution = lastExecutionMap.get(task.getId());

        if (lastExecution != null) {
            if (now.minusMinutes(2).isBefore(lastExecution)) {
                logger.fine("Tarea " + task.getName() + " ya se ejecuto recientemente");
                return false;
            }
        }

        if (frequencyId == 2) {
            logger.info("Tarea diaria detectada: " + task.getName());
            return true;
        }

        if (frequencyId == 3) {
            DayOfWeek dayOfWeek = today.getDayOfWeek();
            boolean isMonday = dayOfWeek == DayOfWeek.MONDAY;

            return isMonday;
        }

        return false;
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

    public void clearExecutionHistory() {
        lastExecutionMap.clear();
    }

    public LocalDateTime getLastExecution(int taskId) {
        return lastExecutionMap.get(taskId);
    }
}
