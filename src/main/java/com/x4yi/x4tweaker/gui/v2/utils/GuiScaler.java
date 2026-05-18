package com.x4yi.x4tweaker.gui.v2.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public final class GuiScaler {
    private GuiScaler() {}

    public static int getGuiScale() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
    }

    public static int getScreenWidth() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
    }

    public static int getScreenHeight() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
    }

    public static int clampWidth(int desired, int min, int max) {
        int screenW = getScreenWidth();
        int clamped = Math.max(min, Math.min(max, desired));
        return Math.min(clamped, screenW - 40);
    }

    public static int clampHeight(int desired, int min, int max) {
        int screenH = getScreenHeight();
        int clamped = Math.max(min, Math.min(max, desired));
        return Math.min(clamped, screenH - 40);
    }

    public static int scaleByPercent(float percent) {
        return (int)(getScreenWidth() * percent);
    }

    public static int scaleHeightByPercent(float percent) {
        return (int)(getScreenHeight() * percent);
    }

    public static boolean fitsInScreen(int x, int y, int w, int h) {
        int screenW = getScreenWidth();
        int screenH = getScreenHeight();
        return x >= 0 && y >= 0 && x + w <= screenW && y + h <= screenH;
    }

    public static int clampToScreenX(int x, int w) {
        int screenW = getScreenWidth();
        if (x + w > screenW) return screenW - w;
        return Math.max(0, x);
    }

    public static int clampToScreenY(int y, int h) {
        int screenH = getScreenHeight();
        if (y + h > screenH) return screenH - h;
        return Math.max(0, y);
    }
}
