package com.x4yi.x4tweaker.automation.task.afk;

import com.x4yi.x4tweaker.automation.BotTask;

public class WaitTask extends BotTask {
    private final int waitTicks;
    private int elapsed;

    public WaitTask(int waitTicks) {
        this.waitTicks = Math.max(1, waitTicks);
    }

    @Override
    public String getName() {
        return "WaitTask";
    }

    @Override
    public void onStart() {
        elapsed = 0;
    }

    @Override
    public void onUpdate() {
        elapsed++;
        if (elapsed >= waitTicks) {
            stop();
        }
    }

    @Override
    public void onStop() {
    }
}
