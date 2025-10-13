package org.iclassq.executor;

import org.iclassq.entity.Task;

public interface TaskExecutorStrategy {
    ExecutionResult execute(Task task);
}
