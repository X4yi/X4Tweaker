package com.x4yi.x4tweaker.module.utility;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.ModeSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import com.x4yi.x4tweaker.utils.camera.DetachedCameraRenderUtil;
import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import com.x4yi.x4tweaker.utils.camera.RaytraceUtil;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

public class CameraDetach extends Module {

    private final ModeSetting lockMode = new ModeSetting("Lock Mode", "Modo de camara", "Absolute", "Absolute", "Follow Player", "Orbit", "Cinematic Drone");
    private final NumberSetting smoothSpeed = new NumberSetting("Smooth Speed", "Suavidad", 0.2, 0.05, 1.0, 0.05);
    private final NumberSetting panSpeed = new NumberSetting("Pan Speed", "Velocidad de paneo manual", 5.0, 1.0, 20.0, 1.0);

    private FakeCameraEntity fakeEntity;
    private static final int ENTITY_ID = -42070;
    private int prevThirdPersonView;
    private Entity overlayBackupViewEntity;

    private double orbitDistance = 5.0;
    private float orbitYaw = 0.0f;
    private float orbitPitch = 20.0f;

    public CameraDetach() {
        super("CameraDetach", "Desacopla la cámara manteniendo control del jugador.", Category.UTILITY);
        addSetting(lockMode);
        addSetting(smoothSpeed);
        addSetting(panSpeed);
    }

    @Override
    public List<Class<? extends Module>> getIncompatibilities() {
        return Arrays.<Class<? extends Module>>asList(Freecam.class);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            setEnabled(false);
            return;
        }

        prevThirdPersonView = mc.gameSettings.thirdPersonView;

        fakeEntity = new FakeCameraEntity();
        mc.world.addEntityToWorld(ENTITY_ID, fakeEntity);
        mc.setRenderViewEntity(fakeEntity);

