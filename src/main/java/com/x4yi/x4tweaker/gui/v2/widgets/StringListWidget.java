package com.x4yi.x4tweaker.gui.v2.widgets;

import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.setting.StringListSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class StringListWidget implements SettingWidget {
    private static final int HEADER_H = 14;
    private static final int INPUT_H = 14;
    private static final int ITEM_H = 14;

    private int x, y, width, height;
    private boolean visible = true;
    private final StringListSetting setting;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private boolean focused = false;
    private String currentInput = "";
    private boolean deleteHovered = false;
    private int deleteIndex = -1;

    public StringListWidget(StringListSetting setting, ThemeBridge theme) {
        this.setting = setting;
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        int totalH = getRequiredHeight();
        int curY = y;

        DrawHelper.drawRect(x, curY, x + width, curY + totalH, 0x00000000);
        mc.fontRenderer.drawStringWithShadow(setting.getName() + ":", x, curY + 3, 0xFFDDDDDD);
        curY += HEADER_H;

        boolean inputHover = mouseX >= x && mouseX <= x + width && mouseY >= curY && mouseY <= curY + INPUT_H;
        DrawHelper.drawBorderedRect(x, curY, x + width, curY + INPUT_H, 1.0f, theme.getSeparatorColor(), theme.getInputFieldColor());
        String displayStr = focused ? currentInput + "_" : "Click to add...";
        mc.fontRenderer.drawStringWithShadow(displayStr, x + 4, curY + 2, 0xFFAAAAAA);
        curY += INPUT_H;

        List<String> items = setting.getValue();
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            mc.fontRenderer.drawStringWithShadow("- " + item, x + 4, curY + 2, 0xFFCCCCCC);
            int delX = x + width - 14;
            deleteHovered = mouseX >= delX && mouseX <= delX + 12 && mouseY >= curY && mouseY <= curY + ITEM_H;
            mc.fontRenderer.drawStringWithShadow("[x]", delX, curY + 2, deleteHovered ? 0xFFFF0000 : 0xFFFF5555);
            curY += ITEM_H;
        }
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || button != 0) return false;
        int curY = y + HEADER_H;
        if (mouseX >= x && mouseX <= x + width && mouseY >= curY && mouseY <= curY + INPUT_H) {
            focused = true;
            currentInput = "";
            return true;
        }
        curY += INPUT_H;
        List<String> items = setting.getValue();
        for (int i = 0; i < items.size(); i++) {
            int delX = x + width - 14;
            if (mouseX >= delX && mouseX <= delX + 12 && mouseY >= curY && mouseY <= curY + ITEM_H) {
                setting.removeString(items.get(i));
                return true;
            }
            curY += ITEM_H;
        }
        focused = false;
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) { return false; }
    @Override
    public boolean onMouseMove(int mouseX, int mouseY, int dx, int dy) { return false; }

    @Override
    public boolean onKey(char typedChar, int keyCode) {
        if (!focused) return false;
        if (keyCode == Keyboard.KEY_ESCAPE) { focused = false; return true; }
        if (keyCode == Keyboard.KEY_RETURN) {
            if (!currentInput.isEmpty()) {
                setting.addString(currentInput);
                currentInput = "";
            }
            focused = false;
            return true;
        }
        if (keyCode == Keyboard.KEY_BACK) {
            if (!currentInput.isEmpty()) currentInput = currentInput.substring(0, currentInput.length() - 1);
            return true;
        }
        if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
            currentInput += typedChar;
            return true;
        }
        return false;
    }

    @Override
    public void update() {}

    @Override
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    @Override
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + getRequiredHeight();
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
    public StringListSetting getSetting() { return setting; }

    @Override
    public int getRequiredHeight() {
        int h = HEADER_H + INPUT_H;
        h += setting.getValue().size() * ITEM_H;
        return h;
    }
    @Override
    public int getPriority() { return 0; }
    @Override
    public void setPriority(int priority) {}
}
