package org.iclassq.repository;

import org.iclassq.entity.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionStatusRepository extends JpaRepository<ExecutionStatus, Integer> {
}
