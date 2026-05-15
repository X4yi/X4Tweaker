package com.x4yi.x4tweaker.module.utility;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.ModeSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import com.x4yi.x4tweaker.utils.camera.RaytraceUtil;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class CameraDetach extends Module {

    private final ModeSetting lockMode = new ModeSetting("Lock Mode", "Modo de camara", "Absolute", "Absolute", "Follow Player", "Orbit", "Cinematic Drone");
    private final NumberSetting smoothSpeed = new NumberSetting("Smooth Speed", "Suavidad", 0.2, 0.05, 1.0, 0.05);
    private final NumberSetting panSpeed = new NumberSetting("Pan Speed", "Velocidad de paneo manual", 5.0, 1.0, 20.0, 1.0);

    private FakeCameraEntity fakeEntity;
    private static final int ENTITY_ID = -42070;
    private boolean prevThirdPerson;

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
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            setEnabled(false);
            return;
        }

        prevThirdPerson = mc.gameSettings.thirdPersonView != 0;
        if (prevThirdPerson) mc.gameSettings.thirdPersonView = 0;

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
            if (prevThirdPerson) mc.gameSettings.thirdPersonView = 1;
        }
        if (mc.world != null) {
            mc.world.removeEntityFromWorld(ENTITY_ID);
        }
        RaytraceUtil.updateMouseOver(mc.getRenderPartialTicks());
        fakeEntity = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null || fakeEntity == null) {
            setEnabled(false);
            return;
        }

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
                    float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                    float f1 = f * f * f * 8.0F;
                    float f2 = (float)dx * f1;
                    float f3 = (float)dy * f1;
                    int i = mc.gameSettings.invertMouse ? -1 : 1;


                    mc.player.turn(f2, f3 * (float)i);
                }
            }
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
    public void onRenderWorld(net.minecraftforge.client.event.RenderWorldLastEvent event) {
        if (fakeEntity != null && mc.player != null) {
            net.minecraft.entity.Entity backup = mc.getRenderManager().renderViewEntity;
            mc.getRenderManager().renderViewEntity = mc.player;
            mc.getRenderManager().renderEntityStatic(mc.player, event.getPartialTicks(), false);
            mc.getRenderManager().renderViewEntity = backup;
        }
    }
}
