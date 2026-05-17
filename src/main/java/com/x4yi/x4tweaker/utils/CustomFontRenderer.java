package com.x4yi.x4tweaker.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class CustomFontRenderer {
    private Font font;
    private DynamicTexture tex;
    private int fontHeight = -1;
    private final int[] charWidth = new int[256];
    private int texWidth = 1024;
    private int texHeight = 1024;

    public CustomFontRenderer(Font font, boolean antiAlias) {
        this.font = font;
        setupMinecraftColorCodes();
        setupTexture(antiAlias);
    }

    private void setupMinecraftColorCodes() {

    }

    private void setupTexture(boolean antiAlias) {
        BufferedImage img = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setFont(font);
        g2d.setColor(new Color(255, 255, 255, 0));
        g2d.fillRect(0, 0, texWidth, texHeight);
        g2d.setColor(Color.WHITE);

        if (antiAlias) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        FontMetrics fontMetrics = g2d.getFontMetrics();
        int charHeight = fontMetrics.getHeight();
        if (charHeight <= 0) charHeight = font.getSize();
        fontHeight = charHeight;

        int x = 0;
        int y = 0;

        for (int i = 0; i < 256; i++) {
            char ch = (char) i;
            Rectangle2D bounds = fontMetrics.getStringBounds(String.valueOf(ch), g2d);
            int width = (int) bounds.getWidth();
            if (width <= 0) width = 1;

            if (x + width >= texWidth) {
                x = 0;
                y += charHeight;
            }

            g2d.drawString(String.valueOf(ch), x, y + fontMetrics.getAscent());
            charWidth[i] = width;
            x += width;
        }

        tex = new DynamicTexture(img);
    }

    public int drawString(String text, float x, float y, int color) {


        return Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color, false);
    }

    public int drawStringWithShadow(String text, float x, float y, int color) {
        return Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    public int getStringWidth(String text) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    public int getFontHeight() {
        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }
}
