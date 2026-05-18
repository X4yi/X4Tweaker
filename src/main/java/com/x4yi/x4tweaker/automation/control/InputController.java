package com.x4yi.x4tweaker.automation.control;

import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InputController {
    private boolean inputLocked;

    public InputController() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void lockInput() {
        inputLocked = true;
    }

    public void unlockInput() {
        inputLocked = false;
    }

    public boolean isInputLocked() {
        return inputLocked;
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (!inputLocked) return;
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
