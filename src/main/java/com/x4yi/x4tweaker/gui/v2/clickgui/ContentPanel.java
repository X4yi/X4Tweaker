package com.x4yi.x4tweaker.gui.v2.clickgui;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.gui.v2.framework.ScrollablePanel;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.gui.v2.utils.MathHelper;
import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.*;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class ContentPanel extends ScrollablePanel {
    private static final int PAD = 6;
    private static final int MOD_H = 22;

    private final ThemeBridge theme;
    private final Minecraft mc;
    private Category currentCategory;
    private final List<ModuleRow> rows = new ArrayList<ModuleRow>();
    private ModuleRow hoveredRow = null;

    public ContentPanel(ThemeBridge theme, Category initialCategory) {
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.currentCategory = initialCategory;
    }

    public void setCategory(Category cat) {
        this.currentCategory = cat;
        resetScroll();
        rebuildRows();
    }

    public Category getCurrentCategory() {
        return currentCategory;
    }

    public void rebuildRows() {
        rows.clear();
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModulesByCategory(currentCategory);
        for (Module m : modules) {
            ModuleRow row = new ModuleRow(m, theme);
            if (!row.shouldBeHidden()) {
                rows.add(row);
            }
        }
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

        hoveredRow = null;
        for (ModuleRow row : rows) {
            row.setBounds(contentX, curY, contentW, MOD_H);
            float anim = row.getExpandAnim();
            int settingsH = row.getModule().isImplemented() ? calcSettingsHeight(row.getModule()) : 0;
            int animH = (int)(settingsH * anim);
            int blockBottom = curY + MOD_H + (animH > 0 ? animH + 4 : 0);

            if (blockBottom >= vpTop && curY <= vpBot) {
                row.render(mouseX, mouseY, partialTicks);
                if (row.isHovered()) hoveredRow = row;
            }

            curY += MOD_H + 2;
            totalH += MOD_H + 2;

            if (anim > 0.01f && row.getModule().isImplemented() && !row.getModule().getSettings().isEmpty()) {
                int panelTop = curY;
                int panelBottom = curY + animH;

                DrawHelper.drawBorderedRect(contentX + 4, panelTop, contentX + contentW, panelBottom, 1.0f, theme.getSeparatorColor(), theme.getSettingsPanelColor());

                int setY = curY + 2;
                for (Setting<?> s : row.getModule().getSettings()) {
                    if (!s.isVisible()) continue;
                    int rowH = getSettingHeight(s);
                    int settingBottom = setY + rowH;
                    if (settingBottom > panelTop && setY < panelBottom) {
                        renderSettingInline(s, contentX, setY, contentW, mouseX, mouseY);
                    }
                    setY += rowH + 2;
                    totalH += rowH + 2;
                }

                curY += animH + 4;
                totalH += animH + 4;
            }
        }

        contentHeight = totalH;
        recalcMaxScroll(height);

        endClip();
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible) return false;
        if (!isInsideViewport(mouseX, mouseY)) return false;

        int curY = y + PAD + (int)scrollOffset;
        int contentX = x + PAD;
        int contentW = width - PAD * 2;
        for (ModuleRow row : rows) {
            row.setBounds(contentX, curY, contentW, MOD_H);
            float anim = row.getExpandAnim();
            int settingsH = row.getModule().isImplemented() ? calcSettingsHeight(row.getModule()) : 0;
            int animH = (int)(settingsH * anim);

            if (row.onMouseClick(mouseX, mouseY, button)) {
                X4TweakerClient.getInstance().getConfigManager().save();
                return true;
            }

            curY += MOD_H + 2;
            if (anim > 0.01f && row.getModule().isImplemented() && !row.getModule().getSettings().isEmpty()) {
                int setY = curY + 2;
                for (Setting<?> s : row.getModule().getSettings()) {
                    if (!s.isVisible()) continue;
                    int rowH = getSettingHeight(s);
                    if (handleSettingClick(s, contentX, setY, contentW, mouseX, mouseY, button)) {
                        X4TweakerClient.getInstance().getConfigManager().save();
                        return true;
                    }
                    setY += rowH + 2;
                }
                curY += animH + 4;
            }
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) {
        return super.onMouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean onKey(char typedChar, int keyCode) {
        return super.onKey(typedChar, keyCode);
    }

    public ModuleRow getHoveredRow() { return hoveredRow; }

    private int calcSettingsHeight(Module m) {
        int h = 0;
        for (Setting<?> s : m.getSettings()) {
            if (!s.isVisible()) continue;
            h += getSettingHeight(s) + 2;
        }
        return h;
    }

    private int getSettingHeight(Setting<?> s) {
        if (s instanceof StringListSetting) {
            int h = 14;
            h += 14;
            h += ((StringListSetting) s).getValue().size() * 14;
            return h;
        }
        return 16;
    }

    private void renderSettingInline(Setting<?> s, int sx, int sy, int sw, int mx, int my) {
        int textMaxW = sw - 8;
        if (s instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) s;
            double val = ns.getValue();
            double min = ns.getMin();
            double max = ns.getMax();
            double ratio = (val - min) / (max - min);
            int trackH = 4;
            int trackY = sy + 8;
            DrawHelper.drawBorderedRect(sx, trackY, sx + sw, trackY + trackH, 1.0f, theme.getSeparatorColor(), theme.getSliderTrackColor());
            int fillW = (int)(sw * ratio);
            if (fillW > 0) DrawHelper.drawRoundedRect(sx, trackY, sx + fillW, trackY + trackH, 2, theme.getAccentColor());
            int nubX = sx + fillW;
            DrawHelper.drawCircle(nubX, trackY + trackH / 2, 4, 0xFFFFFFFF);
            DrawHelper.drawCircle(nubX, trackY + trackH / 2, 3, theme.getAccentColor());
            String displayVal = ns.getIncrement() == 1.0 ? String.valueOf((int) val) : String.format("%.2f", val);
            drawWrappedString(s.getName() + ": " + displayVal, sx + 4, sy + 2, textMaxW, 0xFFDDDDDD);
        } else if (s instanceof BooleanSetting) {
            boolean on = ((BooleanSetting) s).getValue();
            drawWrappedString(s.getName(), sx + 4, sy + 4, textMaxW - 30, 0xFFDDDDDD);
            int swX = sx + sw - 26;
            int swBg = on ? theme.getEnabledColor() : theme.getToggleSwitchBgColor();
            DrawHelper.drawBorderedRect(swX, sy + 3, swX + 22, sy + 13, 1.0f, theme.getSeparatorColor(), swBg);
            int handleCenterX = on ? (swX + 22 - 4) : (swX + 4);
            DrawHelper.drawCircle(handleCenterX, sy + 8, 4, 0xFFFFFFFF);
        } else if (s instanceof ModeSetting) {
            String modeVal = String.valueOf(s.getValue());
            int modeW = mc.fontRenderer.getStringWidth(modeVal);
            int centerX = sx + sw / 2;
            drawWrappedString(s.getName() + ":", sx + 4, sy + 4, textMaxW / 2 - 10, 0xFFDDDDDD);
            mc.fontRenderer.drawStringWithShadow("\u25C0", centerX - modeW / 2 - 14, sy + 4, 0xFF888888);
            mc.fontRenderer.drawStringWithShadow(modeVal, centerX - modeW / 2, sy + 4, 0xFFBBBBFF);
            mc.fontRenderer.drawStringWithShadow("\u25B6", centerX + modeW / 2 + 4, sy + 4, 0xFF888888);
        } else if (s instanceof StringListSetting) {
            StringListSetting sl = (StringListSetting) s;
            drawWrappedString(s.getName() + ":", sx + 4, sy + 3, textMaxW, 0xFFDDDDDD);
            int inputY = sy + 14;
            DrawHelper.drawBorderedRect(sx + 4, inputY, sx + sw - 4, inputY + 12, 1.0f, theme.getSeparatorColor(), theme.getInputFieldColor());
            mc.fontRenderer.drawStringWithShadow("Click to add...", sx + 8, inputY + 2, 0xFFAAAAAA);
            int itemY = inputY + 14;
            for (String item : sl.getValue()) {
                drawWrappedString("- " + item, sx + 8, itemY + 2, textMaxW - 24, 0xFFCCCCCC);
                mc.fontRenderer.drawStringWithShadow("[x]", sx + sw - 20, itemY + 2, 0xFFFF5555);
                itemY += 14;
            }
        } else if (s instanceof ColorSetting) {
            int color = ((ColorSetting) s).getValue().getRGB();
            drawWrappedString(s.getName(), sx + 4, sy + 4, textMaxW - 16, 0xFFDDDDDD);
            int previewX = sx + sw - 16;
            DrawHelper.drawBorderedRect(previewX, sy + 3, previewX + 12, sy + 13, 1.0f, theme.getSeparatorColor(), color);
        } else {
            drawWrappedString(s.getName() + ": " + s.getValue(), sx + 4, sy + 3, textMaxW, 0xFFDDDDDD);
        }
    }

    private void drawWrappedString(String text, int x, int y, int maxWidth, int color) {
        if (maxWidth <= 0) return;
        net.minecraft.client.gui.FontRenderer fr = mc.fontRenderer;
        if (fr.getStringWidth(text) <= maxWidth) {
            fr.drawStringWithShadow(text, x, y, color);
            return;
        }
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            String test = line.length() > 0 ? line + " " + word : word;
            if (fr.getStringWidth(test) > maxWidth && line.length() > 0) {
                fr.drawStringWithShadow(line.toString(), x, curY, color);
                line = new StringBuilder(word);
                curY += fr.FONT_HEIGHT + 2;
            } else {
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            fr.drawStringWithShadow(line.toString(), x, curY, color);
        }
    }

    private boolean handleSettingClick(Setting<?> s, int sx, int sy, int sw, int mx, int my, int button) {
        if (!(mx >= sx && mx <= sx + sw && my >= sy && my <= sy + getSettingHeight(s))) return false;
        if (s instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) s;
            double ratio = MathHelper.clamp((double)(mx - sx) / (double)sw, 0.0, 1.0);
            double newVal = ns.getMin() + ratio * (ns.getMax() - ns.getMin());
            double stepped = Math.round(newVal / ns.getIncrement()) * ns.getIncrement();
            ns.setValue(MathHelper.clamp(stepped, ns.getMin(), ns.getMax()));
            return true;
        }
        if (s instanceof BooleanSetting) {
            ((BooleanSetting) s).toggle();
            return true;
        }
        if (s instanceof ModeSetting) {
            ModeSetting ms = (ModeSetting) s;
            if (button == 0) ms.cycle(); else ms.cycleBack();
            return true;
        }
        return false;
    }
}
