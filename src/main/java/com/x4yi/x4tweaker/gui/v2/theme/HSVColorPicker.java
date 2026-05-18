package com.x4yi.x4tweaker.gui.v2.theme;

import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.gui.v2.utils.MathHelper;
import net.minecraft.client.Minecraft;

public class HSVColorPicker implements GuiComponent {
    private static final int SV_W = 160;
    private static final int SV_H = 100;
    private static final int BAR_W = 16;
    private static final int BAR_H = 100;
    private static final int GAP = 8;

    private int x, y, width, height;
    private boolean visible = true;
    private final Minecraft mc;
    private float hue = 0.0f;
    private float sat = 1.0f;
    private float val = 1.0f;
    private int alpha = 255;
    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private Runnable onColorChange;

    public HSVColorPicker(Runnable onColorChange) {
        this.mc = Minecraft.getMinecraft();
        this.onColorChange = onColorChange;
        this.width = SV_W + GAP + BAR_W + GAP + BAR_W;
        this.height = SV_H;
    }

    public void setColor(int rgb) {
        float[] hsb = java.awt.Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);
        this.hue = hsb[0];
        this.sat = hsb[1];
        this.val = hsb[2];
        this.alpha = (rgb >> 24) & 0xFF;
    }

    public int getCurrentColor() {
        int rgb = java.awt.Color.HSBtoRGB(hue, sat, val);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }

    public float getHue() { return hue; }
    public float getSat() { return sat; }
    public float getVal() { return val; }
    public int getAlpha() { return alpha; }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        int currentRgb = java.awt.Color.HSBtoRGB(hue, sat, val);

        DrawHelper.drawSatValSquare(x, y, SV_W, SV_H, hue);
        int px = x + (int)(sat * SV_W);
        int py = y + (int)((1.0f - val) * SV_H);
        DrawHelper.drawBorderedRect(px - 2, py - 2, px + 2, py + 2, 1.0f, 0xFF000000, 0xFFFFFFFF);

        int hueX = x + SV_W + GAP;
        DrawHelper.drawHueBar(hueX, y, BAR_W, SV_H);
        int hx = hueX + (int)(hue * BAR_W);
        DrawHelper.drawBorderedRect(hx - 2, y - 1, hx + 2, y + SV_H + 1, 1.0f, 0xFF000000, 0xFFFFFFFF);

        int alphaX = hueX + BAR_W + GAP;
        DrawHelper.drawAlphaBar(alphaX, y, BAR_W, SV_H, currentRgb);
        int ax = alphaX + (int)((alpha / 255f) * BAR_W);
        DrawHelper.drawBorderedRect(ax - 2, y - 1, ax + 2, y + SV_H + 1, 1.0f, 0xFF000000, 0xFFFFFFFF);

        int previewX = x;
        int previewY = y + SV_H + 10;
        int previewW = SV_W + GAP + BAR_W + GAP + BAR_W;
        int previewH = 20;
        DrawHelper.drawBorderedRect(previewX, previewY, previewX + previewW, previewY + previewH, 1.0f, 0xFF555555, getCurrentColor());

        java.awt.Color c = new java.awt.Color(getCurrentColor(), true);
        String rgbaStr = "RGBA: " + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ", " + c.getAlpha();
        mc.fontRenderer.drawStringWithShadow(rgbaStr, previewX, previewY + previewH + 6, 0xFFDDDDDD);

        if (draggingSV) {
            sat = MathHelper.clamp((float)(mouseX - x) / (float)SV_W, 0.0f, 1.0f);
            val = 1.0f - MathHelper.clamp((float)(mouseY - y) / (float)SV_H, 0.0f, 1.0f);
            if (onColorChange != null) onColorChange.run();
        }
        if (draggingHue) {
            hue = MathHelper.clamp((float)(mouseX - hueX) / (float)BAR_W, 0.0f, 1.0f);
            if (onColorChange != null) onColorChange.run();
        }
        if (draggingAlpha) {
            alpha = (int)(MathHelper.clamp((float)(mouseX - alphaX) / (float)BAR_W, 0.0f, 1.0f) * 255);
            if (onColorChange != null) onColorChange.run();
        }
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0) return false;
        if (mouseX >= x && mouseX <= x + SV_W && mouseY >= y && mouseY <= y + SV_H) {
            draggingSV = true;
            return true;
        }
        int hueX = x + SV_W + GAP;
        if (mouseX >= hueX && mouseX <= hueX + BAR_W && mouseY >= y && mouseY <= y + SV_H) {
            draggingHue = true;
            return true;
        }
        int alphaX = hueX + BAR_W + GAP;
        if (mouseX >= alphaX && mouseX <= alphaX + BAR_W && mouseY >= y && mouseY <= y + SV_H) {
            draggingAlpha = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) {
        boolean was = draggingSV || draggingHue || draggingAlpha;
        draggingSV = false;
        draggingHue = false;
        draggingAlpha = false;
        return was;
    }

    @Override
    public boolean onMouseMove(int mouseX, int mouseY, int dx, int dy) { return false; }
    @Override
    public boolean onKey(char typedChar, int keyCode) { return false; }
    @Override
    public void update() {}

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getWidth() { return width; }
    @Override
    public int getHeight() { return height; }
    @Override
    public boolean isVisible() { return visible; }
    @Override
    public void setVisible(boolean visible) { this.visible = visible; }
    @Override
    public int getPriority() { return 100; }
    @Override
    public void setPriority(int priority) {}
}
