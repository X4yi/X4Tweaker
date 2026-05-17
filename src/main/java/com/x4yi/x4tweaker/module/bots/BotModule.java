package com.x4yi.x4tweaker.module.bots;

import com.x4yi.x4tweaker.automation.BotTask;


public interface BotModule {
    String getBotStatus();

    BotTask createTask();

    boolean isTaskRunning();
}
