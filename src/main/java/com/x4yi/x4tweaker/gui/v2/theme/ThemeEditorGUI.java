package com.x4yi.x4tweaker.gui.v2.theme;

import com.x4yi.x4tweaker.gui.v2.clickgui.ClickGUI;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.io.IOException;

public class ThemeEditorGUI extends GuiScreen {
    private final ClickGUI parent;
    private final ThemeBridge theme;
    private ColorSlotList colorSlotList;
    private HSVColorPicker colorPicker;
    private PreviewControlPanel previewControl;
    private GuiPreviewRenderer previewRenderer;
    private int selectedPreset = -1;
    private String[] presetNames;

    private int panelX, panelY, panelW, panelH;
    private int headerH = 28;
    private int closeBtnX1, closeBtnY1, closeBtnX2, closeBtnY2;
    private int resetAllBtnX1, resetAllBtnY1, resetAllBtnX2, resetAllBtnY2;
    private int saveBtnX1, saveBtnY1, saveBtnX2, saveBtnY2;
    private int presetDropdownX1, presetDropdownY1, presetDropdownX2, presetDropdownY2;
    private boolean presetDropdownOpen = false;

    public ThemeEditorGUI(ClickGUI parent, ThemeBridge theme) {
        this.parent = parent;
        this.theme = theme;
        this.presetNames = ThemePresets.getNames();
    }

