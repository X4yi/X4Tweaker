package com.x4yi.x4tweaker.automation;

import net.minecraft.client.Minecraft;

public abstract class BotTask {
    protected final Minecraft mc = Minecraft.getMinecraft();

    public enum TaskState {
        IDLE,
        RUNNING,
        COMPLETED,
        FAILED
    }

    private TaskState state = TaskState.IDLE;
    private String statusMessage = "";

    public abstract String getName();

    public abstract void onStart();
    public abstract void onUpdate();
    public abstract void onStop();

    public boolean isActive() {
        return state == TaskState.RUNNING;
    }

    public TaskState getState() {
        return state;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    protected void setStatusMessage(String msg) {
        this.statusMessage = msg;
    }

    public void start() {
        if (state != TaskState.RUNNING) {
            state = TaskState.RUNNING;
            statusMessage = "";
            onStart();
        }
    }

    public void stop() {
        if (state == TaskState.RUNNING) {
            state = TaskState.COMPLETED;
            onStop();
        }
    }

    public void fail(String reason) {
        state = TaskState.FAILED;
        statusMessage = reason;
        try {
            onStop();
        } catch (Exception ignored) {
        }
    }
}
