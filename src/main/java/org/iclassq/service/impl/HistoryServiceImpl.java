package org.iclassq.service.impl;

import jakarta.validation.ValidationException;
import org.iclassq.entity.History;
import org.iclassq.repository.HistoryRepository;
import org.iclassq.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Service
public class HistoryServiceImpl implements HistoryService {
    private final Logger logger = Logger.getLogger(HistoryServiceImpl.class.getName());
    LocalDate date = LocalDate.now();

    @Autowired
    private HistoryRepository historyRepository;

    @Override
    public Long countExecutions() {
        return historyRepository.countByCreatedAt_Date(date);
    }

    @Override
    public Long countByStatus(Integer status) {
        return historyRepository.countByStatus_Id(status);
    }

    @Override
    public Long averageDuration() {
        return 0L;
    }

    @Override
    public List<History> findAll() {
        return historyRepository.findAll();
    }

    @Override
    public History findById(Integer id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Tarea no encontrada"));
    }

    @Override
    @Transactional
    public void delete() {
        historyRepository.deleteAll();
    }
}
