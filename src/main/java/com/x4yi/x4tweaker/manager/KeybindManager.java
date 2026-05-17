package com.x4yi.x4tweaker.manager;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.module.Module;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class KeybindManager {

    public void init() {
    }

    public void handleKey(int key) {
        if (key == Keyboard.KEY_NONE) return;
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module m = modules.get(i);
            if (m.getKeybind() == key) {
                m.toggle();
            }
        }
    }
}
