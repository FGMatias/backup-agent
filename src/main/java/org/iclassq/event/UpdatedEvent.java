package org.iclassq.event;

import org.iclassq.entity.History;
import org.iclassq.entity.Task;
import org.springframework.context.ApplicationEvent;

public class UpdatedEvent extends ApplicationEvent {
    private final Task task;
    private final History history;
    private EventType eventType;

    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }

    public UpdatedEvent(Object source, History history, EventType eventType, Task task) {
        super(source);
        this.history = history;
        this.eventType = eventType;
        this.task = task;
    }

    public UpdatedEvent(Object source, Task task, EventType eventType) {
        this(source, null, eventType, task);
    }

    public UpdatedEvent(Object source, History history, EventType eventType) {
        this(source, history, eventType, null);
    }

    public Task getTask() {
        return task;
    }

    public History getHistory() {
        return history;
    }

    public EventType getEventType() {
        return eventType;
    }
}
