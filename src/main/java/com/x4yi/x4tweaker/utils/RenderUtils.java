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

    public static void dibujarRect(float x, float y, float w, float h, int color) {
        drawRect(x, y, w, h, color);
    }

    public static void dibujarRectGradienteHorizontal(float x, float y, float w, float h, int color1, int color2) {
        drawGradientRectHorizontal(x, y, w, h, color1, color2);
    }

    public static void dibujarRectBordeado(float x, float y, float w, float h, float lineWidth, int lineColor, int bgColor) {
        drawBorderedRect(x, y, w, h, lineWidth, lineColor, bgColor);
    }

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

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();


        drawRect(x + radius, y, w - radius, h, color);

        drawRect(x, y + radius, x + radius, h - radius, color);
        drawRect(w - radius, y + radius, w, h - radius, color);


        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        int segments = 8;
        GlStateManager.color(c[0], c[1], c[2], c[3]);

        drawArcFill(x + radius, y + radius, radius, 180, 270, segments, color);

        drawArcFill(w - radius, y + radius, radius, 270, 360, segments, color);

        drawArcFill(x + radius, h - radius, radius, 90, 180, segments, color);

        drawArcFill(w - radius, h - radius, radius, 0, 90, segments, color);
        GL11.glDisable(GL11.GL_POINT_SMOOTH);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
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
