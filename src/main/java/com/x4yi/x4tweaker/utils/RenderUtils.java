package com.x4yi.x4tweaker.utils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public final class RenderUtils {

    private RenderUtils() {}

    public static void drawRect(float x, float y, float w, float h, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red   = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8  & 255) / 255.0F;
        float blue  = (color       & 255) / 255.0F;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(red, green, blue, alpha);

        buf.begin(7, DefaultVertexFormats.POSITION);
        buf.pos(x, h, 0.0).endVertex();
        buf.pos(w, h, 0.0).endVertex();
        buf.pos(w, y, 0.0).endVertex();
        buf.pos(x, y, 0.0).endVertex();
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawGradientRect(float x, float y, float w, float h, int color1, int color2) {
        float[] c1 = unpackColor(color1);
        float[] c2 = unpackColor(color2);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        buf.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(w, y, 0.0).color(c1[0], c1[1], c1[2], c1[3]).endVertex();
        buf.pos(x, y, 0.0).color(c1[0], c1[1], c1[2], c1[3]).endVertex();
        buf.pos(x, h, 0.0).color(c2[0], c2[1], c2[2], c2[3]).endVertex();
        buf.pos(w, h, 0.0).color(c2[0], c2[1], c2[2], c2[3]).endVertex();
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRectHorizontal(float x, float y, float w, float h, int color1, int color2) {
        float[] c1 = unpackColor(color1);
        float[] c2 = unpackColor(color2);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        buf.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x, y, 0.0).color(c1[0], c1[1], c1[2], c1[3]).endVertex();
        buf.pos(x, h, 0.0).color(c1[0], c1[1], c1[2], c1[3]).endVertex();
        buf.pos(w, h, 0.0).color(c2[0], c2[1], c2[2], c2[3]).endVertex();
        buf.pos(w, y, 0.0).color(c2[0], c2[1], c2[2], c2[3]).endVertex();
        tess.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawBorderedRect(float x, float y, float w, float h, float lineWidth, int lineColor, int bgColor) {
        drawRect(x, y, w, h, bgColor);

        float[] lc = unpackColor(lineColor);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(lc[0], lc[1], lc[2], lc[3]);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(lineWidth);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        buf.pos(x, h, 0.0).endVertex();
        buf.pos(w, h, 0.0).endVertex();
        buf.pos(w, y, 0.0).endVertex();
        buf.pos(x, y, 0.0).endVertex();
        buf.pos(x, h, 0.0).endVertex();
        tess.draw();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }


    /**
     * Rounded rect with batched GL state — sets up GL once, draws all geometry, restores once.
     * Avoids the 12+ redundant GL state toggles from calling drawRect/drawArcFill internally.
     */
    public static void drawRoundedRect(float x, float y, float w, float h, float radius, int color) {
        float[] c = unpackColor(color);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(c[0], c[1], c[2], c[3]);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Center rect
        buf.begin(7, DefaultVertexFormats.POSITION);
        buf.pos(x + radius, h, 0.0).endVertex();
        buf.pos(w - radius, h, 0.0).endVertex();
        buf.pos(w - radius, y, 0.0).endVertex();
        buf.pos(x + radius, y, 0.0).endVertex();
        tess.draw();

        // Left side rect
        buf.begin(7, DefaultVertexFormats.POSITION);
        buf.pos(x, h - radius, 0.0).endVertex();
        buf.pos(x + radius, h - radius, 0.0).endVertex();
        buf.pos(x + radius, y + radius, 0.0).endVertex();
        buf.pos(x, y + radius, 0.0).endVertex();
        tess.draw();

        // Right side rect
        buf.begin(7, DefaultVertexFormats.POSITION);
        buf.pos(w - radius, h - radius, 0.0).endVertex();
        buf.pos(w, h - radius, 0.0).endVertex();
        buf.pos(w, y + radius, 0.0).endVertex();
        buf.pos(w - radius, y + radius, 0.0).endVertex();
        tess.draw();

        // Corner arcs (reusing existing GL state)
        int segments = 8;
        drawArcFillRaw(buf, tess, x + radius, y + radius, radius, 180, 270, segments);
        drawArcFillRaw(buf, tess, w - radius, y + radius, radius, 270, 360, segments);
        drawArcFillRaw(buf, tess, x + radius, h - radius, radius, 90, 180, segments);
        drawArcFillRaw(buf, tess, w - radius, h - radius, radius, 0, 90, segments);

        GL11.glDisable(GL11.GL_POINT_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    /**
     * Arc fill without GL state management — caller is responsible for GL setup/teardown.
     */
    private static void drawArcFillRaw(BufferBuilder buf, Tessellator tess,
                                        float cx, float cy, float radius,
                                        int startAngle, int endAngle, int segments) {
        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        buf.pos(cx, cy, 0.0).endVertex();
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(startAngle + (endAngle - startAngle) * i / (double) segments);
            buf.pos(cx + Math.cos(angle) * radius, cy - Math.sin(angle) * radius, 0.0).endVertex();
        }
        tess.draw();
    }

    private static void drawArcFill(float cx, float cy, float radius, int startAngle, int endAngle, int segments, int color) {
        float[] c = unpackColor(color);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.color(c[0], c[1], c[2], c[3]);

        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        buf.pos(cx, cy, 0.0).endVertex();
        for (int i = 0; i <= segments; i++) {
            double angle = Math.toRadians(startAngle + (endAngle - startAngle) * i / (double) segments);
            buf.pos(cx + Math.cos(angle) * radius, cy - Math.sin(angle) * radius, 0.0).endVertex();
        }
        tess.draw();
    }

    public static void drawCircle(float cx, float cy, float radius, int color) {
        float[] c = unpackColor(color);
        int segments = 16;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(c[0], c[1], c[2], c[3]);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        buf.pos(cx, cy, 0.0).endVertex();
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2 * i / segments;
            buf.pos(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, 0.0).endVertex();
        }
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static float[] unpackColor(int color) {
        return new float[]{
            (color >> 16 & 255) / 255.0F,
            (color >> 8  & 255) / 255.0F,
            (color       & 255) / 255.0F,
            (color >> 24 & 255) / 255.0F
        };
    }
}
