package com.x4yi.x4tweaker.gui.v2.clickgui;

import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class MigrationBanner implements GuiComponent {
    private static final int MAX_H = 78;
    private static final int LINE_H = 11;
    private static final int MAX_LINES = 5;
    private static final int PAD = 6;

    private int x, y, width, height;
    private boolean visible = true;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private final List<String> notices = new ArrayList<String>();
    private int scroll = 0;
    private int maxScroll = 0;

    public MigrationBanner(ThemeBridge theme) {
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
    }

    public void setNotices(List<String> notices) {
        this.notices.clear();
        if (notices != null) this.notices.addAll(notices);
        this.maxScroll = Math.max(0, this.notices.size() - MAX_LINES);
        this.scroll = 0;
        this.visible = !this.notices.isEmpty();
    }

    public boolean hasNotices() {
        return visible && !notices.isEmpty();
    }

    public void handleWheel(int dWheel) {
        if (!visible) return;
        if (dWheel < 0) scroll = Math.min(maxScroll, scroll + 1);
        else scroll = Math.max(0, scroll - 1);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible || notices.isEmpty()) return;
        DrawHelper.drawBorderedRect(x, y, x + width, y + height, 1.0f, theme.getSeparatorColor(), theme.getContentBgColor());
        mc.fontRenderer.drawStringWithShadow("Config actualizada - revisa ajustes", x + PAD, y + PAD, 0xFFFFFFFF);

        int closeX = x + width - 16;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= y + 4 && mouseY <= y + 16;
        mc.fontRenderer.drawStringWithShadow("x", closeX, y + 6, closeHover ? 0xFFFF6666 : 0xFFBBBBBB);

        int baseY = y + PAD + 14;
        for (int i = 0; i < MAX_LINES; i++) {
            int idx = i + scroll;
            if (idx >= notices.size()) break;
            String msg = notices.get(idx);
            if (msg.length() > 46) msg = msg.substring(0, 43) + "...";
            mc.fontRenderer.drawStringWithShadow("- " + msg, x + PAD + 2, baseY + i * LINE_H, 0xFFD6D6D6);
        }

        if (maxScroll > 0) {
            String footer = (scroll + 1) + "/" + (maxScroll + 1);
            mc.fontRenderer.drawStringWithShadow(footer, x + width - mc.fontRenderer.getStringWidth(footer) - PAD, y + height - 10, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0) return false;
        int closeX = x + width - 16;
        if (mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= y + 4 && mouseY <= y + 16) {
            visible = false;
            return true;
        }
        if (contains(mouseX, mouseY)) return true;
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
    @Override
    public int getPriority() { return 50; }
    @Override
    public void setPriority(int priority) {}
}
