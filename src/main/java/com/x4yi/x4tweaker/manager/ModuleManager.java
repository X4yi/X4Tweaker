package com.x4yi.x4tweaker.manager;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.module.bots.BetterAFK;
import com.x4yi.x4tweaker.module.client.ClickGUIModule;
import com.x4yi.x4tweaker.module.combat.KillAuraLegit;
import com.x4yi.x4tweaker.module.random_tweaks.FastCrafting;
import com.x4yi.x4tweaker.module.tweaks.AutoSprint;
import com.x4yi.x4tweaker.module.visuals.*;
import com.x4yi.x4tweaker.module.utility.Freecam;
import com.x4yi.x4tweaker.module.utility.CameraDetach;
import com.x4yi.x4tweaker.module.utility.MobInfo;
import com.x4yi.x4tweaker.module.utility.ContainerPreview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        register(
            new Fullbright(),
            new ChestESP(),
            new PlayerESP(),
            new MobESP(),
            new ActiveTweaks(),
            new AutoSprint(),
            new KillAuraLegit(),
            new FastCrafting(),
            new ClickGUIModule(),
            new BetterAFK(),
            new Freecam(),
            new CameraDetach(),
            new MobInfo(),
            new ContainerPreview()
        );
    }

    private void register(Module... mods) {
        for (Module m : mods) {
            if (m != null) modules.add(m);
        }
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
            .filter(m -> m.getCategory() == category)
            .collect(Collectors.toList());
    }

    public Module getModuleByName(String name) {
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module m = modules.get(i);
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module m = modules.get(i);
            if (clazz.isInstance(m)) return clazz.cast(m);
        }
        return null;
    }

    public void onModuleEnabled(Module module) {
        List<Class<? extends Module>> incompatibilities = module.getIncompatibilities();
        if (incompatibilities.isEmpty()) return;
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module current = modules.get(i);
            if (current == module || !current.isEnabled()) continue;
            if (incompatibilities.contains(current.getClass()) || current.getIncompatibilities().contains(module.getClass())) {
                current.disable();
            }
        }
    }
}
