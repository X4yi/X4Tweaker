package com.x4yi.x4tweaker.gui.v2.clickgui;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.gui.v2.framework.ScrollablePanel;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.gui.v2.utils.GLHelper;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.module.client.ClickGUIModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class KeybindOverlay implements GuiComponent {
    private static final int PANEL_W = 220;
    private static final int PANEL_H = 220;
    private static final int ITEM_H = 14;
    private static final int PAD = 10;

    private int x, y, width, height;
    private boolean visible = true;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private final Runnable onClose;
    private int bindingKey = Keyboard.KEY_NONE;
    private Module bindingModule = null;
    private final ScrollablePanel scrollPanel;
    private boolean canConfirm = false;

    public KeybindOverlay(ThemeBridge theme, Runnable onClose) {
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.onClose = onClose;
        this.width = PANEL_W;
        this.height = PANEL_H;
        this.x = (mc.displayWidth - width) / 2;
        this.y = (mc.displayHeight - height) / 2;
        this.scrollPanel = new ScrollablePanel(0.2f);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        DrawHelper.drawRect(0, 0, mc.displayWidth, mc.displayHeight, 0xAA000000);
        DrawHelper.drawBorderedRect(x - 1, y - 1, x + width + 1, y + height + 1, 1.5f, theme.getBorderColor(), theme.getBgColor());
        DrawHelper.drawRect(x, y, x + width, y + height, theme.getBgColor());
        mc.fontRenderer.drawStringWithShadow("Add Keybind", x + PAD, y + PAD, 0xFFFFFFFF);

        String keyText = bindingKey == Keyboard.KEY_NONE ? "Press a key..." : "Key: " + Keyboard.getKeyName(bindingKey);
        mc.fontRenderer.drawStringWithShadow(keyText, x + PAD, y + PAD + 14, bindingKey == Keyboard.KEY_NONE ? 0xFFFF5555 : 0xFF55FF55);

        int listTop = y + PAD + 36;
        int listBot = y + height - PAD - 24;
        int listW = width - PAD * 2;
        int listH = listBot - listTop;

        DrawHelper.drawBorderedRect(x + PAD, listTop, x + PAD + listW, listBot, 1.0f, theme.getSeparatorColor(), theme.getContentBgColor());

        GLHelper.enableScissor(x + PAD, listTop, listW, listH);
        scrollPanel.updateScroll();

        int curY = listTop + (int)scrollPanel.getScrollOffset();
        int totalH = 0;
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (Module m : modules) {
            if (m instanceof ClickGUIModule) continue;
            boolean isSelected = bindingModule == m;
            boolean hover = mouseX >= x + PAD && mouseX <= x + PAD + listW && mouseY >= curY && mouseY <= curY + ITEM_H;
            if (isSelected) {
                DrawHelper.drawRect(x + PAD, curY, x + PAD + listW, curY + ITEM_H, theme.getEnabledColor());
            } else if (hover) {
                DrawHelper.drawRect(x + PAD, curY, x + PAD + listW, curY + ITEM_H, theme.getSurfaceHoverColor());
            }
            String name = getDisplayName(m);
            mc.fontRenderer.drawStringWithShadow(name, x + PAD + 4, curY + 3, isSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
            curY += ITEM_H;
            totalH += ITEM_H;
        }
        scrollPanel.setContentHeight(totalH);
        scrollPanel.recalcMaxScroll(listH);
        GLHelper.disableScissor();

        canConfirm = bindingKey != Keyboard.KEY_NONE && bindingModule != null;
        int btnY = y + height - PAD - 20;
        boolean confHover = canConfirm && mouseX >= x + PAD && mouseX <= x + PAD + 80 && mouseY >= btnY && mouseY <= btnY + 16;
        DrawHelper.drawRect(x + PAD, btnY, x + PAD + 80, btnY + 16, canConfirm ? (confHover ? 0xFF00AA00 : 0xFF008800) : 0xFF444444);
        mc.fontRenderer.drawStringWithShadow("Confirm", x + PAD + 22, btnY + 4, canConfirm ? 0xFFFFFFFF : 0xFFAAAAAA);

        boolean cancelHover = mouseX >= x + PAD + 86 && mouseX <= x + PAD + 86 + 80 && mouseY >= btnY && mouseY <= btnY + 16;
        DrawHelper.drawRect(x + PAD + 86, btnY, x + PAD + 86 + 80, btnY + 16, cancelHover ? 0xFFAA0000 : 0xFF880000);
        mc.fontRenderer.drawStringWithShadow("Cancel", x + PAD + 86 + 22, btnY + 4, 0xFFFFFFFF);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0) return false;
        int listTop = y + PAD + 36;
        int listBot = y + height - PAD - 24;
        int listW = width - PAD * 2;
        int curY = listTop + (int)scrollPanel.getScrollOffset();
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (Module m : modules) {
            if (m instanceof ClickGUIModule) continue;
            if (mouseY >= curY && mouseY <= curY + ITEM_H && mouseX >= x + PAD && mouseX <= x + PAD + listW) {
                bindingModule = m;
                return true;
            }
            curY += ITEM_H;
        }

        int btnY = y + height - PAD - 20;
        if (canConfirm && mouseX >= x + PAD && mouseX <= x + PAD + 80 && mouseY >= btnY && mouseY <= btnY + 16) {
            bindingModule.setKeybind(bindingKey);
            X4TweakerClient.getInstance().getConfigManager().save();
            if (onClose != null) onClose.run();
            return true;
        }
        if (mouseX >= x + PAD + 86 && mouseX <= x + PAD + 86 + 80 && mouseY >= btnY && mouseY <= btnY + 16) {
            if (onClose != null) onClose.run();
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) { return false; }
    @Override
    public boolean onMouseMove(int mouseX, int mouseY, int dx, int dy) { return false; }

    @Override
    public boolean onKey(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (onClose != null) onClose.run();
            return true;
        }
        bindingKey = keyCode;
        return true;
    }

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
        return true;
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
    public int getPriority() { return 200; }
    @Override
    public void setPriority(int priority) {}

    public void handleMouseWheel(int dWheel) {
        scrollPanel.handleMouseWheel(dWheel);
    }

    private String getDisplayName(Module m) {
        String key = "x4tweaker.module." + com.x4yi.x4tweaker.utils.I18nUtils.normalizeKey(m.getName()) + ".name";
        return I18n.hasKey(key) ? I18n.format(key) : m.getName();
    }
}
