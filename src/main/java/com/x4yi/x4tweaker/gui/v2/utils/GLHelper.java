package com.x4yi.x4tweaker.gui.v2.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public final class GLHelper {
    private GLHelper() {}

    private static final java.util.Stack<int[]> scissorStack = new java.util.Stack<int[]>();

    public static void enableScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        int glY = mc.displayHeight - (y + height) * scale;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, glY, width * scale, height * scale);
        scissorStack.push(new int[]{x, y, width, height});
    }

    public static void disableScissor() {
        scissorStack.clear();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public static void pushScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();

        if (!scissorStack.isEmpty()) {
            int[] parent = scissorStack.peek();
            int newX = Math.max(x, parent[0]);
            int newY = Math.max(y, parent[1]);
            int newW = Math.min(x + width, parent[0] + parent[2]) - newX;
            int newH = Math.min(y + height, parent[1] + parent[3]) - newY;
            if (newW <= 0 || newH <= 0) {
                GL11.glScissor(0, 0, 0, 0);
                scissorStack.push(new int[]{0, 0, 0, 0});
                return;
            }
            x = newX; y = newY; width = newW; height = newH;
        }

        int glY = mc.displayHeight - (y + height) * scale;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, glY, width * scale, height * scale);
        scissorStack.push(new int[]{x, y, width, height});
    }

    public static void popScissor() {
        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            return;
        }
        scissorStack.pop();
        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            int[] parent = scissorStack.peek();
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution sr = new ScaledResolution(mc);
            int scale = sr.getScaleFactor();
            int glY = mc.displayHeight - (parent[1] + parent[3]) * scale;
            GL11.glScissor(parent[0] * scale, glY, parent[2] * scale, parent[3] * scale);
        }
    }

    public static void pushMatrix() {
        GL11.glPushMatrix();
    }

    public static void popMatrix() {
        GL11.glPopMatrix();
    }

    public static void setupBlend() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void restoreBlend() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
