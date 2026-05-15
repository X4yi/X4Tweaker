package com.x4yi.x4tweaker.manager;

import com.google.gson.*;
import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.Setting;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.List;

public class ConfigManager {
    private static final int CONFIG_VERSION = 2;

    private final File configDir;
    private final File modulesDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        configDir = new File(Minecraft.getMinecraft().mcDataDir, "x4tweaker");
        modulesDir = new File(configDir, "modules");
        modulesDir.mkdirs();
    }

    public void init() {

        File legacyFile = new File(configDir, "modules.json");
        if (legacyFile.exists()) {
            System.out.println("[X4Tweaker] Encontrado modules.json antiguo, migrando a configuración modular...");
            loadLegacy(legacyFile);
            legacyFile.renameTo(new File(configDir, "modules.json.old"));
            save();
        } else {
            load();
        }
    }

    private void loadLegacy(File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            JsonObject modulesJson = json.has("modules") && json.get("modules").isJsonObject()
                ? json.getAsJsonObject("modules")
                : json;

            List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
            for (int i = 0, size = modules.size(); i < size; i++) {
                Module module = modules.get(i);
                if (!modulesJson.has(module.getName())) continue;
                JsonObject moduleJson = modulesJson.getAsJsonObject(module.getName());

                if (moduleJson.has("enabled")) module.setEnabled(moduleJson.get("enabled").getAsBoolean());
                if (moduleJson.has("keybind")) module.setKeybind(moduleJson.get("keybind").getAsInt());
                if (moduleJson.has("settings")) {
                    JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                    for (Setting<?> s : module.getSettings()) s.loadFromJson(settingsJson);
                }
            }
        } catch (Exception e) {
            System.err.println("[X4Tweaker] Error migrando config antigua: " + e.getMessage());
        }
    }

    public void save() {
        modulesDir.mkdirs();
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module module = modules.get(i);
            File moduleFile = new File(modulesDir, module.getName() + ".json");
            File tmpFile = new File(modulesDir, module.getName() + ".json.tmp");

            try {
                JsonObject json = new JsonObject();
                json.addProperty("configVersion", CONFIG_VERSION);
                json.addProperty("enabled", module.isEnabled());
                json.addProperty("keybind", module.getKeybind());

                JsonObject settingsJson = new JsonObject();
                for (Setting<?> s : module.getSettings()) {
                    s.saveToJson(settingsJson);
                }
                json.add("settings", settingsJson);

                try (FileWriter writer = new FileWriter(tmpFile)) {
                    gson.toJson(json, writer);
                }

                if (moduleFile.exists() && !moduleFile.delete()) {
                    throw new IOException("No se pudo reemplazar config: " + module.getName());
                }
                if (!tmpFile.renameTo(moduleFile)) {
                    throw new IOException("No se pudo mover config temporal: " + module.getName());
                }
            } catch (Exception e) {
                System.err.println("[X4Tweaker] Error guardando config " + module.getName() + ": " + e.getMessage());
                tmpFile.delete();
            }
        }
    }

    public void load() {
        if (!modulesDir.exists()) return;
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module module = modules.get(i);
            File moduleFile = new File(modulesDir, module.getName() + ".json");
            if (!moduleFile.exists()) continue;

            try (FileReader reader = new FileReader(moduleFile)) {
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                if (json.has("enabled")) module.setEnabled(json.get("enabled").getAsBoolean());
                if (json.has("keybind")) module.setKeybind(json.get("keybind").getAsInt());
                if (json.has("settings")) {
                    JsonObject settingsJson = json.getAsJsonObject("settings");
                    for (Setting<?> s : module.getSettings()) {
                        s.loadFromJson(settingsJson);
                    }
                }
            } catch (Exception e) {
                System.err.println("[X4Tweaker] Error cargando config " + module.getName() + ": " + e.getMessage());
            }
        }
    }
}
