package com.x4yi.x4tweaker.module.utility;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.utils.camera.DetachedCameraRenderUtil;
import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import com.x4yi.x4tweaker.utils.camera.RaytraceUtil;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public abstract class DetachedCameraModule extends Module {

    protected FakeCameraEntity fakeEntity;
    protected int prevThirdPersonView;
    protected Entity overlayBackupViewEntity;
    protected boolean isRenderingPlayer = false;

    public DetachedCameraModule(String name, String description, Category category) {
        super(name, description, category);
    }

    protected abstract int getEntityId();

    protected abstract void onCameraUpdate();

    protected boolean shouldRenderPlayer() {
        return true;
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            setEnabled(false);
            return;
        }

        prevThirdPersonView = mc.gameSettings.thirdPersonView;

        fakeEntity = new FakeCameraEntity();
        mc.world.addEntityToWorld(getEntityId(), fakeEntity);
        mc.setRenderViewEntity(fakeEntity);

        onCameraEnabled();
    }

    protected void onCameraEnabled() {}

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.setRenderViewEntity(mc.player);
            if (mc.gameSettings.thirdPersonView != prevThirdPersonView) {
                mc.gameSettings.thirdPersonView = prevThirdPersonView;
            }
        }
        if (mc.world != null) {
            mc.world.removeEntityFromWorld(getEntityId());
        }
        overlayBackupViewEntity = null;
        fakeEntity = null;
        isRenderingPlayer = false;

        onCameraDisabled();
    }

    protected void onCameraDisabled() {}

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null || fakeEntity == null) {
            setEnabled(false);
            return;
        }

        if (fakeEntity.world != mc.world) {
            fakeEntity = new FakeCameraEntity();
            mc.world.addEntityToWorld(getEntityId(), fakeEntity);
            mc.setRenderViewEntity(fakeEntity);
        } else if (mc.getRenderViewEntity() != fakeEntity) {
            mc.setRenderViewEntity(fakeEntity);
        }

        mc.gameSettings.thirdPersonView = 0;

        onCameraUpdate();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && fakeEntity != null) {
            RaytraceUtil.updateMouseOver(1.0F);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || fakeEntity == null) return;

        if (mc.inGameHasFocus && org.lwjgl.opengl.Display.isActive()) {
            int dx = org.lwjgl.input.Mouse.getDX();
            int dy = org.lwjgl.input.Mouse.getDY();

            if (dx != 0 || dy != 0) {
                float[] delta = com.x4yi.x4tweaker.utils.camera.CameraUtil.calculateMouseDelta(dx, dy);
                onCameraTurn(delta[0], delta[1]);
            }
        }
        RaytraceUtil.updateMouseOver(event.renderTickTime);
    }

    protected void onCameraTurn(float yawDelta, float pitchDelta) {
        mc.player.turn(yawDelta, pitchDelta);
    }

    @SubscribeEvent
    public void onMouseInput(net.minecraftforge.client.event.MouseEvent event) {
        if (fakeEntity != null) {
            RaytraceUtil.updateMouseOver(mc.getRenderPartialTicks());
        }
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        if (fakeEntity != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (!isDetachedActive()) return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (fakeEntity == null || mc.player == null) return;
        if (!isDetachedActive()) return;
        if (event.getEntityPlayer() == fakeEntity) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntityPlayer() == mc.player && !isRenderingPlayer) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!isDetachedActive()) return;
        if (overlayBackupViewEntity != null) return;
        overlayBackupViewEntity = mc.getRenderViewEntity();
        mc.setRenderViewEntity(mc.player);
    }

    @SubscribeEvent
    public void onOverlayPost(RenderGameOverlayEvent.Post event) {
        if (overlayBackupViewEntity == null) return;
        mc.setRenderViewEntity(overlayBackupViewEntity);
        overlayBackupViewEntity = null;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isDetachedActive()) return;

        RaytraceUtil.updateMouseOver(event.getPartialTicks());

        net.minecraft.util.math.RayTraceResult rtr = mc.objectMouseOver;
        if (rtr != null && rtr.typeOfHit == net.minecraft.util.math.RayTraceResult.Type.BLOCK) {
            net.minecraft.util.math.BlockPos pos = rtr.getBlockPos();
            net.minecraft.block.state.IBlockState state = mc.world.getBlockState(pos);
            if (state.getMaterial() != net.minecraft.block.material.Material.AIR && mc.world.getWorldBorder().contains(pos)) {
                drawBlockHighlight(pos);
            }
        }

        if (shouldRenderPlayer()) {
            isRenderingPlayer = true;
            DetachedCameraRenderUtil.renderLocalPlayerOpaque(mc, event.getPartialTicks());
            isRenderingPlayer = false;
        }
    }

    private void drawBlockHighlight(net.minecraft.util.math.BlockPos pos) {
        double rx = mc.getRenderManager().viewerPosX;
        double ry = mc.getRenderManager().viewerPosY;
        double rz = mc.getRenderManager().viewerPosZ;

        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(
            net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
            net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE,
            net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
        );
        net.minecraft.client.renderer.GlStateManager.glLineWidth(2.0F);
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();
        net.minecraft.client.renderer.GlStateManager.depthMask(false);

        net.minecraft.util.math.AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos)
            .grow(0.0020000000949949026D).offset(-rx, -ry, -rz);
        net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox(bb, 0.0F, 0.0F, 0.0F, 0.4F);

        net.minecraft.client.renderer.GlStateManager.depthMask(true);
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        net.minecraft.client.renderer.GlStateManager.disableBlend();
    }

    protected boolean isDetachedActive() {
        return mc != null && mc.player != null && fakeEntity != null && mc.getRenderViewEntity() == fakeEntity;
    }
}
