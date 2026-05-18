package com.x4yi.x4tweaker.gui.v2.widgets;

import com.x4yi.x4tweaker.gui.v2.framework.AnimationHelper;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import net.minecraft.client.Minecraft;

public class ToggleWidget implements SettingWidget {
    private static final int HEIGHT = 16;
    private static final int SWITCH_W = 28;
    private static final int SWITCH_H = 12;
    private static final int HANDLE_R = 4;

    private int x, y, width, height;
    private boolean visible = true;
    private final BooleanSetting setting;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private float handleAnim = 0;
    private boolean hovered = false;

    public ToggleWidget(BooleanSetting setting, ThemeBridge theme) {
        this.setting = setting;
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.height = HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        float target = setting.getValue() ? 1.0f : 0.0f;
        handleAnim = AnimationHelper.lerp(handleAnim, target, 0.2f);
        hovered = contains(mouseX, mouseY);

        DrawHelper.drawRect(x, y, x + width, y + HEIGHT, 0x00000000);
        mc.fontRenderer.drawStringWithShadow(setting.getName(), x, y + 4, 0xFFDDDDDD);

        int swX = x + width - SWITCH_W;
        int swY = y + (HEIGHT - SWITCH_H) / 2;
        boolean on = setting.getValue();
        int bgColor = on ? theme.getEnabledColor() : theme.getToggleSwitchBgColor();
        DrawHelper.drawBorderedRect(swX, swY, swX + SWITCH_W, swY + SWITCH_H, 1.0f, theme.getSeparatorColor(), bgColor);

        int handleCenterX = swX + HANDLE_R + (int)(handleAnim * (SWITCH_W - HANDLE_R * 2));
        DrawHelper.drawCircle(handleCenterX, swY + SWITCH_H / 2, HANDLE_R, 0xFFFFFFFF);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0 || !contains(mouseX, mouseY)) return false;
        setting.toggle();
        return true;
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
    public BooleanSetting getSetting() { return setting; }
    @Override
    public int getRequiredHeight() { return HEIGHT; }
    @Override
    public int getPriority() { return 0; }
    @Override
    public void setPriority(int priority) {}
}
