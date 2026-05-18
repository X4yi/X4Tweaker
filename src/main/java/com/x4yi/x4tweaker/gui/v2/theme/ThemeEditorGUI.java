package com.x4yi.x4tweaker.gui.v2.theme;

import com.x4yi.x4tweaker.gui.v2.clickgui.ClickGUI;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.gui.v2.utils.GuiScaler;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.io.IOException;

public class ThemeEditorGUI extends GuiScreen {
    private final ClickGUI parentScreen;
    private final ThemeBridge theme;
    private ColorSlotList colorPalette;
    private HSVColorPicker hsvPicker;
    private PreviewControlPanel previewControls;
    private GuiPreviewRenderer guiPreviewRenderer;
    private int selectedPreset = -1;
    private String[] presetNames;

    private int windowX, windowY, windowW, windowH;
    private int headerHeight = 28;
    private int btnCloseX1, btnCloseY1, btnCloseX2, btnCloseY2;
    private int btnResetX1, btnResetY1, btnResetX2, btnResetY2;
    private int btnSaveX1, btnSaveY1, btnSaveX2, btnSaveY2;
    private int btnPresetX1, btnPresetY1, btnPresetX2, btnPresetY2;
    private boolean presetDropdownOpen = false;

    private int previewAreaX, previewAreaY, previewAreaW, previewAreaH;

    public ThemeEditorGUI(ClickGUI parentScreen, ThemeBridge theme) {
        this.parentScreen = parentScreen;
        this.theme = theme;
        this.presetNames = ThemePresets.getNames();
    }

    @Override
    public void initGui() {
        int screenW = GuiScaler.getScreenWidth();
        int screenH = GuiScaler.getScreenHeight();
        windowW = GuiScaler.clampWidth((int)(screenW * 0.75f), 600, 800);
        windowH = GuiScaler.clampHeight((int)(screenH * 0.7f), 400, 500);
        windowX = (screenW - windowW) / 2;
        windowY = (screenH - windowH) / 2;

        btnCloseX1 = windowX + windowW - 18;
        btnCloseY1 = windowY + 6;
        btnCloseX2 = btnCloseX1 + 14;
        btnCloseY2 = btnCloseY1 + 16;

        btnResetX1 = windowX + windowW - 160;
        btnResetY1 = windowY + 8;
        btnResetX2 = btnResetX1 + 70;
        btnResetY2 = btnResetY1 + 14;

        btnSaveX1 = windowX + windowW - 84;
        btnSaveY1 = windowY + 8;
        btnSaveX2 = btnSaveX1 + 70;
        btnSaveY2 = btnSaveY1 + 14;

        btnPresetX1 = windowX + 10;
        btnPresetY1 = windowY + 8;
        btnPresetX2 = btnPresetX1 + 100;
        btnPresetY2 = btnPresetY1 + 14;

        int paletteW = 160;
        int pickerW = hsvPickerWidth();
        int rightPanelW = windowW - paletteW - pickerW - 24;
        int contentH = windowH - headerHeight - 10;

        colorPalette = new ColorSlotList(theme, new Runnable() {
            @Override
            public void run() {
                int idx = colorPalette.getSelectedIndex();
                if (idx >= 0) {
                    hsvPicker.setColor(theme.getColorByIndex(idx).getRGB());
                }
            }
        });
        colorPalette.setBounds(windowX + 6, windowY + headerHeight + 6, paletteW - 6, contentH - 12);

        hsvPicker = new HSVColorPicker(new Runnable() {
            @Override
            public void run() {
                int idx = colorPalette.getSelectedIndex();
                if (idx >= 0) {
                    theme.setColorByIndex(idx, new Color(hsvPicker.getCurrentColor(), true));
                }
            }
        });
        hsvPicker.setBounds(windowX + paletteW + 6, windowY + headerHeight + 30, pickerW, 140);

        previewControls = new PreviewControlPanel(theme);
        previewControls.setBounds(windowX + paletteW + pickerW + 12, windowY + headerHeight + 6, rightPanelW - 6, 80);

        guiPreviewRenderer = new GuiPreviewRenderer(theme);

        previewAreaX = windowX + paletteW + pickerW + 12;
        previewAreaY = windowY + windowH - 160;
        previewAreaW = rightPanelW - 6;
        previewAreaH = 150;
    }

