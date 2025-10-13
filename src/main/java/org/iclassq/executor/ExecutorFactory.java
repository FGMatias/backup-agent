package org.iclassq.executor;

import org.iclassq.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ExecutorFactory {
    private static final Logger logger = Logger.getLogger(ExecutorFactory.class.getName());

    @Autowired
    private ApplicationContext context;

    public TaskExecutorStrategy getExecutor(Task task) {
        int typeId = task.getType().getId();

        String beanName = switch (typeId) {
            case 1 -> "databaseBackupExecutor";
            case 2 -> "fileBackupExecutor";
            case 3 -> "moveFilesExecutor";
            case 4 -> "cleanFolderExecutor";
            default -> throw new IllegalArgumentException("Tipo de tarea no valido: " + typeId);
        };

        logger.info("Obteniendo ejecutor: " + beanName + " para tarea tipo " + task.getType().getDescription());
        return context.getBean(beanName, TaskExecutorStrategy.class);
    }
}
