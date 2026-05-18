package com.x4yi.x4tweaker.gui.v2.clickgui;

import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.module.Category;
import net.minecraft.client.Minecraft;

import java.util.Locale;

public class SidebarPanel implements GuiComponent {
    private static final int CATEGORY_ITEM_H = 20;
    private static final int PAD_X = 8;
    private static final int PAD_TOP = 6;
    private static final int PAD_BOTTOM = 6;
    private static final int SEPARATOR_H = 2;

    private int x, y, width, height;
    private boolean visible = true;
    private int priority = 0;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private Category selected;
    private final CategoryCallback onSelect;

    public interface CategoryCallback {
        void onCategorySelected(Category cat);
    }

    public SidebarPanel(ThemeBridge theme, Category selected, CategoryCallback onSelect) {
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.selected = selected;
        this.onSelect = onSelect;
    }

    public void setSelected(Category cat) {
        this.selected = cat;
    }

    public Category getSelected() {
        return selected;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        DrawHelper.drawBorderedRect(x, y, x + width, y + height, 1.0f, theme.getSeparatorColor(), theme.getSidebarBgColor());

        int curY = y + PAD_TOP;
        for (Category cat : Category.values()) {
            if (cat == Category.HIDDEN || cat == Category.THEME || cat == Category.KEYBINDS) continue;
            boolean isSelected = cat == selected;
            boolean hover = mouseX >= x && mouseX <= x + width && mouseY >= curY && mouseY <= curY + CATEGORY_ITEM_H;

            if (isSelected) {
                DrawHelper.drawRect(x, curY, x + 3, curY + CATEGORY_ITEM_H, theme.getAccentColor());
                DrawHelper.drawRect(x + 3, curY, x + width, curY + CATEGORY_ITEM_H, theme.getSurfaceHoverColor());
            } else if (hover) {
                DrawHelper.drawRect(x, curY, x + width, curY + CATEGORY_ITEM_H, theme.getSurfaceColor());
            }

            String label = formatCategory(cat);
            mc.fontRenderer.drawStringWithShadow(label, x + PAD_X, curY + 6, isSelected ? 0xFFFFFFFF : 0xFF999999);
            curY += CATEGORY_ITEM_H;
        }

        int keybindY = y + height - CATEGORY_ITEM_H - PAD_BOTTOM;
        boolean kbSelected = selected == Category.KEYBINDS;
        boolean kbHover = mouseX >= x && mouseX <= x + width && mouseY >= keybindY && mouseY <= keybindY + CATEGORY_ITEM_H;

        DrawHelper.drawRect(x, keybindY - SEPARATOR_H, x + width, keybindY, theme.getSeparatorColor());

        if (kbSelected) {
            DrawHelper.drawRect(x, keybindY, x + 3, keybindY + CATEGORY_ITEM_H, theme.getAccentColor());
            DrawHelper.drawRect(x + 3, keybindY, x + width, keybindY + CATEGORY_ITEM_H, theme.getSurfaceHoverColor());
        } else if (kbHover) {
            DrawHelper.drawRect(x, keybindY, x + width, keybindY + CATEGORY_ITEM_H, theme.getSurfaceColor());
        }

        mc.fontRenderer.drawStringWithShadow("Keybinds", x + PAD_X, keybindY + 6, kbSelected ? 0xFFFFFFFF : 0xFF999999);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0 || !contains(mouseX, mouseY)) return false;
        int curY = y + PAD_TOP;
        for (Category cat : Category.values()) {
            if (cat == Category.HIDDEN || cat == Category.THEME || cat == Category.KEYBINDS) continue;
            if (mouseY >= curY && mouseY <= curY + CATEGORY_ITEM_H) {
                selected = cat;
                if (onSelect != null) onSelect.onCategorySelected(cat);
                return true;
            }
            curY += CATEGORY_ITEM_H;
        }

        int keybindY = y + height - CATEGORY_ITEM_H - PAD_BOTTOM;
        if (mouseY >= keybindY && mouseY <= keybindY + CATEGORY_ITEM_H) {
            selected = Category.KEYBINDS;
            if (onSelect != null) onSelect.onCategorySelected(Category.KEYBINDS);
            return true;
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
    public int getPriority() { return priority; }
    @Override
    public void setPriority(int priority) { this.priority = priority; }

    private String formatCategory(Category cat) {
        String n = cat.name();
        return n.charAt(0) + n.substring(1).toLowerCase(Locale.ROOT);
    }
}
