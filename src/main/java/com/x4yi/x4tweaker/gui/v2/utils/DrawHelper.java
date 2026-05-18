package com.x4yi.x4tweaker.gui.v2.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public final class DrawHelper {
    private static final int CIRCLE_SEGMENTS = 32;
    private static final float[] SIN_TABLE = new float[360];
    private static final float[] COS_TABLE = new float[360];

    static {
        for (int i = 0; i < 360; i++) {
            double rad = Math.toRadians(i);
            SIN_TABLE[i] = (float) Math.sin(rad);
            COS_TABLE[i] = (float) Math.cos(rad);
        }
    }

    private DrawHelper() {}

    public static void drawRect(int x1, int y1, int x2, int y2, int color) {
        if (x1 == x2 || y1 == y2) return;
        float a = (float)(color >> 24 & 255) / 255.0f;
        float r = (float)(color >> 16 & 255) / 255.0f;
        float g = (float)(color >> 8 & 255) / 255.0f;
        float b = (float)(color & 255) / 255.0f;
        GLHelper.setupBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1, y2, 0.0).color(r, g, b, a).endVertex();
        buffer.pos(x2, y2, 0.0).color(r, g, b, a).endVertex();
        buffer.pos(x2, y1, 0.0).color(r, g, b, a).endVertex();
        buffer.pos(x1, y1, 0.0).color(r, g, b, a).endVertex();
        tessellator.draw();
        GLHelper.restoreBlend();
    }

    public static void drawGradientRect(int x1, int y1, int x2, int y2, int topColor, int bottomColor) {
        float ta = (float)(topColor >> 24 & 255) / 255.0f;
        float tr = (float)(topColor >> 16 & 255) / 255.0f;
        float tg = (float)(topColor >> 8 & 255) / 255.0f;
        float tb = (float)(topColor & 255) / 255.0f;
        float ba = (float)(bottomColor >> 24 & 255) / 255.0f;
        float br = (float)(bottomColor >> 16 & 255) / 255.0f;
        float bg = (float)(bottomColor >> 8 & 255) / 255.0f;
        float bb = (float)(bottomColor & 255) / 255.0f;
        GLHelper.setupBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1, y2, 0.0).color(br, bg, bb, ba).endVertex();
        buffer.pos(x2, y2, 0.0).color(br, bg, bb, ba).endVertex();
        buffer.pos(x2, y1, 0.0).color(tr, tg, tb, ta).endVertex();
        buffer.pos(x1, y1, 0.0).color(tr, tg, tb, ta).endVertex();
        tessellator.draw();
        GLHelper.restoreBlend();
    }

    public static void drawGradientRectH(int x1, int y1, int x2, int y2, int leftColor, int rightColor) {
        float la = (float)(leftColor >> 24 & 255) / 255.0f;
        float lr = (float)(leftColor >> 16 & 255) / 255.0f;
        float lg = (float)(leftColor >> 8 & 255) / 255.0f;
        float lb = (float)(leftColor & 255) / 255.0f;
        float ra = (float)(rightColor >> 24 & 255) / 255.0f;
        float rr = (float)(rightColor >> 16 & 255) / 255.0f;
        float rg = (float)(rightColor >> 8 & 255) / 255.0f;
        float rb = (float)(rightColor & 255) / 255.0f;
        GLHelper.setupBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1, y2, 0.0).color(lr, lg, lb, la).endVertex();
        buffer.pos(x2, y2, 0.0).color(rr, rg, rb, ra).endVertex();
        buffer.pos(x2, y1, 0.0).color(rr, rg, rb, ra).endVertex();
        buffer.pos(x1, y1, 0.0).color(lr, lg, lb, la).endVertex();
        tessellator.draw();
        GLHelper.restoreBlend();
    }

    public static void drawBorderedRect(int x1, int y1, int x2, int y2, float borderWidth, int borderColor, int fillColor) {
        drawRect(x1, y1, x2, y2, fillColor);
        if (borderWidth > 0 && borderColor != 0x00000000) {
            float a = (float)(borderColor >> 24 & 255) / 255.0f;
            float r = (float)(borderColor >> 16 & 255) / 255.0f;
            float g = (float)(borderColor >> 8 & 255) / 255.0f;
            float b = (float)(borderColor & 255) / 255.0f;
            GLHelper.setupBlend();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            buffer.pos(x1, y1 + borderWidth, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x1, y2, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x1 + borderWidth, y2, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x1 + borderWidth, y1 + borderWidth, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x2 - borderWidth, y1, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x2, y1, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x2, y2, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x2 - borderWidth, y2, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x1, y1, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x2, y1, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x2, y1 + borderWidth, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x1, y1 + borderWidth, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x1, y2 - borderWidth, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x2, y2 - borderWidth, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x2, y2, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(x1, y2, 0.0).color(r, g, b, a).endVertex();
            tessellator.draw();
            GLHelper.restoreBlend();
        }
    }

    public static void drawRoundedRect(int x1, int y1, int x2, int y2, int radius, int color) {
        if (radius <= 0) {
            drawRect(x1, y1, x2, y2, color);
            return;
        }
        int r = Math.min(radius, (x2 - x1) / 2);
        r = Math.min(r, (y2 - y1) / 2);
        float a = (float)(color >> 24 & 255) / 255.0f;
        float cr = (float)(color >> 16 & 255) / 255.0f;
        float cg = (float)(color >> 8 & 255) / 255.0f;
        float cb = (float)(color & 255) / 255.0f;
        GLHelper.setupBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1 + r, y1, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x2 - r, y1, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x2 - r, y1 + r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1 + r, y1 + r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1, y1 + r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1, y2 - r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1 + r, y2 - r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1 + r, y1 + r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x2 - r, y1 + r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x2 - r, y2 - r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1 + r, y2 - r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1 + r, y1 + r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1 + r, y2 - r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x2 - r, y2 - r, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x2 - r, y2, 0.0).color(cr, cg, cb, a).endVertex();
        buffer.pos(x1 + r, y2, 0.0).color(cr, cg, cb, a).endVertex();
        drawArc(buffer, x1 + r, y1 + r, r, 180, 270, cr, cg, cb, a);
        drawArc(buffer, x2 - r, y1 + r, r, 270, 360, cr, cg, cb, a);
        drawArc(buffer, x1 + r, y2 - r, r, 90, 180, cr, cg, cb, a);
        drawArc(buffer, x2 - r, y2 - r, r, 0, 90, cr, cg, cb, a);
        tessellator.draw();
        GLHelper.restoreBlend();
    }

    private static void drawArc(BufferBuilder buffer, int cx, int cy, int radius, int startAngle, int endAngle, float r, float g, float b, float a) {
        for (int i = startAngle; i <= endAngle; i += 3) {
            int idx1 = i % 360;
            int idx2 = (i + 3) % 360;
            buffer.pos(cx, cy, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(cx + COS_TABLE[idx1] * radius, cy + SIN_TABLE[idx1] * radius, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(cx + COS_TABLE[idx2] * radius, cy + SIN_TABLE[idx2] * radius, 0.0).color(r, g, b, a).endVertex();
        }
    }

    public static void drawCircle(int cx, int cy, int radius, int color) {
        float a = (float)(color >> 24 & 255) / 255.0f;
        float r = (float)(color >> 16 & 255) / 255.0f;
        float g = (float)(color >> 8 & 255) / 255.0f;
        float b = (float)(color & 255) / 255.0f;
        GLHelper.setupBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        int step = 360 / CIRCLE_SEGMENTS;
        for (int i = 0; i < 360; i += step) {
            int idx1 = i % 360;
            int idx2 = (i + step) % 360;
            buffer.pos(cx, cy, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(cx + COS_TABLE[idx1] * radius, cy + SIN_TABLE[idx1] * radius, 0.0).color(r, g, b, a).endVertex();
            buffer.pos(cx + COS_TABLE[idx2] * radius, cy + SIN_TABLE[idx2] * radius, 0.0).color(r, g, b, a).endVertex();
        }
        tessellator.draw();
        GLHelper.restoreBlend();
    }

    public static void drawCircleOutline(int cx, int cy, int radius, int color, float lineWidth) {
        float a = (float)(color >> 24 & 255) / 255.0f;
        float r = (float)(color >> 16 & 255) / 255.0f;
        float g = (float)(color >> 8 & 255) / 255.0f;
        float b = (float)(color & 255) / 255.0f;
        GLHelper.setupBlend();
        GL11.glLineWidth(lineWidth);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(2, DefaultVertexFormats.POSITION_COLOR);
        int step = 360 / CIRCLE_SEGMENTS;
        for (int i = 0; i < 360; i += step) {
            int idx = i % 360;
            buffer.pos(cx + COS_TABLE[idx] * radius, cy + SIN_TABLE[idx] * radius, 0.0).color(r, g, b, a).endVertex();
        }
        tessellator.draw();
        GLHelper.restoreBlend();
    }

    public static void drawHueBar(int x, int y, int width, int height) {
        for (int dx = 0; dx < width; dx++) {
            float hue = (float) dx / (float) width;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
            drawRect(x + dx, y, x + dx + 1, y + height, rgb | 0xFF000000);
        }
    }

    public static void drawAlphaBar(int x, int y, int width, int height, int baseColor) {
        int transparent = baseColor & 0x00FFFFFF;
        int opaque = baseColor | 0xFF000000;
        drawGradientRectH(x, y, x + width, y + height, transparent, opaque);
    }

    public static void drawSatValSquare(int x, int y, int width, int height, float hue) {
        for (int dx = 0; dx < width; dx += 2) {
            for (int dy = 0; dy < height; dy += 2) {
                float sat = (float) dx / (float) width;
                float val = 1.0f - (float) dy / (float) height;
                int rgb = java.awt.Color.HSBtoRGB(hue, sat, val);
                drawRect(x + dx, y + dy, x + dx + 2, y + dy + 2, rgb | 0xFF000000);
            }
        }
    }
}
