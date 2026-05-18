package com.x4yi.x4tweaker.gui.v2.framework;

public class InputRouter {
    private GuiComponent root;
    private GuiComponent focusedComponent;

    public void setRoot(GuiComponent root) {
        this.root = root;
    }

    public void setFocused(GuiComponent component) {
        this.focusedComponent = component;
    }

    public GuiComponent getFocused() {
        return focusedComponent;
    }

    public void routeClick(int mouseX, int mouseY, int button) {
        if (root != null) {
            root.onMouseClick(mouseX, mouseY, button);
        }
    }

    public void routeRelease(int mouseX, int mouseY, int button) {
        if (root != null) {
            root.onMouseRelease(mouseX, mouseY, button);
        }
    }

    public void routeMove(int mouseX, int mouseY, int dx, int dy) {
        if (root != null) {
            root.onMouseMove(mouseX, mouseY, dx, dy);
        }
    }

    public void routeKey(char typedChar, int keyCode) {
        if (focusedComponent != null) {
            focusedComponent.onKey(typedChar, keyCode);
        } else if (root != null) {
            root.onKey(typedChar, keyCode);
        }
    }

    public void update() {
        if (root != null) {
            root.update();
        }
    }
}
