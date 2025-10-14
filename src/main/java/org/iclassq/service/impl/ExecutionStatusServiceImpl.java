package org.iclassq.service.impl;

import org.iclassq.entity.ExecutionStatus;
import org.iclassq.repository.ExecutionStatusRepository;
import org.iclassq.service.ExecutionStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionStatusServiceImpl implements ExecutionStatusService {

    @Autowired
    private ExecutionStatusRepository executionStatusRepository;

    @Override
    public List<ExecutionStatus> findAll() {
        return executionStatusRepository.findAll();
    }
}
