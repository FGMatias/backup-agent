package org.iclassq.service;

import org.iclassq.entity.Task;
import java.util.List;

public interface TaskService {
    Long count();
    Long countActive();
    Task findNextScheduledTask();
    List<Task> findAll();
    List<Task> findByIsActive(Boolean isActive);
    Task findById(Integer id);
    Task save(Task task);
    Task update(Task task);
    void delete(Integer id);

}
