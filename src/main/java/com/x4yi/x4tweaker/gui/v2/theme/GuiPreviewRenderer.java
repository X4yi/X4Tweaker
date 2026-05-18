package com.x4yi.x4tweaker.gui.v2.theme;

import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import net.minecraft.client.Minecraft;

public class GuiPreviewRenderer {
    private static final int HEADER_H = 14;
    private static final int ROW_H = 8;
    private static final int SIDEBAR_W_RATIO = 30;
    private static final int PAD = 3;

    private final ThemeBridge theme;
    private final Minecraft mc;

    public GuiPreviewRenderer(ThemeBridge theme) {
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
    }

    public void renderClickGUIThumbnail(int x, int y, int w, int h) {
        DrawHelper.drawBorderedRect(x, y, x + w, y + h, 1.0f, theme.getBorderColor(), theme.getBgColor());
        DrawHelper.drawRect(x, y, x + w, y + HEADER_H, theme.getAccentColor());
        mc.fontRenderer.drawStringWithShadow("X4Tweaker", x + PAD, y + 4, 0xFFFFFFFF);

        int sidebarW = w * SIDEBAR_W_RATIO / 100;
        DrawHelper.drawRect(x, y + HEADER_H, x + sidebarW, y + h, theme.getSidebarBgColor());
        DrawHelper.drawRect(x + sidebarW, y + HEADER_H, x + w, y + h, theme.getContentBgColor());

        String[] cats = {"Visuals", "Combat", "Utility", "Tweaks", "Bots"};
        int catY = y + HEADER_H + 4;
        for (int i = 0; i < cats.length && catY + 8 < y + h; i++) {
            boolean sel = i == 0;
            if (sel) {
                DrawHelper.drawRect(x, catY, x + 2, catY + 8, theme.getAccentColor());
                DrawHelper.drawRect(x + 2, catY, x + sidebarW, catY + 8, theme.getSurfaceHoverColor());
            }
            mc.fontRenderer.drawStringWithShadow(cats[i], x + 4, catY + 1, sel ? 0xFFFFFFFF : 0xFF777777);
            catY += 10;
        }

        int rowX = x + sidebarW + PAD;
        int rowW = w - sidebarW - PAD * 2;
        int rowY = y + HEADER_H + PAD;
        for (int i = 0; i < 5 && rowY + ROW_H < y + h; i++) {
            boolean hover = i == 1;
            int bg = hover ? theme.getSurfaceHoverColor() : theme.getSurfaceColor();
            DrawHelper.drawRect(rowX, rowY, rowX + rowW, rowY + ROW_H, bg);
            String name = i == 0 ? "Fullbright" : (i == 1 ? "PlayerESP" : (i == 2 ? "AutoSprint" : (i == 3 ? "KillAura" : "Freecam")));
            mc.fontRenderer.drawStringWithShadow(name, rowX + 2, rowY + 1, 0xFFDDDDDD);
            if (i % 2 == 0) {
                int tw = 14;
                DrawHelper.drawBorderedRect(rowX + rowW - tw - 2, rowY + 1, rowX + rowW - 2, rowY + ROW_H - 1, 0.5f, theme.getEnabledDarkColor(), theme.getEnabledColor());
                mc.fontRenderer.drawStringWithShadow("ON", rowX + rowW - tw, rowY + 2, 0xFFFFFFFF);
            }
            rowY += ROW_H + 2;
        }
    }

    public void renderChangelogThumbnail(int x, int y, int w, int h) {
        DrawHelper.drawBorderedRect(x, y, x + w, y + h, 1.0f, 0xFF303030, 0x00000000);
        DrawHelper.drawRect(x, y, x + w, y + h, 0xE0151515);
        DrawHelper.drawGradientRectH(x, y, x + w, y + HEADER_H, 0xFF2A2A2A, 0xFF383838);

        mc.fontRenderer.drawStringWithShadow("Changelog", x + PAD, y + 4, 0xFFFFFFFF);

        int btnX = x + PAD + 40;
        DrawHelper.drawRect(btnX, y + 3, btnX + 30, y + 11, 0xFF333333);
        mc.fontRenderer.drawStringWithShadow("r1.0.3", btnX + 2, y + 4, 0xFFFFFFFF);

        DrawHelper.drawRect(btnX + 34, y + 3, btnX + 50, y + 11, 0xFF333333);
        mc.fontRenderer.drawStringWithShadow("ES", btnX + 38, y + 4, 0xFFFFFFFF);

        int contentY = y + HEADER_H + PAD;
        mc.fontRenderer.drawStringWithShadow("Changelog r1.0.3", x + PAD, contentY, 0xFF66FF66);
        contentY += 10;
        mc.fontRenderer.drawStringWithShadow("  - GUIs refactor completo", x + PAD, contentY, 0xFFE0E0E0);
        contentY += 10;
        mc.fontRenderer.drawStringWithShadow("  - Nuevo Theme Editor", x + PAD, contentY, 0xFFE0E0E0);
        contentY += 10;
        mc.fontRenderer.drawStringWithShadow("  - Click derecho para config", x + PAD, contentY, 0xFFE0E0E0);
        contentY += 10;
        mc.fontRenderer.drawStringWithShadow("  - Fix toggle visual bugs", x + PAD, contentY, 0xFFE0E0E0);
    }
}
