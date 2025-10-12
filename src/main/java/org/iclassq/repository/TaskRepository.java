package org.iclassq.repository;

import org.iclassq.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    boolean existsByNameAndSourcePath(String name, String sourcePath);
    List<Task> findByIsActive(Boolean isActive);
    List<Task> findByNameContainingIgnoreCase(String name);
}
