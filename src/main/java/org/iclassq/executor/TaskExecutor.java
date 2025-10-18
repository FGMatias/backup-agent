package org.iclassq.executor;

import org.iclassq.entity.ExecutionStatus;
import org.iclassq.entity.History;
import org.iclassq.entity.Task;
import org.iclassq.event.UpdatedEvent;
import org.iclassq.repository.ExecutionStatusRepository;
import org.iclassq.repository.HistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Service
public class TaskExecutor {
    private static final Logger logger = Logger.getLogger(TaskExecutor.class.getName());

    @Autowired
    private ExecutorFactory executorFactory;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ExecutionStatusRepository executionStatusRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void executeTask(Task task) {
        logger.info("========================================");
        logger.info("Ejecutando tarea: " + task.getName());
        logger.info("Tipo: " + task.getType().getDescription());
        logger.info("========================================");

        try {
            TaskExecutorStrategy executor = executorFactory.getExecutor(task);

            ExecutionResult result = executor.execute(task);

            saveToHistory(task, result);

            logger.info("Tarea completada: " + task.getName() + " - " + result.getMessage());
        } catch (Exception e) {
            logger.severe("Error al ejecutar tarea " + task.getName() + ": " + e.getMessage());

            ExecutionResult errorResult = new ExecutionResult(false, "Error: " + e.getMessage());
            errorResult.setStatusId(2);
            errorResult.finish();
            saveToHistory(task, errorResult);
        }
    }

    private void saveToHistory(Task task, ExecutionResult result) {
        try {
            History history = new History();
            history.setTaskId(task);
            history.setMessage(result.getMessage());
            history.setTypeTask(task.getType());
            history.setStartTime(result.getStartTime());
            history.setEndTime(result.getEndTime());
            history.setDuration(result.getDuration());
            history.setSize(result.getSize());
            history.setFileCount(result.getFileCount());
            history.setCreatedAt(LocalDateTime.now());

            ExecutionStatus status = executionStatusRepository.findById(result.getStatusId())
                    .orElse(null);

            history.setStatus(status);

            History savedHistory = historyRepository.save(history);
            logger.info("Historial guardado para la tarea: " + task.getName());

            eventPublisher.publishEvent(
                    new UpdatedEvent(this, savedHistory, UpdatedEvent.EventType.CREATED)
            );
        } catch (Exception e) {
            logger.severe("Error al guardar el historial: " + e.getMessage());
        }
    }
}
