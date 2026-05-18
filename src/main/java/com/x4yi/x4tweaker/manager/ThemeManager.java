package com.x4yi.x4tweaker.manager;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.awt.Color;
import java.io.*;

public class ThemeManager {
    private static final String THEME_FILE = "theme.json";

    private Color colorBotonNormal        = new Color(75, 0, 130, 200);
    private Color colorBotonOscuro        = new Color(48, 0, 84, 200);
    private Color colorToggleEncendido    = new Color(0, 180, 80);
    private Color colorToggleEncendidoOscuro = new Color(0, 120, 50);
    private Color colorToggleApagado      = new Color(150, 150, 150);
    private Color colorToggleApagadoOscuro = new Color(100, 100, 100);
    private Color colorFondo              = new Color(15, 15, 15, 230);
    private Color colorFondoBorde         = new Color(30, 30, 30, 255);

    private Color colorModuloNormal       = new Color(30, 30, 30, 180);
    private Color colorModuloHover        = new Color(75, 0, 130, 100);
    private Color colorSettingsPanel      = new Color(20, 20, 20, 200);
    private Color colorSliderTrack        = new Color(40, 40, 40, 200);
    private Color colorSidebarBg          = new Color(12, 12, 12, 240);
    private Color colorContentBg          = new Color(18, 18, 18, 220);
    private Color colorToggleSwitchBg     = new Color(60, 60, 60, 200);
    private Color colorInputField         = new Color(35, 35, 35, 180);
    private Color colorSeparator          = new Color(60, 60, 60, 150);

    private boolean enablePause = true;

    private static final String[] COLOR_NAMES = {
        "Primary Button", "Primary Button Shade",
        "Toggle Enabled", "Toggle Enabled Shade",
        "Toggle Disabled", "Toggle Disabled Shade",
        "Panel Background", "Panel Border",
        "Module Button", "Module Button Hover",
        "Settings Panel", "Slider Track",
        "Sidebar Background", "Content Background",
        "Toggle Switch BG", "Input Field", "Separator"
    };

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File themeFile;

