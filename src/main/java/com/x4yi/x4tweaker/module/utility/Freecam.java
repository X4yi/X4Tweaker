package com.x4yi.x4tweaker.module.utility;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import com.x4yi.x4tweaker.utils.camera.DetachedCameraRenderUtil;
import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import com.x4yi.x4tweaker.utils.camera.RaytraceUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

public class Freecam extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", "Velocidad de vuelo", 1.0, 0.1, 5.0, 0.1);
    private final NumberSetting verticalSpeed = new NumberSetting("Vertical Speed", "Velocidad vertical", 1.0, 0.1, 5.0, 0.1);
    private final BooleanSetting renderPlayer = new BooleanSetting("Render Player", "Muestra el jugador real", true);
    private final BooleanSetting freezeRotations = new BooleanSetting("Freeze Rotations", "Congela la rotacion del jugador", true);

    private FakeCameraEntity fakeEntity;
    private static final int ENTITY_ID = -42069;
    private int prevThirdPersonView;
    private Entity overlayBackupViewEntity;
    private final MovementInput freecamInput = new MovementInput();

    public Freecam() {
        super("Freecam", "Cámara libre estilo espectador.", Category.UTILITY);
        addSetting(speed);
        addSetting(verticalSpeed);
        addSetting(renderPlayer);
        addSetting(freezeRotations);
    }

    @Override
    public List<Class<? extends Module>> getIncompatibilities() {
        return Arrays.<Class<? extends Module>>asList(CameraDetach.class);
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
        overlayBackupViewEntity = null;
        fakeEntity = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null || fakeEntity == null) {
            setEnabled(false);
            return;
        }

        freecamInput.moveForward = 0.0F;
        freecamInput.moveStrafe = 0.0F;
        freecamInput.forwardKeyDown = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        freecamInput.backKeyDown = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        freecamInput.leftKeyDown = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        freecamInput.rightKeyDown = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        freecamInput.jump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        freecamInput.sneak = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());

        if (freecamInput.forwardKeyDown) freecamInput.moveForward++;
        if (freecamInput.backKeyDown) freecamInput.moveForward--;
        if (freecamInput.leftKeyDown) freecamInput.moveStrafe++;
        if (freecamInput.rightKeyDown) freecamInput.moveStrafe--;

        mc.gameSettings.thirdPersonView = 0;

        boolean isSprinting = Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode());

        fakeEntity.moveCamera(freecamInput, speed.getValue().floatValue(), verticalSpeed.getValue().floatValue(), isSprinting);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START && fakeEntity != null) {
            RaytraceUtil.updateMouseOver(1.0F);
        } else if (event.phase == TickEvent.Phase.END && fakeEntity != null) {
            com.x4yi.x4tweaker.utils.camera.CameraUtil.forceUpdateWalkingPlayer(mc);
            if (mc.player != null && (mc.player.isDead || mc.player.getHealth() <= 0.0f)) {
                this.setEnabled(false);
            }
        }
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (fakeEntity == null) return;
        event.getMovementInput().moveForward = 0;
        event.getMovementInput().moveStrafe = 0;
        event.getMovementInput().forwardKeyDown = false;
        event.getMovementInput().backKeyDown = false;
        event.getMovementInput().leftKeyDown = false;
        event.getMovementInput().rightKeyDown = false;
        event.getMovementInput().jump = false;
        event.getMovementInput().sneak = false;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START && fakeEntity != null) {

            if (mc.inGameHasFocus && org.lwjgl.opengl.Display.isActive()) {
                int dx = org.lwjgl.input.Mouse.getDX();
                int dy = org.lwjgl.input.Mouse.getDY();

                if (dx != 0 || dy != 0) {
                    float[] delta = com.x4yi.x4tweaker.utils.camera.CameraUtil.calculateMouseDelta(dx, dy);

                    fakeEntity.turn(delta[0], delta[1]);

                    if (!freezeRotations.getValue()) {
                        mc.player.turn(delta[0], delta[1]);
                    }
                }
            }
            RaytraceUtil.updateMouseOver(event.renderTickTime);
        } else if (event.phase == TickEvent.Phase.END && fakeEntity != null) {
            RaytraceUtil.updateMouseOver(event.renderTickTime);
        }
    }

    @SubscribeEvent
    public void onMouseInput(net.minecraftforge.client.event.MouseEvent event) {
        if (fakeEntity != null) {
            RaytraceUtil.updateMouseOver(mc.getRenderPartialTicks());
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

        if (renderPlayer.getValue()) {
            isRenderingPlayer = true;
            DetachedCameraRenderUtil.renderLocalPlayerOpaque(mc, event.getPartialTicks());
            isRenderingPlayer = false;
        }
    }

    @SubscribeEvent
    public void onRenderHand(net.minecraftforge.client.event.RenderHandEvent event) {
        if (fakeEntity != null) {
            event.setCanceled(true);
        }
    }

    private boolean isDetachedActive() {
        return mc != null && mc.player != null && fakeEntity != null && mc.getRenderViewEntity() == fakeEntity;
    }
}
