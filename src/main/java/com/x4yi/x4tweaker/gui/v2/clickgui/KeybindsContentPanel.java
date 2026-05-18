package com.x4yi.x4tweaker.gui.v2.clickgui;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.gui.v2.framework.ScrollablePanel;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class KeybindsContentPanel extends ScrollablePanel {
    private static final int PAD = 6;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ITEM_HEIGHT = 18;

    private final ThemeBridge theme;
    private final Minecraft mc;
    private KeybindOverlay keybindOverlay;
    private final Runnable onOpenOverlay;

    public KeybindsContentPanel(ThemeBridge theme, Runnable onOpenOverlay) {
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.onOpenOverlay = onOpenOverlay;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        updateScroll();

        DrawHelper.drawRect(x, y, x + width, y + height, theme.getContentBgColor());

        beginClip();

        int curY = y + PAD + (int)scrollOffset;
        int vpTop = y;
        int vpBot = y + height;
        int contentX = x + PAD;
        int contentW = width - PAD * 2;
        int totalH = PAD * 2;

        boolean addHover = mouseX >= contentX && mouseX <= contentX + contentW && mouseY >= curY && mouseY <= curY + BUTTON_HEIGHT;
        if (addHover && mouseY >= vpTop && mouseY <= vpBot) {
            DrawHelper.drawBorderedRect(contentX, curY, contentX + contentW, curY + BUTTON_HEIGHT, 1.0f, theme.getSeparatorColor(), theme.getEnabledColor());
        } else {
            DrawHelper.drawBorderedRect(contentX, curY, contentX + contentW, curY + BUTTON_HEIGHT, 1.0f, theme.getSeparatorColor(), theme.getEnabledDarkColor());
        }
        mc.fontRenderer.drawStringWithShadow("[+] Add Keybind", contentX + 8, curY + 6, 0xFFFFFFFF);
        curY += BUTTON_HEIGHT + 6;
        totalH += BUTTON_HEIGHT + 6;

        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (Module m : modules) {
            if (m.getKeybind() == Keyboard.KEY_NONE) continue;
            if (curY + ITEM_HEIGHT >= vpTop && curY <= vpBot) {
                DrawHelper.drawBorderedRect(contentX, curY, contentX + contentW, curY + ITEM_HEIGHT, 1.0f, theme.getSeparatorColor(), theme.getSurfaceColor());

                String name = getDisplayName(m);
                mc.fontRenderer.drawStringWithShadow(name, contentX + 5, curY + 5, 0xFFFFFFFF);

                String keyName = "[" + Keyboard.getKeyName(m.getKeybind()) + "]";
                int keyWidth = mc.fontRenderer.getStringWidth(keyName);
                mc.fontRenderer.drawStringWithShadow(keyName, contentX + contentW - 25 - keyWidth, curY + 5, 0xFF888888);

                boolean delHover = mouseX >= contentX + contentW - 20 && mouseX <= contentX + contentW - 5 && mouseY >= curY + 4 && mouseY <= curY + 14;
                mc.fontRenderer.drawStringWithShadow("[x]", contentX + contentW - 20, curY + 5, delHover ? 0xFFFF0000 : 0xFFFF5555);
            }
            curY += ITEM_HEIGHT + 2;
            totalH += ITEM_HEIGHT + 2;
        }

        contentHeight = totalH;
        recalcMaxScroll(height);

        endClip();

        if (keybindOverlay != null) {
            keybindOverlay.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible) return false;
        if (keybindOverlay != null) {
            return keybindOverlay.onMouseClick(mouseX, mouseY, button);
        }
        if (!isInsideViewport(mouseX, mouseY)) return false;

        int curY = y + PAD + (int)scrollOffset;
        int contentX = x + PAD;
        int contentW = width - PAD * 2;

        if (button == 0 && mouseX >= contentX && mouseX <= contentX + contentW && mouseY >= curY && mouseY <= curY + BUTTON_HEIGHT) {
            keybindOverlay = new KeybindOverlay(theme, new Runnable() {
                @Override
                public void run() { keybindOverlay = null; }
            });
            return true;
        }

        curY += BUTTON_HEIGHT + 6;
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (Module m : modules) {
            if (m.getKeybind() == Keyboard.KEY_NONE) continue;
            if (mouseX >= contentX + contentW - 20 && mouseX <= contentX + contentW - 5 && mouseY >= curY + 4 && mouseY <= curY + 14) {
                m.setKeybind(Keyboard.KEY_NONE);
                X4TweakerClient.getInstance().getConfigManager().save();
                return true;
            }
            curY += ITEM_HEIGHT + 2;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) {
        if (keybindOverlay != null) return keybindOverlay.onMouseRelease(mouseX, mouseY, button);
        return super.onMouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean onKey(char typedChar, int keyCode) {
        if (keybindOverlay != null) return keybindOverlay.onKey(typedChar, keyCode);
        return super.onKey(typedChar, keyCode);
    }

    public void handleMouseWheel(int dWheel) {
        if (keybindOverlay != null) {
            keybindOverlay.handleMouseWheel(dWheel);
            return;
        }
        super.handleMouseWheel(dWheel);
    }

    private String getDisplayName(Module m) {
        String key = "x4tweaker.module." + com.x4yi.x4tweaker.utils.I18nUtils.normalizeKey(m.getName()) + ".name";
        return I18n.hasKey(key) ? I18n.format(key) : m.getName();
    }
}
