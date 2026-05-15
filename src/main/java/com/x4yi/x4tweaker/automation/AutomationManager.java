package com.x4yi.x4tweaker.automation;

import com.x4yi.x4tweaker.automation.context.AutomationContext;
import com.x4yi.x4tweaker.automation.control.InputController;
import com.x4yi.x4tweaker.automation.control.InventoryController;

import java.util.PriorityQueue;

import net.minecraft.client.Minecraft;

public class AutomationManager {
    private final PriorityQueue<TaskTicket> queue = new PriorityQueue<>(TaskTicket.COMPARATOR);

    private BotTask currentTask = null;
    private AutomationState state = AutomationState.IDLE;
    private String lastStatusMessage = "idle";

    private final AutomationContext context          = new AutomationContext();
    private final InputController inputController    = new InputController();
    private final InventoryController inventoryController = new InventoryController();

    public void init() {
    }

    public void onUpdate() {
        if (Minecraft.getMinecraft().player == null) return;

        if (currentTask != null && currentTask.isActive()) {
            try {
                currentTask.onUpdate();
                state = AutomationState.RUNNING;
                lastStatusMessage = "running:" + currentTask.getName();
            } catch (Exception e) {
                System.err.println("[X4Tweaker] Tarea fallida: " + e.getMessage());
                e.printStackTrace();
                state = AutomationState.FAILED;
                lastStatusMessage = "failed:" + currentTask.getName();
                try {
                    currentTask.stop();
                } catch (Exception stopEx) {
                    System.err.println("[X4Tweaker] Error en stop de tarea: " + stopEx.getMessage());
                    currentTask = null;
                }
            }
            return;
        }

        TaskTicket next = queue.poll();
        if (next != null) {
            startTask(next.getTask(), next.getPriority(), true);
            return;
        }

        if (state != AutomationState.IDLE) {
            state = AutomationState.IDLE;
            lastStatusMessage = "idle";
        }
    }

    public void startTask(BotTask task) {
        startTask(task, TaskPriority.NORMAL, true);
    }

    public void startTask(BotTask task, TaskPriority priority, boolean allowInterrupt) {
        if (task == null) return;

        if (currentTask == null || !currentTask.isActive()) {
            currentTask = task;
            currentTask.start();
            state = AutomationState.RUNNING;
            lastStatusMessage = "running:" + task.getName();
            return;
        }

        if (allowInterrupt && priority.ordinal() > TaskPriority.NORMAL.ordinal()) {
            state = AutomationState.INTERRUPTED;
            lastStatusMessage = "interrupted:" + currentTask.getName();
            currentTask.stop();
            currentTask = task;
            currentTask.start();
            state = AutomationState.RUNNING;
            lastStatusMessage = "running:" + task.getName();
            return;
        }

        queue.add(new TaskTicket(task, priority));
    }

    public void stopCurrentTask() {
        if (currentTask != null) {
            if (currentTask.isActive()) {
                currentTask.stop();
            }
            currentTask = null;
        }
        state = AutomationState.IDLE;
        lastStatusMessage = "idle";
    }

    public void stopTaskOf(BotTask task) {
        if (task == null) return;
        if (currentTask == task) {
            stopCurrentTask();
        }
    }

    public void clearQueuedTasks() {
        queue.clear();
    }

    public void shutdown() {
        if (currentTask != null && currentTask.isActive()) {
            try {
                currentTask.stop();
            } catch (Exception e) {
                System.err.println("[X4Tweaker] Error deteniendo tarea actual: " + e.getMessage());
            }
            currentTask = null;
        }
        clearQueuedTasks();
        try {
            inputController.unlockInput();
            inventoryController.restoreSlot();
        } catch (Exception e) {
            System.err.println("[X4Tweaker] Error limpiando controladores: " + e.getMessage());
        }
        state = AutomationState.IDLE;
        lastStatusMessage = "idle";
    }

    public int getQueuedTaskCount()         { return queue.size(); }
    public AutomationState getState()       { return state; }
    public String getLastStatusMessage()    { return lastStatusMessage; }
    public BotTask getCurrentTask()         { return currentTask; }
    public AutomationContext getContext()   { return context; }
    public InputController getInputController()       { return inputController; }
    public InventoryController getInventoryController(){ return inventoryController; }
}
