package com.x4yi.x4tweaker.gui.v2.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Container implements GuiComponent {
    protected int x, y, width, height;
    protected boolean visible = true;
    protected final List<GuiComponent> children = new ArrayList<GuiComponent>();
    private List<GuiComponent> sortedCache = new ArrayList<GuiComponent>();
    private boolean dirty = true;

    private static final Comparator<GuiComponent> PRIORITY_COMPARATOR = new Comparator<GuiComponent>() {
        @Override
        public int compare(GuiComponent a, GuiComponent b) {
            return Integer.compare(a.getPriority(), b.getPriority());
        }
    };

    public void add(GuiComponent child) {
        children.add(child);
        dirty = true;
    }

    public void remove(GuiComponent child) {
        children.remove(child);
        dirty = true;
    }

    public void clear() {
        children.clear();
        dirty = true;
    }

    public List<GuiComponent> getChildren() {
        return children;
    }

    private void ensureSorted() {
        if (dirty) {
            sortedCache.clear();
            sortedCache.addAll(children);
            Collections.sort(sortedCache, PRIORITY_COMPARATOR);
            dirty = false;
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        ensureSorted();
        for (int i = 0; i < sortedCache.size(); i++) {
            GuiComponent c = sortedCache.get(i);
            if (c.isVisible()) {
                c.render(mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || !contains(mouseX, mouseY)) return false;
        ensureSorted();
        for (int i = sortedCache.size() - 1; i >= 0; i--) {
            GuiComponent c = sortedCache.get(i);
            if (c.isVisible() && c.onMouseClick(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) {
        if (!visible) return false;
        ensureSorted();
        for (int i = sortedCache.size() - 1; i >= 0; i--) {
            GuiComponent c = sortedCache.get(i);
            if (c.isVisible() && c.onMouseRelease(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseMove(int mouseX, int mouseY, int dx, int dy) {
        if (!visible) return false;
        ensureSorted();
        for (int i = sortedCache.size() - 1; i >= 0; i--) {
            GuiComponent c = sortedCache.get(i);
            if (c.isVisible() && c.onMouseMove(mouseX, mouseY, dx, dy)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKey(char typedChar, int keyCode) {
        if (!visible) return false;
        ensureSorted();
        for (int i = sortedCache.size() - 1; i >= 0; i--) {
            GuiComponent c = sortedCache.get(i);
            if (c.isVisible() && c.onKey(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update() {
        if (!visible) return;
        for (int i = 0; i < children.size(); i++) {
            children.get(i).update();
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }
    @Override
    public int getWidth() { return width; }
    @Override
    public int getHeight() { return height; }
    @Override
    public boolean isVisible() { return visible; }
    @Override
    public void setVisible(boolean visible) { this.visible = visible; }
    @Override
    public int getPriority() { return 0; }
    @Override
    public void setPriority(int priority) {}
}
