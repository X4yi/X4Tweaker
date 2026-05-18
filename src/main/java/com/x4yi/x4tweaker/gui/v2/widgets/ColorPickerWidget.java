package com.x4yi.x4tweaker.gui.v2.widgets;

import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.setting.ColorSetting;
import net.minecraft.client.Minecraft;

public class ColorPickerWidget implements SettingWidget {
    private static final int HEIGHT = 16;
    private static final int PREVIEW_SIZE = 12;

    private int x, y, width, height;
    private boolean visible = true;
    private final ColorSetting setting;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private boolean hovered = false;

    public ColorPickerWidget(ColorSetting setting, ThemeBridge theme) {
        this.setting = setting;
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.height = HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        hovered = contains(mouseX, mouseY);

        int color = setting.getValue().getRGB();
        DrawHelper.drawRect(x, y, x + width, y + HEIGHT, 0x00000000);
        mc.fontRenderer.drawStringWithShadow(setting.getName(), x, y + 4, 0xFFDDDDDD);

        int previewX = x + width - PREVIEW_SIZE - 4;
        int previewY = y + (HEIGHT - PREVIEW_SIZE) / 2;
        DrawHelper.drawBorderedRect(previewX, previewY, previewX + PREVIEW_SIZE, previewY + PREVIEW_SIZE, 1.0f, theme.getSeparatorColor(), color);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0 || !contains(mouseX, mouseY)) return false;
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) { return false; }
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
    }

    @Override
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEIGHT;
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
    public ColorSetting getSetting() { return setting; }
    @Override
    public int getRequiredHeight() { return HEIGHT; }
    @Override
    public int getPriority() { return 0; }
    @Override
    public void setPriority(int priority) {}
}
