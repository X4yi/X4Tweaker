package com.x4yi.x4tweaker.automation.context;

public class AutomationContext {
    private final PlayerStateSnapshot snapshot = new PlayerStateSnapshot();
    private String status = "idle";

    public PlayerStateSnapshot getSnapshot() {
        return snapshot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
