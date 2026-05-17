package com.x4yi.x4tweaker.module.visuals;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.ModeSetting;
import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;


public abstract class ESPBase extends Module {
    protected final ModeSetting style = new ModeSetting("Style", "Estilo de renderizado ESP", "Lines&Boxes", "Lines&Boxes", "Boxes", "Lines");
    private final BooleanSetting renderRealEntity = new BooleanSetting("X-Ray Entity", "Ver entidad real a través de paredes", false);


    protected final float[] cachedColor = new float[4];
    private double tracerStartX, tracerStartY, tracerStartZ;

    public ESPBase(String name, String description) {
        super(name, description, Category.VISUALS);
        addSetting(style);
        addSetting(renderRealEntity);
    }

    protected abstract List<Entity> getEntities();


    protected abstract void fillColor(float[] out);

    @Override
    public void onRender3D() {
        if (mc.player == null || mc.world == null) return;

        List<Entity> entities = getEntities();
        if (entities.isEmpty()) return;

        fillColor(cachedColor);
        String currentStyle = style.getValue();
        boolean drawLines = currentStyle.equals("Lines") || currentStyle.equals("Lines&Boxes");
        boolean drawBoxes = currentStyle.equals("Boxes") || currentStyle.equals("Lines&Boxes");

        if (drawLines) {
            updateTracerStart(mc.getRenderPartialTicks());
        }

        pushRenderState();

        for (int i = 0, size = entities.size(); i < size; i++) {
            Entity entity = entities.get(i);
            if (!isRenderableTarget(entity)) continue;
            renderEntity(entity, cachedColor, drawLines, drawBoxes);
        }

        popRenderState();
    }

    protected void renderEntity(Entity entity, float[] color, boolean drawLines, boolean drawBoxes) {
        if (!isRenderableTarget(entity)) return;

        double partialTicks = mc.getRenderPartialTicks();
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB bb = entity.getEntityBoundingBox()
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(x, y, z);

        renderBounds(bb, color, drawLines, drawBoxes);
    }

    protected void renderBounds(AxisAlignedBB bb, float[] color, boolean drawLines, boolean drawBoxes) {
        if (drawLines) {
            double centerX = (bb.minX + bb.maxX) * 0.5;
            double centerY = (bb.minY + bb.maxY) * 0.5;
            double centerZ = (bb.minZ + bb.maxZ) * 0.5;
            drawTracerLine(tracerStartX, tracerStartY, tracerStartZ, centerX, centerY, centerZ, color);
        }
        if (drawBoxes) {
            RenderGlobal.renderFilledBox(bb, color[0], color[1], color[2], color[3] * 0.18f);
            RenderGlobal.drawSelectionBoundingBox(bb, color[0], color[1], color[2], color[3] * 0.8f);
        }
    }

    protected void pushRenderState() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(1.6f);
    }

    protected void popRenderState() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (!isEnabled() || !renderRealEntity.getValue()) return;
        if (isRenderableTarget(event.getEntity()) && getEntities().contains(event.getEntity())) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.disableLighting();
        }
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post<EntityLivingBase> event) {
        if (!isEnabled() || !renderRealEntity.getValue()) return;
        if (isRenderableTarget(event.getEntity()) && getEntities().contains(event.getEntity())) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
        }
    }

    protected boolean isRenderableTarget(Entity entity) {
        return entity != null && entity != mc.player && !(entity instanceof FakeCameraEntity);
    }

    protected void updateTracerStart(float partialTicks) {
        Entity camera = mc.getRenderViewEntity();
        if (camera == null) camera = mc.player;
        if (camera == null) {
            tracerStartX = 0.0D;
            tracerStartY = 0.0D;
            tracerStartZ = 0.0D;
            return;
        }

        java.nio.FloatBuffer modelView = org.lwjgl.BufferUtils.createFloatBuffer(16);
        org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX, modelView);


        float m00 = modelView.get(0), m01 = modelView.get(1), m02 = modelView.get(2);
        float m10 = modelView.get(4), m11 = modelView.get(5), m12 = modelView.get(6);
        float m20 = modelView.get(8), m21 = modelView.get(9), m22 = modelView.get(10);
        float m30 = modelView.get(12), m31 = modelView.get(13), m32 = modelView.get(14);


        float invTx = -(m00 * m30 + m01 * m31 + m02 * m32);
        float invTy = -(m10 * m30 + m11 * m31 + m12 * m32);
        float invTz = -(m20 * m30 + m21 * m31 + m22 * m32);


        float fwdX = -m02;
        float fwdY = -m12;
        float fwdZ = -m22;


        tracerStartX = invTx + fwdX;
        tracerStartY = invTy + fwdY;
        tracerStartZ = invTz + fwdZ;
    }

    protected void drawTracerLine(double startX, double startY, double startZ,
                                   double endX, double endY, double endZ, float[] color) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(color[0], color[1], color[2], color[3]);
        GL11.glVertex3d(startX, startY, startZ);
        GL11.glVertex3d(endX, endY, endZ);
        GL11.glEnd();
    }
}
