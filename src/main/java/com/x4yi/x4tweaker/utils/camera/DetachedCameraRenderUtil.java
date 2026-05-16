package com.x4yi.x4tweaker.utils.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public final class DetachedCameraRenderUtil {
    private DetachedCameraRenderUtil() {}

    public static void renderLocalPlayerOpaque(Minecraft mc, float partialTicks) {
        if (mc == null || mc.player == null || mc.world == null) return;

        EntityPlayerSP player = mc.player;
        Entity backup = mc.getRenderManager().renderViewEntity;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            
            GlStateManager.enableLighting();
            net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            
            GlStateManager.enableCull();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

            mc.getRenderManager().renderViewEntity = player;
            mc.getRenderManager().renderEntityStatic(player, partialTicks, false);

            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        } finally {
            mc.getRenderManager().renderViewEntity = backup;
            GlStateManager.popMatrix();
            GL11.glPopAttrib();
        }
    }
}
