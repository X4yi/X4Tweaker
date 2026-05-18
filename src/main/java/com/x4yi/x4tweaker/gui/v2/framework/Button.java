package com.x4yi.x4tweaker.gui.v2.framework;

import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import net.minecraft.client.Minecraft;

public class Button implements GuiComponent {
    protected int x, y, width, height;
    protected boolean visible = true;
    protected final String text;
    protected final Runnable onClick;
    protected boolean hovered = false;
    protected boolean pressed = false;
    protected int bgColor;
    protected int hoverBgColor;
    protected int textColor;
    protected int borderColor;
    protected int priority = 0;

    public Button(String text, int x, int y, int width, int height, Runnable onClick) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onClick = onClick;
        this.bgColor = 0x33333333;
        this.hoverBgColor = 0x55333333;
        this.textColor = 0xFFFFFFFF;
        this.borderColor = 0x00000000;
    }

    public Button setColors(int bgColor, int hoverBgColor, int textColor) {
        this.bgColor = bgColor;
        this.hoverBgColor = hoverBgColor;
        this.textColor = textColor;
        return this;
    }

    public Button setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        hovered = contains(mouseX, mouseY);
        int bg = pressed ? hoverBgColor : (hovered ? hoverBgColor : bgColor);
        DrawHelper.drawRect(x, y, x + width, y + height, bg);
        if (borderColor != 0x00000000) {
            DrawHelper.drawBorderedRect(x, y, x + width, y + height, 1.0f, borderColor, 0x00000000);
        }
        Minecraft mc = Minecraft.getMinecraft();
        int tw = mc.fontRenderer.getStringWidth(text);
        mc.fontRenderer.drawStringWithShadow(text, x + (width - tw) / 2, y + (height - 8) / 2, textColor);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0 || !contains(mouseX, mouseY)) return false;
        pressed = true;
        return true;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) {
        if (!visible || button != 0 || !pressed) return false;
        pressed = false;
        if (contains(mouseX, mouseY) && onClick != null) {
            onClick.run();
            return true;
        }
        return false;
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
        this.width = width;
        this.height = height;
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

    public String getText() { return text; }
    public boolean isHovered() { return hovered; }
    @Override
    public int getPriority() { return priority; }
    @Override
    public void setPriority(int priority) { this.priority = priority; }
}