    private int hsvPickerWidth() {
        return 210;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        DrawHelper.drawBorderedRect(windowX - 1, windowY - 1, windowX + windowW + 1, windowY + windowH + 1, 1.5f, theme.getBorderColor(), theme.getBgColor());
        DrawHelper.drawRect(windowX, windowY, windowX + windowW, windowY + windowH, theme.getBgColor());
        DrawHelper.drawGradientRectH(windowX, windowY, windowX + windowW, windowY + headerHeight, theme.getAccentDarkColor(), theme.getAccentColor());

        mc.fontRenderer.drawStringWithShadow("Theme Editor", windowX + 10, windowY + 10, 0xFFFFFFFF);

        boolean closeHover = mouseX >= btnCloseX1 && mouseX <= btnCloseX2 && mouseY >= btnCloseY1 && mouseY <= btnCloseY2;
        mc.fontRenderer.drawStringWithShadow("\u2715", btnCloseX1 + 3, btnCloseY1 + 3, closeHover ? 0xFFFF5555 : 0xFFAAAAAA);

        boolean resetHover = mouseX >= btnResetX1 && mouseX <= btnResetX2 && mouseY >= btnResetY1 && mouseY <= btnResetY2;
        DrawHelper.drawRect(btnResetX1, btnResetY1, btnResetX2, btnResetY2, resetHover ? 0xFFAA0000 : 0xFF880000);
        mc.fontRenderer.drawStringWithShadow("Reset All", btnResetX1 + 8, btnResetY1 + 3, 0xFFFFFFFF);

        boolean saveHover = mouseX >= btnSaveX1 && mouseX <= btnSaveX2 && mouseY >= btnSaveY1 && mouseY <= btnSaveY2;
        DrawHelper.drawRect(btnSaveX1, btnSaveY1, btnSaveX2, btnSaveY2, saveHover ? 0xFF00AA00 : 0xFF008800);
        mc.fontRenderer.drawStringWithShadow("Save", btnSaveX1 + 18, btnSaveY1 + 3, 0xFFFFFFFF);

        boolean presetHover = mouseX >= btnPresetX1 && mouseX <= btnPresetX2 && mouseY >= btnPresetY1 && mouseY <= btnPresetY2;
        DrawHelper.drawRect(btnPresetX1, btnPresetY1, btnPresetX2, btnPresetY2, presetHover ? 0xFF444444 : 0xFF333333);
        String presetLabel = selectedPreset >= 0 ? presetNames[selectedPreset] : "Presets";
        mc.fontRenderer.drawStringWithShadow(presetLabel + " \u25BC", btnPresetX1 + 4, btnPresetY1 + 3, 0xFFFFFFFF);

        if (presetDropdownOpen) {
            int dropY = btnPresetY2;
            int dropH = presetNames.length * 12;
            DrawHelper.drawRect(btnPresetX1, dropY, btnPresetX2, dropY + dropH, 0xFF222222);
            DrawHelper.drawBorderedRect(btnPresetX1, dropY, btnPresetX2, dropY + dropH, 1.0f, 0xFF555555, 0x00000000);
            for (int i = 0; i < presetNames.length; i++) {
                int itemY = dropY + i * 12;
                boolean itemHover = mouseX >= btnPresetX1 && mouseX <= btnPresetX2 && mouseY >= itemY && mouseY <= itemY + 12;
                if (itemHover) DrawHelper.drawRect(btnPresetX1, itemY, btnPresetX2, itemY + 12, 0xFF444444);
                mc.fontRenderer.drawStringWithShadow(presetNames[i], btnPresetX1 + 4, itemY + 2, i == selectedPreset ? 0xFF66FF66 : 0xFFDDDDDD);
            }
        }

        colorPalette.render(mouseX, mouseY, partialTicks);

        int idx = colorPalette.getSelectedIndex();
        if (idx >= 0) {
            hsvPicker.setVisible(true);
            hsvPicker.render(mouseX, mouseY, partialTicks);
            String slotName = theme.getColorName(idx);
            mc.fontRenderer.drawStringWithShadow("Editing: " + slotName, hsvPicker.getX(), hsvPicker.getY() - 14, 0xFFCCCCCC);
        } else {
            hsvPicker.setVisible(false);
        }

        previewControls.render(mouseX, mouseY, partialTicks);

        DrawHelper.drawBorderedRect(previewAreaX, previewAreaY, previewAreaX + previewAreaW, previewAreaY + previewAreaH, 1.0f, theme.getSeparatorColor(), theme.getContentBgColor());
        mc.fontRenderer.drawStringWithShadow("Live Preview", previewAreaX + 4, previewAreaY - 10, 0xFFAAAAAA);

        if (previewControls.showClickGUIPreview || previewControls.showChangelogPreview) {
            int count = (previewControls.showClickGUIPreview ? 1 : 0) + (previewControls.showChangelogPreview ? 1 : 0);
            int gap = 8;
            int pad = 6;
            int availW = previewAreaW - pad * 2 - gap * (count - 1);
            int singleW = availW / count;
            int singleH = previewAreaH - pad * 2;
            int curX = previewAreaX + pad;
            int curY = previewAreaY + pad;

            if (previewControls.showClickGUIPreview) {
                guiPreviewRenderer.renderClickGUIThumbnail(curX, curY, singleW, singleH);
                curX += singleW + gap;
            }
            if (previewControls.showChangelogPreview) {
                guiPreviewRenderer.renderChangelogThumbnail(curX, curY, singleW, singleH);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            if (mouseX >= btnCloseX1 && mouseX <= btnCloseX2 && mouseY >= btnCloseY1 && mouseY <= btnCloseY2) {
                theme.save();
                mc.displayGuiScreen(parentScreen);
                return;
            }
            if (mouseX >= btnResetX1 && mouseX <= btnResetX2 && mouseY >= btnResetY1 && mouseY <= btnResetY2) {
                theme.loadDefaultTheme();
                hsvPicker.setColor(theme.getColorByIndex(0).getRGB());
                selectedPreset = -1;
                return;
            }
            if (mouseX >= btnSaveX1 && mouseX <= btnSaveX2 && mouseY >= btnSaveY1 && mouseY <= btnSaveY2) {
                theme.save();
                mc.displayGuiScreen(parentScreen);
                return;
            }
            if (mouseX >= btnPresetX1 && mouseX <= btnPresetX2 && mouseY >= btnPresetY1 && mouseY <= btnPresetY2) {
                presetDropdownOpen = !presetDropdownOpen;
                return;
            }
            if (presetDropdownOpen) {
                int dropY = btnPresetY2;
                int clickedIdx = (mouseY - dropY) / 12;
                if (clickedIdx >= 0 && clickedIdx < presetNames.length) {
                    selectedPreset = clickedIdx;
                    ThemePresets.applyPreset(presetNames[clickedIdx], theme);
                    presetDropdownOpen = false;
                    int sidx = colorPalette.getSelectedIndex();
                    if (sidx >= 0) hsvPicker.setColor(theme.getColorByIndex(sidx).getRGB());
                    return;
                } else {
                    presetDropdownOpen = false;
                }
            }
        }

        previewControls.onMouseClick(mouseX, mouseY, mouseButton);
        colorPalette.onMouseClick(mouseX, mouseY, mouseButton);
        if (hsvPicker.isVisible()) hsvPicker.onMouseClick(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        colorPalette.handleMouseWheel(wheel);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (hsvPicker.isVisible()) hsvPicker.onMouseRelease(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            theme.save();
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void onGuiClosed() {
        theme.save();
    }
}
