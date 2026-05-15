package com.x4yi.x4tweaker.automation.control;

public class InputController {
    private boolean inputLocked;

    public void lockInput() {
        inputLocked = true;
    }

    public void unlockInput() {
        inputLocked = false;
    }

    public boolean isInputLocked() {
        return inputLocked;
    }
}
