package com.x4yi.x4tweaker.automation.control;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class KeybindDriver {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private boolean forwardActive;
    private boolean backActive;
    private boolean leftActive;
    private boolean rightActive;
    private boolean jumpActive;
    private boolean sprintActive;

    public void setForward(boolean pressed) {
        if (forwardActive == pressed) return;
        forwardActive = pressed;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), pressed);
        if (pressed) backActive = false;
        if (pressed) KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
    }

    public void setBack(boolean pressed) {
        if (backActive == pressed) return;
        backActive = pressed;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), pressed);
        if (pressed) forwardActive = false;
        if (pressed) KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
    }

    public void setStrafeLeft(boolean pressed) {
        if (leftActive == pressed) return;
        leftActive = pressed;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), pressed);
        if (pressed) rightActive = false;
        if (pressed) KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
    }

    public void setStrafeRight(boolean pressed) {
        if (rightActive == pressed) return;
        rightActive = pressed;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), pressed);
        if (pressed) leftActive = false;
        if (pressed) KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
    }

    public void setJump(boolean pressed) {
        if (jumpActive == pressed) return;
        jumpActive = pressed;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), pressed);
    }

    public void setSprint(boolean pressed) {
        if (sprintActive == pressed) return;
        sprintActive = pressed;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), pressed);
    }

    public void releaseAll() {
        setForward(false);
        setBack(false);
        setStrafeLeft(false);
        setStrafeRight(false);
        setJump(false);
        setSprint(false);
    }

    public boolean isForwardActive() { return forwardActive; }
    public boolean isBackActive() { return backActive; }
    public boolean isLeftActive() { return leftActive; }
    public boolean isRightActive() { return rightActive; }
}
