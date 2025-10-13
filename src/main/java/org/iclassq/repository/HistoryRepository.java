package org.iclassq.repository;

import org.iclassq.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Integer> {
    List<History> findByTaskId(int taskId);
    List<History> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
