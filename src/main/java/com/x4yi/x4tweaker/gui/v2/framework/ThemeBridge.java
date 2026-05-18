package com.x4yi.x4tweaker.gui.v2.framework;

import com.x4yi.x4tweaker.manager.ThemeManager;

import java.awt.Color;

public class ThemeBridge {
    private final ThemeManager theme;

    public ThemeBridge(ThemeManager theme) {
        this.theme = theme;
    }

    public int getAccentColor() {
        return theme.getColorBotonNormal().getRGB();
    }

    public int getAccentDarkColor() {
        return theme.getColorBotonOscuro().getRGB();
    }

    public int getEnabledColor() {
        return theme.getColorToggleEncendido().getRGB();
    }

    public int getEnabledDarkColor() {
        return theme.getColorToggleEncendidoOscuro().getRGB();
    }

    public int getDisabledColor() {
        return theme.getColorToggleApagado().getRGB();
    }

    public int getDisabledDarkColor() {
        return theme.getColorToggleApagadoOscuro().getRGB();
    }

    public int getBgColor() {
        return theme.getColorFondo().getRGB();
    }

    public int getBorderColor() {
        return theme.getColorFondoBorde().getRGB();
    }

    public int getSurfaceColor() {
        return theme.getColorModuloNormal().getRGB();
    }

    public int getSurfaceHoverColor() {
        return theme.getColorModuloHover().getRGB();
    }

    public int getSettingsPanelColor() {
        return theme.getColorSettingsPanel().getRGB();
    }

    public int getSliderTrackColor() {
        return theme.getColorSliderTrack().getRGB();
    }

    public int getSidebarBgColor() {
        return theme.getColorSidebarBg().getRGB();
    }

    public int getContentBgColor() {
        return theme.getColorContentBg().getRGB();
    }

    public int getToggleSwitchBgColor() {
        return theme.getColorToggleSwitchBg().getRGB();
    }

    public int getInputFieldColor() {
        return theme.getColorInputField().getRGB();
    }

    public int getSeparatorColor() {
        return theme.getColorSeparator().getRGB();
    }

    public Color getColorByIndex(int index) {
        return theme.getColorByIndex(index);
    }

    public void setColorByIndex(int index, Color color) {
        theme.setColorByIndex(index, color);
    }

    public Color getDefaultColorByIndex(int index) {
        return theme.getDefaultColorByIndex(index);
    }

    public int getColorCount() {
        return theme.getColorCount();
    }

    public String getColorName(int index) {
        return theme.getColorName(index);
    }

    public void save() {
        theme.save();
    }

    public void loadDefaultTheme() {
        theme.loadDefaultTheme();
    }

    public boolean isEnablePause() {
        return theme.isEnablePause();
    }

    public void setEnablePause(boolean value) {
        theme.setEnablePause(value);
    }
}
