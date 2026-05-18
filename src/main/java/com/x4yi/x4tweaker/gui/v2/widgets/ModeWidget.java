package com.x4yi.x4tweaker.gui.v2.widgets;

import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.setting.ModeSetting;
import net.minecraft.client.Minecraft;

public class ModeWidget implements SettingWidget {
    private static final int HEIGHT = 16;
    private static final int ARROW_W = 12;

    private int x, y, width, height;
    private boolean visible = true;
    private final ModeSetting setting;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private boolean leftHovered = false;
    private boolean rightHovered = false;

    public ModeWidget(ModeSetting setting, ThemeBridge theme) {
        this.setting = setting;
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.height = HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        String modeVal = String.valueOf(setting.getValue());
        int modeW = mc.fontRenderer.getStringWidth(modeVal);
        int centerX = x + width / 2;

        int leftX = centerX - modeW / 2 - ARROW_W - 2;
        int rightX = centerX + modeW / 2 + 2;

        leftHovered = mouseX >= leftX && mouseX <= leftX + ARROW_W && mouseY >= y && mouseY <= y + HEIGHT;
        rightHovered = mouseX >= rightX && mouseX <= rightX + ARROW_W && mouseY >= y && mouseY <= y + HEIGHT;

        DrawHelper.drawRect(x, y, x + width, y + HEIGHT, 0x00000000);
        mc.fontRenderer.drawStringWithShadow(setting.getName() + ":", x, y + 4, 0xFFDDDDDD);

        mc.fontRenderer.drawStringWithShadow("\u25C0", leftX, y + 4, leftHovered ? 0xFFFFFFFF : 0xFF888888);
        mc.fontRenderer.drawStringWithShadow(modeVal, centerX - modeW / 2, y + 4, 0xFFBBBBFF);
        mc.fontRenderer.drawStringWithShadow("\u25B6", rightX, y + 4, rightHovered ? 0xFFFFFFFF : 0xFF888888);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || !contains(mouseX, mouseY)) return false;
        String modeVal = String.valueOf(setting.getValue());
        int modeW = mc.fontRenderer.getStringWidth(modeVal);
        int centerX = x + width / 2;
        int leftX = centerX - modeW / 2 - ARROW_W - 2;
        int rightX = centerX + modeW / 2 + 2;

        if (button == 0) {
            if (mouseX >= leftX && mouseX <= leftX + ARROW_W) {
                setting.cycleBack();
                return true;
            }
            if (mouseX >= rightX && mouseX <= rightX + ARROW_W) {
                setting.cycle();
                return true;
            }
            setting.cycle();
            return true;
        }
        if (button == 1) {
            setting.cycleBack();
            return true;
        }
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
    public ModeSetting getSetting() { return setting; }
    @Override
    public int getRequiredHeight() { return HEIGHT; }
    @Override
    public int getPriority() { return 0; }
    @Override
    public void setPriority(int priority) {}
}