    public ThemeManager() {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "x4tweaker");
        themeFile = new File(configDir, THEME_FILE);
    }

    public void load() {
        if (!themeFile.exists()) return;
        try (FileReader reader = new FileReader(themeFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            colorBotonNormal          = readColor(json, "colorBotonNormal",          colorBotonNormal);
            colorBotonOscuro          = readColor(json, "colorBotonOscuro",          colorBotonOscuro);
            colorToggleEncendido      = readColor(json, "colorToggleEncendido",      colorToggleEncendido);
            colorToggleEncendidoOscuro= readColor(json, "colorToggleEncendidoOscuro",colorToggleEncendidoOscuro);
            colorToggleApagado        = readColor(json, "colorToggleApagado",        colorToggleApagado);
            colorToggleApagadoOscuro  = readColor(json, "colorToggleApagadoOscuro", colorToggleApagadoOscuro);
            colorFondo                = readColor(json, "colorFondo",                colorFondo);
            colorFondoBorde           = readColor(json, "colorFondoBorde",           colorFondoBorde);
            colorModuloNormal         = readColor(json, "colorModuloNormal",         colorModuloNormal);
            colorModuloHover          = readColor(json, "colorModuloHover",          colorModuloHover);
            colorSettingsPanel        = readColor(json, "colorSettingsPanel",        colorSettingsPanel);
            colorSliderTrack          = readColor(json, "colorSliderTrack",          colorSliderTrack);
            colorSidebarBg            = readColor(json, "colorSidebarBg",            colorSidebarBg);
            colorContentBg            = readColor(json, "colorContentBg",            colorContentBg);
            colorToggleSwitchBg       = readColor(json, "colorToggleSwitchBg",       colorToggleSwitchBg);
            colorInputField           = readColor(json, "colorInputField",           colorInputField);
            colorSeparator            = readColor(json, "colorSeparator",            colorSeparator);
            if (json.has("enablePause")) enablePause = json.get("enablePause").getAsBoolean();
        } catch (IOException | JsonSyntaxException e) {
            System.err.println("[X4Tweaker] Error cargando tema: " + e.getMessage());
        }
    }

    public void save() {
        try {
            themeFile.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            writeColor(json, "colorBotonNormal",           colorBotonNormal);
            writeColor(json, "colorBotonOscuro",           colorBotonOscuro);
            writeColor(json, "colorToggleEncendido",       colorToggleEncendido);
            writeColor(json, "colorToggleEncendidoOscuro", colorToggleEncendidoOscuro);
            writeColor(json, "colorToggleApagado",         colorToggleApagado);
            writeColor(json, "colorToggleApagadoOscuro",   colorToggleApagadoOscuro);
            writeColor(json, "colorFondo",                 colorFondo);
            writeColor(json, "colorFondoBorde",            colorFondoBorde);
            writeColor(json, "colorModuloNormal",          colorModuloNormal);
            writeColor(json, "colorModuloHover",           colorModuloHover);
            writeColor(json, "colorSettingsPanel",         colorSettingsPanel);
            writeColor(json, "colorSliderTrack",           colorSliderTrack);
            writeColor(json, "colorSidebarBg",             colorSidebarBg);
            writeColor(json, "colorContentBg",             colorContentBg);
            writeColor(json, "colorToggleSwitchBg",        colorToggleSwitchBg);
            writeColor(json, "colorInputField",            colorInputField);
            writeColor(json, "colorSeparator",             colorSeparator);
            json.addProperty("enablePause", enablePause);
            try (FileWriter writer = new FileWriter(themeFile)) {
                gson.toJson(json, writer);
            }
        } catch (IOException e) {
            System.err.println("[X4Tweaker] Error guardando tema: " + e.getMessage());
        }
    }

    private Color readColor(JsonObject json, String key, Color fallback) {
        if (!json.has(key)) return fallback;
        return new Color(json.get(key).getAsInt(), true);
    }

    private void writeColor(JsonObject json, String key, Color color) {
        json.addProperty(key, color.getRGB());
    }

    public Color getColorBotonNormal()             { return colorBotonNormal; }
    public Color getColorBotonOscuro()             { return colorBotonOscuro; }
    public Color getColorToggleEncendido()         { return colorToggleEncendido; }
    public Color getColorToggleEncendidoOscuro()   { return colorToggleEncendidoOscuro; }
    public Color getColorToggleApagado()           { return colorToggleApagado; }
    public Color getColorToggleApagadoOscuro()     { return colorToggleApagadoOscuro; }
    public Color getColorFondo()                   { return colorFondo; }
    public Color getColorFondoBorde()              { return colorFondoBorde; }
    public Color getColorModuloNormal()            { return colorModuloNormal; }
    public Color getColorModuloHover()             { return colorModuloHover; }
    public Color getColorSettingsPanel()           { return colorSettingsPanel; }
    public Color getColorSliderTrack()             { return colorSliderTrack; }
    public Color getColorSidebarBg()               { return colorSidebarBg; }
    public Color getColorContentBg()               { return colorContentBg; }
    public Color getColorToggleSwitchBg()          { return colorToggleSwitchBg; }
    public Color getColorInputField()              { return colorInputField; }
    public Color getColorSeparator()               { return colorSeparator; }

    public void setColorBotonNormal(Color c)              { colorBotonNormal = c; }
    public void setColorBotonOscuro(Color c)              { colorBotonOscuro = c; }
    public void setColorToggleEncendido(Color c)          { colorToggleEncendido = c; }
    public void setColorToggleEncendidoOscuro(Color c)    { colorToggleEncendidoOscuro = c; }
    public void setColorToggleApagado(Color c)            { colorToggleApagado = c; }
    public void setColorToggleApagadoOscuro(Color c)      { colorToggleApagadoOscuro = c; }
    public void setColorFondo(Color c)                    { colorFondo = c; }
    public void setColorFondoBorde(Color c)               { colorFondoBorde = c; }
    public void setColorModuloNormal(Color c)             { colorModuloNormal = c; }
    public void setColorModuloHover(Color c)              { colorModuloHover = c; }
    public void setColorSettingsPanel(Color c)            { colorSettingsPanel = c; }
    public void setColorSliderTrack(Color c)              { colorSliderTrack = c; }
    public void setColorSidebarBg(Color c)                { colorSidebarBg = c; }
    public void setColorContentBg(Color c)                { colorContentBg = c; }
    public void setColorToggleSwitchBg(Color c)           { colorToggleSwitchBg = c; }
    public void setColorInputField(Color c)               { colorInputField = c; }
    public void setColorSeparator(Color c)                { colorSeparator = c; }

    public boolean isEnablePause() { return enablePause; }
    public void setEnablePause(boolean p) { enablePause = p; }

    public int getColorCount() { return COLOR_NAMES.length; }
    public String getColorName(int index) { return COLOR_NAMES[index]; }

    public Color getColorByIndex(int index) {
        switch (index) {
            case 0: return colorBotonNormal;
            case 1: return colorBotonOscuro;
            case 2: return colorToggleEncendido;
            case 3: return colorToggleEncendidoOscuro;
            case 4: return colorToggleApagado;
            case 5: return colorToggleApagadoOscuro;
            case 6: return colorFondo;
            case 7: return colorFondoBorde;
            case 8: return colorModuloNormal;
            case 9: return colorModuloHover;
            case 10: return colorSettingsPanel;
            case 11: return colorSliderTrack;
            case 12: return colorSidebarBg;
            case 13: return colorContentBg;
            case 14: return colorToggleSwitchBg;
            case 15: return colorInputField;
            case 16: return colorSeparator;
            default: return Color.WHITE;
        }
    }

    public void setColorByIndex(int index, Color c) {
        switch (index) {
            case 0: colorBotonNormal = c; break;
            case 1: colorBotonOscuro = c; break;
            case 2: colorToggleEncendido = c; break;
            case 3: colorToggleEncendidoOscuro = c; break;
            case 4: colorToggleApagado = c; break;
            case 5: colorToggleApagadoOscuro = c; break;
            case 6: colorFondo = c; break;
            case 7: colorFondoBorde = c; break;
            case 8: colorModuloNormal = c; break;
            case 9: colorModuloHover = c; break;
            case 10: colorSettingsPanel = c; break;
            case 11: colorSliderTrack = c; break;
            case 12: colorSidebarBg = c; break;
            case 13: colorContentBg = c; break;
            case 14: colorToggleSwitchBg = c; break;
            case 15: colorInputField = c; break;
            case 16: colorSeparator = c; break;
        }
    }

    public Color getDefaultColorByIndex(int index) {
        switch (index) {
            case 0: return new Color(75, 0, 130, 200);
            case 1: return new Color(48, 0, 84, 200);
            case 2: return new Color(0, 180, 80);
            case 3: return new Color(0, 120, 50);
            case 4: return new Color(150, 150, 150);
            case 5: return new Color(100, 100, 100);
            case 6: return new Color(15, 15, 15, 230);
            case 7: return new Color(30, 30, 30, 255);
            case 8: return new Color(30, 30, 30, 180);
            case 9: return new Color(75, 0, 130, 100);
            case 10: return new Color(20, 20, 20, 200);
            case 11: return new Color(40, 40, 40, 200);
            case 12: return new Color(12, 12, 12, 240);
            case 13: return new Color(18, 18, 18, 220);
            case 14: return new Color(60, 60, 60, 200);
            case 15: return new Color(35, 35, 35, 180);
            case 16: return new Color(60, 60, 60, 150);
            default: return Color.WHITE;
        }
    }

    public void loadDefaultTheme() {
        colorBotonNormal        = new Color(75, 0, 130, 200);
        colorBotonOscuro        = new Color(48, 0, 84, 200);
        colorToggleEncendido    = new Color(0, 180, 80);
        colorToggleEncendidoOscuro = new Color(0, 120, 50);
        colorToggleApagado      = new Color(150, 150, 150);
        colorToggleApagadoOscuro = new Color(100, 100, 100);
        colorFondo              = new Color(15, 15, 15, 230);
        colorFondoBorde         = new Color(30, 30, 30, 255);
        colorModuloNormal       = new Color(30, 30, 30, 180);
        colorModuloHover        = new Color(75, 0, 130, 100);
        colorSettingsPanel      = new Color(20, 20, 20, 200);
        colorSliderTrack        = new Color(40, 40, 40, 200);
        colorSidebarBg          = new Color(12, 12, 12, 240);
        colorContentBg          = new Color(18, 18, 18, 220);
        colorToggleSwitchBg     = new Color(60, 60, 60, 200);
        colorInputField         = new Color(35, 35, 35, 180);
        colorSeparator          = new Color(60, 60, 60, 150);
    }
}
