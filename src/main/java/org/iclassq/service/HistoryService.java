package org.iclassq.service;

import org.iclassq.entity.History;

import java.util.List;

public interface HistoryService {
    Long countExecutions();
    Long countByStatus(Integer status);
    Long averageDuration();
    List<History> findAll();
    History findById(Integer id);
    void delete();
}
