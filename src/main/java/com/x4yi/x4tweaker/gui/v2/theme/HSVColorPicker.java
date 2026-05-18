package com.x4yi.x4tweaker.gui.v2.theme;

import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.gui.v2.utils.MathHelper;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class HSVColorPicker implements GuiComponent {
    private static final int SAT_VAL_SIZE = 120;
    private static final int HUE_WHEEL_RADIUS = 36;
    private static final int ALPHA_BAR_W = 14;
    private static final int GAP = 8;

    private int x, y, width, height;
    private boolean visible = true;
    private int priority = 100;
    private final Minecraft mc;
    private float hue = 0.0f;
    private float sat = 1.0f;
    private float val = 1.0f;
    private int alpha = 255;
    private boolean draggingSatVal = false;
    private boolean draggingHueWheel = false;
    private boolean draggingAlphaBar = false;
    private Runnable onColorChanged;

    private int satValX, satValY, hueWheelCx, hueWheelCy, alphaBarX, alphaBarY;
    private int previewX, previewY, previewW, previewH;

    public HSVColorPicker(Runnable onColorChanged) {
        this.mc = Minecraft.getMinecraft();
        this.onColorChanged = onColorChanged;
        this.width = SAT_VAL_SIZE + GAP + HUE_WHEEL_RADIUS * 2 + GAP + ALPHA_BAR_W;
        this.height = Math.max(SAT_VAL_SIZE, HUE_WHEEL_RADIUS * 2);
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

        satValX = x;
        satValY = y;
        hueWheelCx = x + SAT_VAL_SIZE + GAP + HUE_WHEEL_RADIUS;
        hueWheelCy = y + HUE_WHEEL_RADIUS;
        alphaBarX = x + SAT_VAL_SIZE + GAP + HUE_WHEEL_RADIUS * 2 + GAP;
        alphaBarY = y + (SAT_VAL_SIZE - ALPHA_BAR_W) / 2;

        DrawHelper.drawSatValSquare(satValX, satValY, SAT_VAL_SIZE, SAT_VAL_SIZE, hue);
        int svPx = satValX + (int)(sat * SAT_VAL_SIZE);
        int svPy = satValY + (int)((1.0f - val) * SAT_VAL_SIZE);
        DrawHelper.drawBorderedRect(svPx - 2, svPy - 2, svPx + 2, svPy + 2, 1.0f, 0xFF000000, 0xFFFFFFFF);

        renderHueWheel(hueWheelCx, hueWheelCy, HUE_WHEEL_RADIUS);
        float hueAngle = hue * 360.0f;
        double hueRad = Math.toRadians(hueAngle - 90);
        int hueCursorX = hueWheelCx + (int)(Math.cos(hueRad) * HUE_WHEEL_RADIUS);
        int hueCursorY = hueWheelCy + (int)(Math.sin(hueRad) * HUE_WHEEL_RADIUS);
        DrawHelper.drawBorderedRect(hueCursorX - 3, hueCursorY - 3, hueCursorX + 3, hueCursorY + 3, 1.0f, 0xFF000000, 0xFFFFFFFF);

        DrawHelper.drawAlphaBar(alphaBarX, alphaBarY, ALPHA_BAR_W, SAT_VAL_SIZE, currentRgb);
        int ax = alphaBarX + (int)((alpha / 255f) * ALPHA_BAR_W);
        DrawHelper.drawBorderedRect(ax - 2, alphaBarY - 1, ax + 2, alphaBarY + SAT_VAL_SIZE + 1, 1.0f, 0xFF000000, 0xFFFFFFFF);

        previewX = x;
        previewY = y + SAT_VAL_SIZE + 6;
        previewW = width;
        previewH = 16;
        DrawHelper.drawBorderedRect(previewX, previewY, previewX + previewW, previewY + previewH, 1.0f, 0xFF555555, getCurrentColor());

        java.awt.Color c = new java.awt.Color(getCurrentColor(), true);
        String rgbaStr = "RGBA: " + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ", " + c.getAlpha();
        mc.fontRenderer.drawStringWithShadow(rgbaStr, previewX, previewY + previewH + 4, 0xFFDDDDDD);

        if (draggingSatVal) {
            sat = MathHelper.clamp((float)(mouseX - satValX) / (float)SAT_VAL_SIZE, 0.0f, 1.0f);
            val = 1.0f - MathHelper.clamp((float)(mouseY - satValY) / (float)SAT_VAL_SIZE, 0.0f, 1.0f);
            if (onColorChanged != null) onColorChanged.run();
        }
        if (draggingHueWheel) {
            double angle = Math.atan2(mouseY - hueWheelCy, mouseX - hueWheelCx);
            float normalizedAngle = (float)((angle + Math.PI / 2.0) / (2.0 * Math.PI));
            if (normalizedAngle < 0) normalizedAngle += 1.0f;
            hue = MathHelper.clamp(normalizedAngle, 0.0f, 1.0f);
            if (onColorChanged != null) onColorChanged.run();
        }
        if (draggingAlphaBar) {
            alpha = (int)(MathHelper.clamp((float)(mouseY - alphaBarY) / (float)SAT_VAL_SIZE, 0.0f, 1.0f) * 255);
            if (onColorChanged != null) onColorChanged.run();
        }
    }

    private void renderHueWheel(int cx, int cy, int radius) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(6, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
        int segments = 72;
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians((i * 360.0 / segments) - 90);
            float hueVal = i / (float) segments;
            int color = java.awt.Color.HSBtoRGB(hueVal, 1.0f, 1.0f);
            float r = (float)((color >> 16) & 0xFF) / 255.0f;
            float g = (float)((color >> 8) & 0xFF) / 255.0f;
            float b = (float)(color & 0xFF) / 255.0f;
            if (i == 0) {
                buffer.pos(cx, cy, 0.0).color(r, g, b, 1.0f).endVertex();
            }
            buffer.pos(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, 0.0).color(r, g, b, 1.0f).endVertex();
        }
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0) return false;
        double distToWheel = Math.sqrt((mouseX - hueWheelCx) * (mouseX - hueWheelCx) + (mouseY - hueWheelCy) * (mouseY - hueWheelCy));
        if (distToWheel <= HUE_WHEEL_RADIUS + 4 && distToWheel >= HUE_WHEEL_RADIUS - 8) {
            draggingHueWheel = true;
            return true;
        }
        if (mouseX >= satValX && mouseX <= satValX + SAT_VAL_SIZE && mouseY >= satValY && mouseY <= satValY + SAT_VAL_SIZE) {
            draggingSatVal = true;
            return true;
        }
        if (mouseX >= alphaBarX && mouseX <= alphaBarX + ALPHA_BAR_W && mouseY >= alphaBarY && mouseY <= alphaBarY + SAT_VAL_SIZE) {
            draggingAlphaBar = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) {
        boolean was = draggingSatVal || draggingHueWheel || draggingAlphaBar;
        draggingSatVal = false;
        draggingHueWheel = false;
        draggingAlphaBar = false;
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
    public int getPriority() { return priority; }
    @Override
    public void setPriority(int priority) { this.priority = priority; }
}
