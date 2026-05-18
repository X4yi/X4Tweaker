package com.x4yi.x4tweaker.gui.v2.framework;

import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import net.minecraft.client.Minecraft;

import java.util.List;

public class Tooltip {
    private static final int PAD = 4;
    private static final int LINE_H = 10;
    private static final int OFFSET = 10;

    public static void render(List<String> lines, int mouseX, int mouseY, int screenWidth, int screenHeight, int bgColor, int borderColor) {
        if (lines == null || lines.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        int maxWidth = 0;
        for (int i = 0; i < lines.size(); i++) {
            int w = mc.fontRenderer.getStringWidth(lines.get(i));
            if (w > maxWidth) maxWidth = w;
        }
        int boxW = maxWidth + PAD * 2;
        int boxH = lines.size() * LINE_H + PAD * 2;
        int tx = mouseX + OFFSET;
        int ty = mouseY + OFFSET;
        if (tx + boxW > screenWidth) tx = mouseX - boxW - OFFSET;
        if (ty + boxH > screenHeight) ty = mouseY - boxH - OFFSET;
        if (tx < 0) tx = 0;
        if (ty < 0) ty = 0;
        DrawHelper.drawBorderedRect(tx, ty, tx + boxW, ty + boxH, 1.0f, borderColor, bgColor);
        for (int i = 0; i < lines.size(); i++) {
            mc.fontRenderer.drawStringWithShadow(lines.get(i), tx + PAD, ty + PAD + i * LINE_H, 0xFFFFFFFF);
        }
    }
}
