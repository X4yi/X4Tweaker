package com.x4yi.x4tweaker.gui.v2.framework;

import com.x4yi.x4tweaker.gui.v2.utils.GLHelper;
import org.lwjgl.input.Mouse;

public class ScrollablePanel extends Container {
    protected float scrollOffset = 0;
    protected float targetScrollOffset = 0;
    protected float scrollVelocity = 0;
    protected int maxScroll = 0;
    protected int contentHeight = 0;
    protected final float lerpSpeed;

    public ScrollablePanel(float lerpSpeed) {
        this.lerpSpeed = lerpSpeed;
    }

    public ScrollablePanel() {
        this(0.18f);
    }

    public void handleMouseWheel(int dWheel) {
        if (dWheel == 0) return;
        if (dWheel < 0) {
            targetScrollOffset -= 20;
        } else {
            targetScrollOffset += 20;
        }
        targetScrollOffset = Math.max(-maxScroll, Math.min(0, targetScrollOffset));
    }

    public void updateScroll() {
        scrollOffset = AnimationHelper.lerp(scrollOffset, targetScrollOffset, lerpSpeed);
        if (Math.abs(scrollOffset - targetScrollOffset) < 0.5f) {
            scrollOffset = targetScrollOffset;
        }
    }

    public void recalcMaxScroll(int viewHeight) {
        maxScroll = Math.max(0, contentHeight - viewHeight);
        if (-targetScrollOffset > maxScroll) {
            targetScrollOffset = -maxScroll;
        }
    }

    public int getScrollOffset() {
        return (int) scrollOffset;
    }

    public void setScrollOffset(int offset) {
        this.scrollOffset = offset;
        this.targetScrollOffset = offset;
    }

    public void resetScroll() {
        scrollOffset = 0;
        targetScrollOffset = 0;
        scrollVelocity = 0;
    }

    public void beginClip() {
        GLHelper.enableScissor(x, y, width, height);
    }

    public void endClip() {
        GLHelper.disableScissor();
    }

    public boolean isInsideViewport(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int getContentHeight() {
        return contentHeight;
    }

    public void setContentHeight(int h) {
        this.contentHeight = h;
    }

    @Override
    public int getPriority() { return 0; }
    @Override
    public void setPriority(int priority) {}
}