        orbitDistance = 5.0;
        orbitYaw = mc.player.rotationYaw;
        orbitPitch = 20.0f;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.setRenderViewEntity(mc.player);
            if (mc.gameSettings.thirdPersonView != prevThirdPersonView) {
                mc.gameSettings.thirdPersonView = prevThirdPersonView;
            }
        }
        if (mc.world != null) {
            mc.world.removeEntityFromWorld(ENTITY_ID);
        }
        RaytraceUtil.updateMouseOver(mc.getRenderPartialTicks());
        overlayBackupViewEntity = null;
        fakeEntity = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null || fakeEntity == null) {
            setEnabled(false);
            return;
        }

        if (fakeEntity.world != mc.world) {
            fakeEntity = new FakeCameraEntity();
            mc.world.addEntityToWorld(ENTITY_ID, fakeEntity);
            mc.setRenderViewEntity(fakeEntity);
        } else if (mc.getRenderViewEntity() != fakeEntity) {
            mc.setRenderViewEntity(fakeEntity);
        }

        mc.gameSettings.thirdPersonView = 0;

        float pSpeed = panSpeed.getValue().floatValue();
        float smooth = smoothSpeed.getValue().floatValue();
        String mode = lockMode.getValue();

        if (mode.equals("Absolute")) {
            if (Keyboard.isKeyDown(Keyboard.KEY_UP)) fakeEntity.rotationPitch -= pSpeed;
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) fakeEntity.rotationPitch += pSpeed;
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) fakeEntity.rotationYaw -= pSpeed;
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) fakeEntity.rotationYaw += pSpeed;
        } else if (mode.equals("Follow Player")) {
            lookAtPlayer(smooth);
        } else if (mode.equals("Orbit")) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) orbitYaw -= pSpeed;
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) orbitYaw += pSpeed;
            if (Keyboard.isKeyDown(Keyboard.KEY_UP)) orbitPitch -= pSpeed;
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) orbitPitch += pSpeed;

            double radYaw = Math.toRadians(orbitYaw);
            double radPitch = Math.toRadians(orbitPitch);
            double tX = mc.player.posX - (Math.sin(radYaw) * Math.cos(radPitch) * orbitDistance);
            double tY = mc.player.posY + mc.player.getEyeHeight() - (Math.sin(radPitch) * orbitDistance);
            double tZ = mc.player.posZ + (Math.cos(radYaw) * Math.cos(radPitch) * orbitDistance);

            fakeEntity.posX += (tX - fakeEntity.posX) * smooth;
            fakeEntity.posY += (tY - fakeEntity.posY) * smooth;
            fakeEntity.posZ += (tZ - fakeEntity.posZ) * smooth;

            lookAtPlayer(1.0f);
        } else if (mode.equals("Cinematic Drone")) {

            double radYaw = Math.toRadians(mc.player.rotationYaw);
            double distance = 4.0;
            double height = 1.5;

            double tX = mc.player.posX + (Math.sin(radYaw) * distance);
            double tY = mc.player.posY + height;
            double tZ = mc.player.posZ - (Math.cos(radYaw) * distance);


            fakeEntity.posX += (tX - fakeEntity.posX) * (smooth * 0.3);
            fakeEntity.posY += (tY - fakeEntity.posY) * (smooth * 0.3);
            fakeEntity.posZ += (tZ - fakeEntity.posZ) * (smooth * 0.3);


            lookAtPlayer(smooth * 0.5f);
        }
    }

    private void lookAtPlayer(float smooth) {
        double diffX = mc.player.posX - fakeEntity.posX;
        double diffY = (mc.player.posY + mc.player.getEyeHeight()) - (fakeEntity.posY + fakeEntity.getEyeHeight());
        double diffZ = mc.player.posZ - fakeEntity.posZ;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f);
        float targetPitch = (float) -(Math.toDegrees(Math.atan2(diffY, dist)));

        fakeEntity.rotationYaw += getAngleDifference(targetYaw, fakeEntity.rotationYaw) * smooth;
        fakeEntity.rotationPitch += getAngleDifference(targetPitch, fakeEntity.rotationPitch) * smooth;
    }

    private float getAngleDifference(float target, float current) {
        float diff = ((target - current) % 360.0f);
        if (diff > 180.0f) diff -= 360.0f;
        if (diff < -180.0f) diff += 360.0f;
        return diff;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && fakeEntity != null) {
            RaytraceUtil.updateMouseOver(1.0F);
        }

        if (event.phase == TickEvent.Phase.END && fakeEntity != null && mc.player != null) {


            if (mc.player.movementInput != null) {
                mc.player.moveStrafing = mc.player.movementInput.moveStrafe;
                mc.player.moveForward = mc.player.movementInput.moveForward;
                mc.player.setJumping(mc.player.movementInput.jump);
            }

            com.x4yi.x4tweaker.utils.camera.CameraUtil.forceUpdateWalkingPlayer(mc);
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START && fakeEntity != null) {

            if (mc.inGameHasFocus && org.lwjgl.opengl.Display.isActive()) {
                int dx = org.lwjgl.input.Mouse.getDX();
                int dy = org.lwjgl.input.Mouse.getDY();

                if (dx != 0 || dy != 0) {
                    float[] delta = com.x4yi.x4tweaker.utils.camera.CameraUtil.calculateMouseDelta(dx, dy);

                    mc.player.turn(delta[0], delta[1]);
                }
            }
            RaytraceUtil.updateMouseOver(event.renderTickTime);
        } else if (event.phase == TickEvent.Phase.END && fakeEntity != null) {
            RaytraceUtil.updateMouseOver(event.renderTickTime);
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
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
    public void onDrawBlockHighlight(net.minecraftforge.client.event.DrawBlockHighlightEvent event) {
        if (isDetachedActive()) {
            event.setCanceled(true);
        }
    }

    private boolean isRenderingPlayer = false;

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
                double rx = mc.getRenderManager().viewerPosX;
                double ry = mc.getRenderManager().viewerPosY;
                double rz = mc.getRenderManager().viewerPosZ;

                net.minecraft.client.renderer.GlStateManager.enableBlend();
                net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA, net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE, net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO);
                net.minecraft.client.renderer.GlStateManager.glLineWidth(2.0F);
                net.minecraft.client.renderer.GlStateManager.disableTexture2D();
                net.minecraft.client.renderer.GlStateManager.depthMask(false);

                net.minecraft.util.math.AxisAlignedBB bb = state.getSelectedBoundingBox(mc.world, pos).grow(0.0020000000949949026D).offset(-rx, -ry, -rz);
                net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox(bb, 0.0F, 0.0F, 0.0F, 0.4F);

                net.minecraft.client.renderer.GlStateManager.depthMask(true);
                net.minecraft.client.renderer.GlStateManager.enableTexture2D();
                net.minecraft.client.renderer.GlStateManager.disableBlend();
            }
        }

        isRenderingPlayer = true;
        DetachedCameraRenderUtil.renderLocalPlayerOpaque(mc, event.getPartialTicks());
        isRenderingPlayer = false;
    }

    private boolean isDetachedActive() {
        return mc != null && mc.player != null && fakeEntity != null && mc.getRenderViewEntity() == fakeEntity;
    }

}
