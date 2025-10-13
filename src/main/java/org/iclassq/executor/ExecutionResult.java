package org.iclassq.executor;

import java.time.Duration;
import java.time.LocalTime;

public class ExecutionResult {
    private boolean success;
    private String message;
    private LocalTime startTime;
    private LocalTime endTime;
    private String duration;
    private String size;
    private int statusId;

    public ExecutionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.startTime = LocalTime.now();
    }

    public void finish() {
        this.endTime = LocalTime.now();
        calculateDuration();
    }

    private void calculateDuration() {
        if (startTime != null && endTime != null) {
            long seconds = Duration.between(startTime, endTime).getSeconds();
            long minutes = seconds / 60;
            long secs = seconds % 60;
            this.duration = String.format("%d:%d", minutes, secs);
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }
}
