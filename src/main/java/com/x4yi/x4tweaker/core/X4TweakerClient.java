package com.x4yi.x4tweaker.core;

import com.x4yi.x4tweaker.X4Tweaker;
import com.x4yi.x4tweaker.automation.AutomationManager;
import com.x4yi.x4tweaker.manager.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class X4TweakerClient {
    private static final Logger LOGGER = LogManager.getLogger(X4Tweaker.NAME);

    private static volatile X4TweakerClient instance;

    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private KeybindManager keybindManager;
    private EventManager eventManager;
    private ThemeManager themeManager;
    private AutomationManager automationManager;

    private X4TweakerClient() {}

    public static X4TweakerClient getInstance() {
        if (instance == null) {
            synchronized (X4TweakerClient.class) {
                if (instance == null) {
                    instance = new X4TweakerClient();
                }
            }
        }
        return instance;
    }

    public void start() {
        LOGGER.info("Iniciando cliente...");

        eventManager     = new EventManager();
        themeManager     = new ThemeManager();
        moduleManager    = new ModuleManager();
        configManager    = new ConfigManager();
        keybindManager   = new KeybindManager();
        automationManager = new AutomationManager();

        moduleManager.init();
        keybindManager.init();
        themeManager.load();
        configManager.init();
        automationManager.init();

        LOGGER.info("Inicialización completada.");
    }

    public void init() {
        LOGGER.info("Post-inicialización completada.");
    }

    public void shutdown() {
        configManager.save();
        themeManager.save();
        automationManager.shutdown();
        LOGGER.info("Apagado completado.");
    }

    public ModuleManager    getModuleManager()    { return moduleManager; }
    public ConfigManager    getConfigManager()    { return configManager; }
    public KeybindManager   getKeybindManager()   { return keybindManager; }
    public EventManager     getEventManager()     { return eventManager; }
    public ThemeManager     getThemeManager()     { return themeManager; }
    public AutomationManager getAutomationManager() { return automationManager; }
}
