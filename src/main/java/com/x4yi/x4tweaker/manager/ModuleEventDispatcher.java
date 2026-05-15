package com.x4yi.x4tweaker.manager;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.event.Event;
import com.x4yi.x4tweaker.module.Module;

import java.util.List;

public class ModuleEventDispatcher {

    public void dispatchUpdate(Event event) {
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module module = modules.get(i);
            if (!module.isEnabled()) continue;
            module.onUpdate();
            module.onEvent(event);
        }
    }

    public void dispatchRender2D(Event event) {
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module module = modules.get(i);
            if (!module.isEnabled()) continue;
            module.onRender2D();
            module.onEvent(event);
        }
    }

    public void dispatchRender3D(Event event) {
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module module = modules.get(i);
            if (!module.isEnabled()) continue;
            module.onRender3D();
            module.onEvent(event);
        }
    }

    public void dispatchEventOnly(Event event) {
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module module = modules.get(i);
            if (!module.isEnabled()) continue;
            module.onEvent(event);
        }
    }
}
