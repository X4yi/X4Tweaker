package com.x4yi.x4tweaker.module.visuals;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.ModeSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public abstract class ESPBase extends Module {
    protected final ModeSetting style = new ModeSetting("Style", "Estilo de renderizado ESP", "Lines&Boxes", "Lines&Boxes", "Boxes", "Lines");
    private final BooleanSetting renderRealEntity = new BooleanSetting("X-Ray Entity", "Ver entidad real a través de paredes", false);

    public ESPBase(String name, String description) {
        super(name, description, Category.VISUALS);
        addSetting(style);
        addSetting(renderRealEntity);
    }

    protected abstract List<Entity> getEntities();
    protected abstract float[] getColor();

    @Override
    public void onRender3D() {
        if (mc.player == null || mc.world == null) return;

        List<Entity> entities = getEntities();
        if (entities.isEmpty()) return;

        float[] color = getColor();
        String currentStyle = style.getValue();
        boolean drawLines = currentStyle.equals("Lines") || currentStyle.equals("Lines&Boxes");
        boolean drawBoxes = currentStyle.equals("Boxes") || currentStyle.equals("Lines&Boxes");

        pushRenderState();

        for (int i = 0, size = entities.size(); i < size; i++) {
            Entity entity = entities.get(i);
            if (entity == mc.player) continue;
            renderEntity(entity, color, drawLines, drawBoxes);
        }

        popRenderState();
    }

    protected void renderEntity(Entity entity, float[] color, boolean drawLines, boolean drawBoxes) {
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
            Vec3d tracerStart = getTracerStart();
            double centerX = (bb.minX + bb.maxX) * 0.5;
            double centerY = (bb.minY + bb.maxY) * 0.5;
            double centerZ = (bb.minZ + bb.maxZ) * 0.5;
            drawTracerLine(tracerStart, centerX, centerY, centerZ, color);
        }
        if (drawBoxes) {
            RenderGlobal.renderFilledBox(bb, color[0], color[1], color[2], color[3] * 0.4f);
            RenderGlobal.drawSelectionBoundingBox(bb, color[0], color[1], color[2], color[3]);
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
        GL11.glLineWidth(3.0f);
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
        if (getEntities().contains(event.getEntity())) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.disableLighting();
        }
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post<EntityLivingBase> event) {
        if (!isEnabled() || !renderRealEntity.getValue()) return;
        if (getEntities().contains(event.getEntity())) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
        }
    }

    protected Vec3d getTracerStart() {
        Vec3d look = mc.player.getLook(mc.getRenderPartialTicks());
        return new Vec3d(look.x * 1.5D, look.y * 1.5D, look.z * 1.5D);
    }

    protected void drawTracerLine(Vec3d start, double endX, double endY, double endZ, float[] color) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(color[0], color[1], color[2], color[3]);
        GL11.glVertex3d(start.x, start.y, start.z);
        GL11.glVertex3d(endX, endY, endZ);
        GL11.glEnd();
    }
}
