package com.x4yi.x4tweaker.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.x4yi.x4tweaker.X4Tweaker;
import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.Setting;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigManager {
    private static final int LEGACY_CONFIG_VERSION = 4;
    private static final String BUILD_VERSION = X4Tweaker.VERSION;

    private final File configDir;
    private final File modulesDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final List<String> migrationReport = new ArrayList<String>();
    private boolean pendingMigrationNotice;

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
            migrationReport.add("Config legacy detectada: modules.json -> modular");
            pendingMigrationNotice = true;
            save();
            return;
        }
        load();
    }

    public boolean hasPendingMigrationNotice() {
        return pendingMigrationNotice && !migrationReport.isEmpty();
    }

    public List<String> consumeMigrationReport() {
        if (!hasPendingMigrationNotice()) return Collections.emptyList();
        List<String> copy = new ArrayList<String>(migrationReport);
        migrationReport.clear();
        pendingMigrationNotice = false;
        return copy;
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
                json.addProperty("buildVersion", BUILD_VERSION);
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

        boolean rewriteRequired = false;
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();

        for (int i = 0, size = modules.size(); i < size; i++) {
            Module module = modules.get(i);
            File moduleFile = new File(modulesDir, module.getName() + ".json");
            if (!moduleFile.exists()) continue;

            try (FileReader reader = new FileReader(moduleFile)) {
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                String fileBuildVersion = json.has("buildVersion") ? json.get("buildVersion").getAsString() : null;
                int legacyVersion = json.has("configVersion") ? json.get("configVersion").getAsInt() : 1;
                int fromIndex = resolveMigrationIndex(fileBuildVersion, legacyVersion);
                int currentIndex = resolveMigrationIndex(BUILD_VERSION, LEGACY_CONFIG_VERSION);
                boolean outdated = fromIndex < currentIndex;

                JsonObject settingsJson = json.has("settings") && json.get("settings").isJsonObject()
                    ? json.getAsJsonObject("settings")
                    : new JsonObject();

                if (outdated) {
                    backupConfig(moduleFile, fileBuildVersion != null ? fileBuildVersion : ("v" + legacyVersion));
                    applyMigrations(module.getName(), fromIndex, settingsJson);
                    migrationReport.add(module.getName() + " migrado " + (fileBuildVersion != null ? fileBuildVersion : ("v" + legacyVersion)) + " -> " + BUILD_VERSION);
                    rewriteRequired = true;
                }

                if (json.has("enabled")) module.setEnabled(json.get("enabled").getAsBoolean());
                if (json.has("keybind")) module.setKeybind(json.get("keybind").getAsInt());

                for (Setting<?> s : module.getSettings()) {
                    s.loadFromJson(settingsJson);
                }
            } catch (Exception e) {
                System.err.println("[X4Tweaker] Error cargando config " + module.getName() + ": " + e.getMessage());
            }
        }

        if (rewriteRequired) {
            pendingMigrationNotice = true;
            save();
        }
    }

    private void backupConfig(File moduleFile, String oldVersion) {
        try {
            String name = moduleFile.getName();
            int dot = name.lastIndexOf('.');
            String base = dot > 0 ? name.substring(0, dot) : name;
            String sanitized = oldVersion == null ? "legacy" : oldVersion.replaceAll("[^a-zA-Z0-9._-]", "_");
            File backup = new File(modulesDir, base + "." + sanitized + ".bak");
            if (!backup.exists()) {
                Files.copy(moduleFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("[X4Tweaker] Error creando backup de config: " + e.getMessage());
        }
    }

    private void applyMigrations(String moduleName, int fromVersion, JsonObject settings) {
        if ("MobESP".equalsIgnoreCase(moduleName)) {
            migrateMobEspSettings(settings);
        }

        if (fromVersion < 3) {
            if (settings.has("Eat Duration") && !settings.has("Eat Until")) {
                settings.add("Eat Until", settings.get("Eat Duration"));
                migrationReport.add("BetterAFK: Eat Duration -> Eat Until");
            }
        }
    }

    private int resolveMigrationIndex(String buildVersion, int legacyVersion) {
        if (buildVersion == null || buildVersion.trim().isEmpty()) {
            return legacyVersion;
        }
        if ("r1.0".equalsIgnoreCase(buildVersion)) {
            return 4;
        }
        if ("r1.0.1".equalsIgnoreCase(buildVersion)) {
            return 5;
        }
        return 5;
    }

    private void migrateMobEspSettings(JsonObject settings) {
        invertSetting(settings, "Hostiles", "Ignore Hostiles");
        invertSetting(settings, "Animals", "Ignore Animals");
        invertSetting(settings, "Villagers", "Ignore Villagers");
        invertSetting(settings, "Slimes", "Ignore Slimes");
        invertSetting(settings, "Flying", "Ignore Flying");
        invertSetting(settings, "Pets", "Ignore Pets");
        invertSetting(settings, "Invisible", "Ignore Invisible");
        invertSetting(settings, "Armor Stands", "Ignore Armor Stands");
        invertSetting(settings, "Players", "Ignore Players");
    }

    private void invertSetting(JsonObject settings, String oldKey, String newKey) {
        if (settings.has(newKey)) return;
        if (!settings.has(oldKey)) return;

        try {
            boolean include = settings.get(oldKey).getAsBoolean();
            settings.addProperty(newKey, !include);
            migrationReport.add("MobESP: " + oldKey + " -> " + newKey);
        } catch (Exception ignored) {}
    }

}
