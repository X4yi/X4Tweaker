package com.x4yi.x4tweaker.gui.v2.framework;

public interface GuiComponent {
    void render(int mouseX, int mouseY, float partialTicks);
    boolean onMouseClick(int mouseX, int mouseY, int button);
    boolean onMouseRelease(int mouseX, int mouseY, int button);
    boolean onMouseMove(int mouseX, int mouseY, int dx, int dy);
    boolean onKey(char typedChar, int keyCode);
    void update();
    void setBounds(int x, int y, int width, int height);
    boolean contains(int mouseX, int mouseY);
    int getX();
    int getY();
    int getWidth();
    int getHeight();
    boolean isVisible();
    void setVisible(boolean visible);
    int getPriority();
    void setPriority(int priority);
}
