package com.x4yi.x4tweaker.gui.v2.theme;

import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.gui.v2.framework.ScrollablePanel;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.gui.v2.utils.GLHelper;
import net.minecraft.client.Minecraft;

import java.awt.Color;

public class ColorSlotList implements GuiComponent {
    private static final int ITEM_H = 22;
    private static final int SWATCH_SIZE = 12;
    private static final int PAD = 4;

    private int x, y, width, height;
    private boolean visible = true;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private final ScrollablePanel scrollPanel;
    private int selectedIndex = -1;
    private final Runnable onSelect;

    public ColorSlotList(ThemeBridge theme, Runnable onSelect) {
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.onSelect = onSelect;
        this.scrollPanel = new ScrollablePanel(0.2f);
    }

    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int idx) { this.selectedIndex = idx; }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        scrollPanel.updateScroll();
        DrawHelper.drawBorderedRect(x, y, x + width, y + height, 1.0f, theme.getSeparatorColor(), theme.getContentBgColor());
        GLHelper.enableScissor(x, y, width, height);

        int curY = y + (int)scrollPanel.getScrollOffset();
        int totalH = 0;
        for (int i = 0; i < theme.getColorCount(); i++) {
            boolean isSelected = i == selectedIndex;
            boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= curY && mouseY <= curY + ITEM_H;

            int bg = isSelected ? theme.getSurfaceHoverColor() : (hover ? theme.getSurfaceColor() : 0x00000000);
            DrawHelper.drawRect(x, curY, x + width, curY + ITEM_H, bg);

            int swatchX = x + PAD;
            int swatchY = curY + (ITEM_H - SWATCH_SIZE) / 2;
            int rgb = theme.getColorByIndex(i).getRGB();
            DrawHelper.drawBorderedRect(swatchX, swatchY, swatchX + SWATCH_SIZE, swatchY + SWATCH_SIZE, 1.0f, 0xFF000000, rgb);

            String name = theme.getColorName(i);
            mc.fontRenderer.drawStringWithShadow(name, swatchX + SWATCH_SIZE + 6, curY + 6, isSelected ? 0xFFFFFFFF : 0xFFCCCCCC);

            String hex = String.format("#%08X", rgb);
            int hexW = mc.fontRenderer.getStringWidth(hex);
            mc.fontRenderer.drawStringWithShadow(hex, x + width - hexW - PAD, curY + 6, 0xFF888888);

            curY += ITEM_H;
            totalH += ITEM_H;
        }
        scrollPanel.setContentHeight(totalH);
        scrollPanel.recalcMaxScroll(height);
        GLHelper.disableScissor();
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0 || !contains(mouseX, mouseY)) return false;
        int curY = y + (int)scrollPanel.getScrollOffset();
        for (int i = 0; i < theme.getColorCount(); i++) {
            if (mouseY >= curY && mouseY <= curY + ITEM_H) {
                selectedIndex = i;
                if (onSelect != null) onSelect.run();
                return true;
            }
            curY += ITEM_H;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) { return false; }
    @Override
    public boolean onMouseMove(int mouseX, int mouseY, int dx, int dy) { return false; }
    @Override
    public boolean onKey(char typedChar, int keyCode) { return false; }
    @Override
    public void update() {}

    public void handleMouseWheel(int dWheel) { scrollPanel.handleMouseWheel(dWheel); }

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
