package org.iclassq.repository;

import org.iclassq.entity.History;
import org.iclassq.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Integer> {
    Long countByStatus_Id(int statusId);

    List<History> findByTaskId(Task taskId);

    List<History> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
