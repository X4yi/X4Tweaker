package com.x4yi.x4tweaker.module.bots;

import com.x4yi.x4tweaker.automation.BotTask;
import com.x4yi.x4tweaker.module.Module;

import java.util.List;


public interface BotModule {
    String getBotStatus();

    BotTask createTask();

    boolean isTaskRunning();

    List<Class<? extends Module>> getIncompatibilities();
}
