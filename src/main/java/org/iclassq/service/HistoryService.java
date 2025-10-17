package org.iclassq.service;

import org.iclassq.entity.History;

import java.util.List;

public interface HistoryService {
    Long count();
    Long countByStatus(Integer status);
    Long countCompletedToday();
    Long countByTypeToday(Integer typeId);
    Long countFilesByTypeToday(Integer typeId);
    String getTotalSizeToday();
    String getAverageDurationFormatted();
    List<History> findAll();
    History findById(Integer id);
    void delete();
}
