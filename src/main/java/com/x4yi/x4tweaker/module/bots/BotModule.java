package com.x4yi.x4tweaker.module.bots;

import com.x4yi.x4tweaker.automation.BotTask;

/**
 * Interface for modules that manage bot tasks.
 * Note: getIncompatibilities() is inherited from Module base class.
 */
public interface BotModule {
    String getBotStatus();

    BotTask createTask();

    boolean isTaskRunning();
}
