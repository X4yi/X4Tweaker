package com.x4yi.x4tweaker.module.utility;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import com.x4yi.x4tweaker.utils.camera.RaytraceUtil;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;

public class Freecam extends DetachedCameraModule {

    private final NumberSetting speed = new NumberSetting("Speed", "Velocidad de vuelo", 1.0, 0.1, 5.0, 0.1);
    private final NumberSetting verticalSpeed = new NumberSetting("Vertical Speed", "Velocidad vertical", 1.0, 0.1, 5.0, 0.1);
    private final BooleanSetting renderPlayer = new BooleanSetting("Render Player", "Muestra el jugador real", true);
    private final BooleanSetting freezeRotations = new BooleanSetting("Freeze Rotations", "Congela la rotacion del jugador", true);

    private static final int ENTITY_ID = -42069;
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
    protected int getEntityId() {
        return ENTITY_ID;
    }

    @Override
    protected boolean shouldRenderPlayer() {
        return renderPlayer.getValue();
    }

    @Override
    protected void onCameraUpdate() {
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

        boolean isSprinting = Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode());
        fakeEntity.moveCamera(freecamInput, speed.getValue().floatValue(), verticalSpeed.getValue().floatValue(), isSprinting);
    }

    @Override
    protected void onCameraTurn(float yawDelta, float pitchDelta) {
        fakeEntity.turn(yawDelta, pitchDelta);
        if (!freezeRotations.getValue()) {
            mc.player.turn(yawDelta, pitchDelta);
        }
    }

    @SubscribeEvent
    public void onClientTickFreecam(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && fakeEntity != null) {
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
}
