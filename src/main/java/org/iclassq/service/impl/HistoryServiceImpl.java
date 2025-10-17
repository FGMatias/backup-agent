package org.iclassq.service.impl;

import jakarta.validation.ValidationException;
import org.iclassq.entity.History;
import org.iclassq.repository.HistoryRepository;
import org.iclassq.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class HistoryServiceImpl implements HistoryService {
    private final Logger logger = Logger.getLogger(HistoryServiceImpl.class.getName());

    @Autowired
    private HistoryRepository historyRepository;

    @Override
    public Long count() {
        return historyRepository.count();
    }

    @Override
    public Long countByStatus(Integer status) {
        return historyRepository.countByStatus_Id(status);
    }

    @Override
    public Long countCompletedToday() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return historyRepository.findByCreatedAtBetween(startOfDay, endOfDay).stream()
                .filter(history -> history.getStatus() != null && history.getStatus().getId() == 1)
                .count();
    }

    @Override
    public Long countByTypeToday(Integer typeId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return historyRepository.findByCreatedAtBetween(startOfDay, endOfDay).stream()
                .filter(history -> history.getTypeTask() != null && history.getTypeTask().getId() == typeId)
                .count();
    }

    @Override
    public Long countFilesByTypeToday(Integer typeId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return historyRepository.findByCreatedAtBetween(startOfDay, endOfDay).stream()
                .filter(history -> history.getTypeTask() != null && history.getTypeTask().getId() == typeId)
                .mapToLong(history -> history.getFileCount() != null ? history.getFileCount() : 0)
                .sum();
    }

    @Override
    public String getTotalSizeToday() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        long totalBytes = historyRepository.findByCreatedAtBetween(startOfDay, endOfDay).stream()
                .mapToLong(history -> history.getSize() != null ? history.getSize() : 0)
                .sum();

        return formatFileSize(totalBytes);
    }

    private String formatFileSize(long bytes) {
        if (bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @Override
    public String getAverageDurationFormatted() {
        try {
            List<History> allHistory = historyRepository.findAll();

            if (allHistory.isEmpty()) return "0 s";

            long totalSeconds = 0;
            int validCount = 0;

            for (History history : allHistory) {
                if (history.getStartTime() != null && history.getEndTime() != null) {
                    Duration duration = Duration.between(history.getStartTime(), history.getEndTime());
                    totalSeconds += duration.getSeconds();
                    validCount++;
                }
            }

            if (validCount == 0) return "0 s";

            long avgSeconds = totalSeconds / validCount;

            return formatDuration(avgSeconds);
        } catch (Exception e) {
            logger.severe("Error calculando la duracion promedio: " + e.getMessage());
            return "0 s";
        }
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return String.format("%d min %d s", minutes, secs);
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return String.format("%d h %d min", hours, minutes);
        }
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
