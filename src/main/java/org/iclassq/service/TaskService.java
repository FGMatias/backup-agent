package org.iclassq.service;

import org.iclassq.entity.Frequency;
import org.iclassq.entity.Task;
import org.iclassq.entity.TypeTask;

import java.util.List;

public interface TaskService {
    Long count();
    List<Task> findAll();
    List<Task> findByIsActive(Boolean isActive);
    Task findById(Integer id);
    Task save(Task task);
    Task update(Task task);
    void delete(Integer id);

}
