package com.x4yi.x4tweaker.automation;

import java.util.Comparator;

public class TaskTicket {
    public static final Comparator<TaskTicket> COMPARATOR = Comparator
        .comparingInt((TaskTicket t) -> t.priority.ordinal()).reversed()
        .thenComparingLong(t -> t.createdAt);

    private final BotTask task;
    private final TaskPriority priority;
    private final long createdAt;

    public TaskTicket(BotTask task, TaskPriority priority) {
        this.task      = task;
        this.priority  = priority;
        this.createdAt = System.nanoTime();
    }

    public BotTask getTask()          { return task; }
    public TaskPriority getPriority() { return priority; }
    public long getCreatedAt()        { return createdAt; }
}