    @Override
    public void initGui() {
        panelW = Math.max(600, Math.min(800, (int)(this.width * 0.75f)));
        panelH = Math.max(400, Math.min(500, (int)(this.height * 0.7f)));
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        closeBtnX1 = panelX + panelW - 18;
        closeBtnY1 = panelY + 6;
        closeBtnX2 = closeBtnX1 + 14;
        closeBtnY2 = closeBtnY1 + 16;

        resetAllBtnX1 = panelX + panelW - 160;
        resetAllBtnY1 = panelY + 8;
        resetAllBtnX2 = resetAllBtnX1 + 70;
        resetAllBtnY2 = resetAllBtnY1 + 14;

        saveBtnX1 = panelX + panelW - 84;
        saveBtnY1 = panelY + 8;
        saveBtnX2 = saveBtnX1 + 70;
        saveBtnY2 = saveBtnY1 + 14;

        presetDropdownX1 = panelX + 10;
        presetDropdownY1 = panelY + 8;
        presetDropdownX2 = presetDropdownX1 + 100;
        presetDropdownY2 = presetDropdownY1 + 14;

        int listW = 160;
        int pickerW = 200;
        int contentH = panelH - headerH - 10;

        colorSlotList = new ColorSlotList(theme, new Runnable() {
            @Override
            public void run() {
                int idx = colorSlotList.getSelectedIndex();
                if (idx >= 0) {
                    colorPicker.setColor(theme.getColorByIndex(idx).getRGB());
                }
            }
        });
        colorSlotList.setBounds(panelX + 6, panelY + headerH + 6, listW - 6, contentH - 12);

        colorPicker = new HSVColorPicker(new Runnable() {
            @Override
            public void run() {
                int idx = colorSlotList.getSelectedIndex();
                if (idx >= 0) {
                    theme.setColorByIndex(idx, new Color(colorPicker.getCurrentColor(), true));
                }
            }
        });
        colorPicker.setBounds(panelX + listW + 6, panelY + headerH + 30, pickerW, 140);

        previewControl = new PreviewControlPanel(theme);
        previewControl.setBounds(panelX + listW + pickerW + 12, panelY + headerH + 6, panelW - listW - pickerW - 18, 80);

        previewRenderer = new GuiPreviewRenderer(theme);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        DrawHelper.drawBorderedRect(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, 1.5f, theme.getBorderColor(), theme.getBgColor());
        DrawHelper.drawRect(panelX, panelY, panelX + panelW, panelY + panelH, theme.getBgColor());
        DrawHelper.drawGradientRectH(panelX, panelY, panelX + panelW, panelY + headerH, theme.getAccentDarkColor(), theme.getAccentColor());

        mc.fontRenderer.drawStringWithShadow("Theme Editor", panelX + 10, panelY + 10, 0xFFFFFFFF);

        boolean closeHover = mouseX >= closeBtnX1 && mouseX <= closeBtnX2 && mouseY >= closeBtnY1 && mouseY <= closeBtnY2;
        mc.fontRenderer.drawStringWithShadow("\u2715", closeBtnX1 + 3, closeBtnY1 + 3, closeHover ? 0xFFFF5555 : 0xFFAAAAAA);

        boolean resetHover = mouseX >= resetAllBtnX1 && mouseX <= resetAllBtnX2 && mouseY >= resetAllBtnY1 && mouseY <= resetAllBtnY2;
        DrawHelper.drawRect(resetAllBtnX1, resetAllBtnY1, resetAllBtnX2, resetAllBtnY2, resetHover ? 0xFFAA0000 : 0xFF880000);
        mc.fontRenderer.drawStringWithShadow("Reset All", resetAllBtnX1 + 8, resetAllBtnY1 + 3, 0xFFFFFFFF);

        boolean saveHover = mouseX >= saveBtnX1 && mouseX <= saveBtnX2 && mouseY >= saveBtnY1 && mouseY <= saveBtnY2;
        DrawHelper.drawRect(saveBtnX1, saveBtnY1, saveBtnX2, saveBtnY2, saveHover ? 0xFF00AA00 : 0xFF008800);
        mc.fontRenderer.drawStringWithShadow("Save", saveBtnX1 + 18, saveBtnY1 + 3, 0xFFFFFFFF);

        boolean presetHover = mouseX >= presetDropdownX1 && mouseX <= presetDropdownX2 && mouseY >= presetDropdownY1 && mouseY <= presetDropdownY2;
        DrawHelper.drawRect(presetDropdownX1, presetDropdownY1, presetDropdownX2, presetDropdownY2, presetHover ? 0xFF444444 : 0xFF333333);
        String presetLabel = selectedPreset >= 0 ? presetNames[selectedPreset] : "Presets";
        mc.fontRenderer.drawStringWithShadow(presetLabel + " \u25BC", presetDropdownX1 + 4, presetDropdownY1 + 3, 0xFFFFFFFF);

        if (presetDropdownOpen) {
            int dropY = presetDropdownY2;
            int dropH = presetNames.length * 12;
            DrawHelper.drawRect(presetDropdownX1, dropY, presetDropdownX2, dropY + dropH, 0xFF222222);
            DrawHelper.drawBorderedRect(presetDropdownX1, dropY, presetDropdownX2, dropY + dropH, 1.0f, 0xFF555555, 0x00000000);
            for (int i = 0; i < presetNames.length; i++) {
                int itemY = dropY + i * 12;
                boolean itemHover = mouseX >= presetDropdownX1 && mouseX <= presetDropdownX2 && mouseY >= itemY && mouseY <= itemY + 12;
                if (itemHover) DrawHelper.drawRect(presetDropdownX1, itemY, presetDropdownX2, itemY + 12, 0xFF444444);
                mc.fontRenderer.drawStringWithShadow(presetNames[i], presetDropdownX1 + 4, itemY + 2, i == selectedPreset ? 0xFF66FF66 : 0xFFDDDDDD);
            }
        }

        colorSlotList.render(mouseX, mouseY, partialTicks);

        int idx = colorSlotList.getSelectedIndex();
        if (idx >= 0) {
            colorPicker.setVisible(true);
            colorPicker.render(mouseX, mouseY, partialTicks);
            String slotName = theme.getColorName(idx);
            mc.fontRenderer.drawStringWithShadow("Editing: " + slotName, colorPicker.getX(), colorPicker.getY() - 14, 0xFFCCCCCC);
        } else {
            colorPicker.setVisible(false);
        }

        previewControl.render(mouseX, mouseY, partialTicks);

        int previewAreaX = panelX + 170 + 12;
        int previewAreaY = previewControl.getY() + previewControl.getHeight() + 6;
        int previewAreaH = panelY + panelH - previewAreaY - 6;

        if (previewControl.showClickGUI || previewControl.showChangelog) {
            int count = (previewControl.showClickGUI ? 1 : 0) + (previewControl.showChangelog ? 1 : 0);
            int gap = 8;
            int totalW = panelW - 170 - 18 - gap * (count - 1);
            int singleW = totalW / count;
            int singleH = Math.min(previewAreaH, singleW * 3 / 4);
            int curX = previewAreaX;

            if (previewControl.showClickGUI) {
                DrawHelper.drawBorderedRect(curX, previewAreaY, curX + singleW, previewAreaY + singleH, 1.0f, theme.getSeparatorColor(), 0x00000000);
                mc.fontRenderer.drawStringWithShadow("ClickGUI", curX + 4, previewAreaY - 10, 0xFFAAAAAA);
                previewRenderer.renderClickGUIThumbnail(curX, previewAreaY, singleW, singleH);
                curX += singleW + gap;
            }
            if (previewControl.showChangelog) {
                DrawHelper.drawBorderedRect(curX, previewAreaY, curX + singleW, previewAreaY + singleH, 1.0f, theme.getSeparatorColor(), 0x00000000);
                mc.fontRenderer.drawStringWithShadow("Changelog", curX + 4, previewAreaY - 10, 0xFFAAAAAA);
                previewRenderer.renderChangelogThumbnail(curX, previewAreaY, singleW, singleH);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            if (mouseX >= closeBtnX1 && mouseX <= closeBtnX2 && mouseY >= closeBtnY1 && mouseY <= closeBtnY2) {
                theme.save();
                mc.displayGuiScreen(parent);
                return;
            }
            if (mouseX >= resetAllBtnX1 && mouseX <= resetAllBtnX2 && mouseY >= resetAllBtnY1 && mouseY <= resetAllBtnY2) {
                theme.loadDefaultTheme();
                colorPicker.setColor(theme.getColorByIndex(0).getRGB());
                selectedPreset = -1;
                return;
            }
            if (mouseX >= saveBtnX1 && mouseX <= saveBtnX2 && mouseY >= saveBtnY1 && mouseY <= saveBtnY2) {
                theme.save();
                mc.displayGuiScreen(parent);
                return;
            }
            if (mouseX >= presetDropdownX1 && mouseX <= presetDropdownX2 && mouseY >= presetDropdownY1 && mouseY <= presetDropdownY2) {
                presetDropdownOpen = !presetDropdownOpen;
                return;
            }
            if (presetDropdownOpen) {
                int dropY = presetDropdownY2;
                int clickedIdx = (mouseY - dropY) / 12;
                if (clickedIdx >= 0 && clickedIdx < presetNames.length) {
                    selectedPreset = clickedIdx;
                    ThemePresets.applyPreset(presetNames[clickedIdx], theme);
                    presetDropdownOpen = false;
                    int sidx = colorSlotList.getSelectedIndex();
                    if (sidx >= 0) colorPicker.setColor(theme.getColorByIndex(sidx).getRGB());
                    return;
                } else {
                    presetDropdownOpen = false;
                }
            }
        }

        previewControl.onMouseClick(mouseX, mouseY, mouseButton);
        colorSlotList.onMouseClick(mouseX, mouseY, mouseButton);
        if (colorPicker.isVisible()) colorPicker.onMouseClick(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        colorSlotList.handleMouseWheel(wheel);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (colorPicker.isVisible()) colorPicker.onMouseRelease(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            theme.save();
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void onGuiClosed() {
        theme.save();
    }
}
