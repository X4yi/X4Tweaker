package com.x4yi.x4tweaker.module.utility;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import com.x4yi.x4tweaker.utils.camera.RaytraceUtil;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
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
    private boolean prevThirdPerson;

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

        prevThirdPerson = mc.gameSettings.thirdPersonView != 0;
        if (prevThirdPerson) mc.gameSettings.thirdPersonView = 0;

        fakeEntity = new FakeCameraEntity();
        mc.world.addEntityToWorld(ENTITY_ID, fakeEntity);
        mc.setRenderViewEntity(fakeEntity);
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
        fakeEntity = null;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null || fakeEntity == null) {
            setEnabled(false);
            return;
        }

        MovementInput input = new MovementInput();
        input.forwardKeyDown = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        input.backKeyDown = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        input.leftKeyDown = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        input.rightKeyDown = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        input.jump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        input.sneak = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());

        if (input.forwardKeyDown) input.moveForward++;
        if (input.backKeyDown) input.moveForward--;
        if (input.leftKeyDown) input.moveStrafe++;
        if (input.rightKeyDown) input.moveStrafe--;

        boolean isSprinting = Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode());

        fakeEntity.moveCamera(input, speed.getValue().floatValue(), verticalSpeed.getValue().floatValue(), isSprinting);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && fakeEntity != null) {
            com.x4yi.x4tweaker.utils.camera.CameraUtil.forceUpdateWalkingPlayer(mc);
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
                    float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                    float f1 = f * f * f * 8.0F;
                    float f2 = (float)dx * f1;
                    float f3 = (float)dy * f1;
                    int i = mc.gameSettings.invertMouse ? -1 : 1;


                    fakeEntity.turn(f2, f3 * (float)i);


                    if (!freezeRotations.getValue()) {
                        mc.player.turn(f2, f3 * (float)i);
                    }
                }
            }
            RaytraceUtil.updateMouseOver(event.renderTickTime);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (fakeEntity != null && renderPlayer.getValue() && mc.player != null) {
            net.minecraft.entity.Entity backup = mc.getRenderManager().renderViewEntity;
            mc.getRenderManager().renderViewEntity = mc.player;
            mc.getRenderManager().renderEntityStatic(mc.player, event.getPartialTicks(), false);
            mc.getRenderManager().renderViewEntity = backup;
        }
    }
}
