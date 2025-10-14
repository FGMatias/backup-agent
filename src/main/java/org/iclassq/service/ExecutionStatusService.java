package org.iclassq.service;

import org.iclassq.entity.ExecutionStatus;

import java.util.List;

public interface ExecutionStatusService {
    List<ExecutionStatus> findAll();
}
