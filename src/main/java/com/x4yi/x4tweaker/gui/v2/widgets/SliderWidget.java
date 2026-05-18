package com.x4yi.x4tweaker.gui.v2.widgets;

import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.gui.v2.utils.MathHelper;
import com.x4yi.x4tweaker.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

public class SliderWidget implements SettingWidget {
    private static final int HEIGHT = 16;
    private static final int TRACK_H = 4;
    private static final int NUB_R = 5;

    private int x, y, width, height;
    private boolean visible = true;
    private final NumberSetting setting;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private boolean dragging = false;
    private boolean hovered = false;

    public SliderWidget(NumberSetting setting, ThemeBridge theme) {
        this.setting = setting;
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.height = HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        hovered = contains(mouseX, mouseY);

        double val = setting.getValue();
        double min = setting.getMin();
        double max = setting.getMax();
        double ratio = (val - min) / (max - min);

        int trackX = x;
        int trackY = y + HEIGHT / 2 - TRACK_H / 2;
        int trackW = width;

        DrawHelper.drawBorderedRect(trackX, trackY, trackX + trackW, trackY + TRACK_H, 1.0f,
            theme.getSeparatorColor(), theme.getSliderTrackColor());

        int fillW = (int)(trackW * ratio);
        if (fillW > 0) {
            DrawHelper.drawRoundedRect(trackX, trackY, trackX + fillW, trackY + TRACK_H, 2, theme.getAccentColor());
        }

        int nubX = trackX + fillW;
        int nubY = trackY + TRACK_H / 2;
        DrawHelper.drawCircle(nubX, nubY, NUB_R, 0xFFFFFFFF);
        DrawHelper.drawCircle(nubX, nubY, NUB_R - 1, theme.getAccentColor());

        String displayVal = setting.getIncrement() == 1.0 ? String.valueOf((int) val) : String.format("%.2f", val);
        String label = setting.getName() + ": " + displayVal;
        mc.fontRenderer.drawStringWithShadow(label, x + 4, y + 2, 0xFFDDDDDD);

        if (dragging) {
            double clickRatio = MathHelper.clamp((double)(mouseX - trackX) / (double)trackW, 0.0, 1.0);
            double newVal = min + clickRatio * (max - min);
            double stepped = Math.round(newVal / setting.getIncrement()) * setting.getIncrement();
            setting.setValue(MathHelper.clamp(stepped, min, max));
        }
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0 || !contains(mouseX, mouseY)) return false;
        dragging = true;
        return true;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) {
        if (dragging) {
            dragging = false;
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
    public NumberSetting getSetting() { return setting; }
    @Override
    public int getRequiredHeight() { return HEIGHT; }
    @Override
    public int getPriority() { return 0; }
    @Override
    public void setPriority(int priority) {}
}
