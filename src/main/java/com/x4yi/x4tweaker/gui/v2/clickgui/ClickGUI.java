package com.x4yi.x4tweaker.gui.v2.clickgui;

import com.x4yi.x4tweaker.X4Tweaker;
import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.gui.v2.changelog.ChangelogScreen;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.framework.Tooltip;
import com.x4yi.x4tweaker.gui.v2.theme.ThemeEditorGUI;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.manager.ThemeManager;
import com.x4yi.x4tweaker.module.Category;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends GuiScreen {
    private static final int MIN_W = 400;
    private static final int MAX_W = 600;
    private static final int MIN_H = 260;
    private static final int MAX_H = 420;
    private static final int HEADER_H = 28;
    private static final int SIDEBAR_W = 100;

    private int windowX, windowY, windowW, windowH;
    private ThemeBridge themeBridge;
    private SidebarPanel sidebar;
    private ContentPanel contentPanel;
    private KeybindsContentPanel keybindsContentPanel;
    private MigrationBanner migrationBanner;

    private int btnChangelogX1, btnChangelogY1, btnChangelogX2, btnChangelogY2;
    private int btnThemeX1, btnThemeY1, btnThemeX2, btnThemeY2;
    private int btnCloseX1, btnCloseY1, btnCloseX2, btnCloseY2;
    private int btnPauseX1, btnPauseY1, btnPauseX2, btnPauseY2;

    @Override
    public boolean doesGuiPauseGame() {
        return themeBridge.isEnablePause() && mc.isSingleplayer();
    }

    @Override
    public void initGui() {
        ThemeManager tm = X4TweakerClient.getInstance().getThemeManager();
        themeBridge = new ThemeBridge(tm);

        windowW = Math.max(MIN_W, Math.min(MAX_W, (int)(this.width * 0.65f)));
        windowH = Math.max(MIN_H, Math.min(MAX_H, (int)(this.height * 0.6f)));
        windowX = (this.width - windowW) / 2;
        windowY = (this.height - windowH) / 2;

        sidebar = new SidebarPanel(themeBridge, Category.VISUALS, new SidebarPanel.CategoryCallback() {
            @Override
            public void onCategorySelected(Category cat) {
                if (cat == Category.KEYBINDS) {
                    contentPanel.setVisible(false);
                    keybindsContentPanel.setVisible(true);
                } else {
                    contentPanel.setVisible(true);
                    keybindsContentPanel.setVisible(false);
                    contentPanel.setCategory(cat);
                }
                migrationBanner.setVisible(false);
            }
        });
        sidebar.setBounds(windowX, windowY + HEADER_H, SIDEBAR_W, windowH - HEADER_H);

        contentPanel = new ContentPanel(themeBridge, Category.VISUALS);
        contentPanel.setBounds(windowX + SIDEBAR_W, windowY + HEADER_H, windowW - SIDEBAR_W, windowH - HEADER_H);
        contentPanel.rebuildRows();

        keybindsContentPanel = new KeybindsContentPanel(themeBridge, null);
        keybindsContentPanel.setBounds(windowX + SIDEBAR_W, windowY + HEADER_H, windowW - SIDEBAR_W, windowH - HEADER_H);
        keybindsContentPanel.setVisible(false);

        btnChangelogX1 = windowX + windowW - 180;
        btnChangelogY1 = windowY + 7;
        btnChangelogX2 = btnChangelogX1 + 70;
        btnChangelogY2 = btnChangelogY1 + 14;

        btnThemeX1 = btnChangelogX2 + 6;
        btnThemeY1 = windowY + 7;
        btnThemeX2 = btnThemeX1 + 50;
        btnThemeY2 = btnThemeY1 + 14;

        btnCloseX1 = windowX + windowW - 18;
        btnCloseY1 = windowY + 6;
        btnCloseX2 = btnCloseX1 + 14;
        btnCloseY2 = btnCloseY1 + 16;

        if (mc.isSingleplayer()) {
            btnPauseX1 = windowX + windowW - 70;
            btnPauseY1 = windowY + 7;
            btnPauseX2 = btnPauseX1 + 44;
            btnPauseY2 = btnPauseY1 + 14;
        }

        migrationBanner = new MigrationBanner(themeBridge);
        List<String> report = X4TweakerClient.getInstance().getConfigManager().consumeMigrationReport();
        migrationBanner.setNotices(report.isEmpty() ? null : new ArrayList<String>(report));

        int bannerH = migrationBanner.hasNotices() ? 78 : 0;
        int contentTop = windowY + HEADER_H + bannerH;
        int contentH = windowH - HEADER_H - bannerH;
        contentPanel.setBounds(windowX + SIDEBAR_W, contentTop, windowW - SIDEBAR_W, contentH);
        keybindsContentPanel.setBounds(windowX + SIDEBAR_W, contentTop, windowW - SIDEBAR_W, contentH);

        if (migrationBanner.hasNotices()) {
            migrationBanner.setBounds(windowX + SIDEBAR_W + 6, contentTop + 6, windowW - SIDEBAR_W - 14, 78);
        }

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        DrawHelper.drawBorderedRect(windowX - 1, windowY - 1, windowX + windowW + 1, windowY + windowH + 1, 1.5f, themeBridge.getBorderColor(), themeBridge.getBgColor());
        DrawHelper.drawRect(windowX, windowY, windowX + windowW, windowY + windowH, themeBridge.getBgColor());
        DrawHelper.drawGradientRectH(windowX, windowY, windowX + windowW, windowY + HEADER_H, themeBridge.getAccentDarkColor(), themeBridge.getAccentColor());

        mc.fontRenderer.drawStringWithShadow("X4Tweaker", windowX + 10, windowY + 10, 0xFFFFFFFF);
        String ver = X4Tweaker.VERSION;
        int verW = mc.fontRenderer.getStringWidth(ver);
        mc.fontRenderer.drawStringWithShadow(ver, windowX + (windowW - verW) / 2, windowY + 10, 0x88AAAAAA);

        drawHeaderButtons(mouseX, mouseY);
        sidebar.render(mouseX, mouseY, partialTicks);

        if (sidebar.getSelected() == Category.KEYBINDS) {
            keybindsContentPanel.render(mouseX, mouseY, partialTicks);
        } else {
            contentPanel.render(mouseX, mouseY, partialTicks);
        }

        if (migrationBanner.hasNotices()) {
            migrationBanner.render(mouseX, mouseY, partialTicks);
        }

        if (sidebar.getSelected() != Category.KEYBINDS) {
            ModuleRow hovered = contentPanel.getHoveredRow();
            if (hovered != null) {
                List<String> lines = hovered.getTooltipLines();
                if (!lines.isEmpty()) {
                    Tooltip.render(lines, mouseX, mouseY, this.width, this.height, themeBridge.getBgColor(), themeBridge.getBorderColor());
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHeaderButtons(int mouseX, int mouseY) {
        boolean changelogHover = mouseX >= btnChangelogX1 && mouseX <= btnChangelogX2 && mouseY >= btnChangelogY1 && mouseY <= btnChangelogY2;
        DrawHelper.drawRect(btnChangelogX1, btnChangelogY1, btnChangelogX2, btnChangelogY2, changelogHover ? 0x44FFFFFF : 0x22000000);
        mc.fontRenderer.drawStringWithShadow("Changelog", btnChangelogX1 + 4, btnChangelogY1 + 3, 0xFFFFFFFF);

        boolean themeHover = mouseX >= btnThemeX1 && mouseX <= btnThemeX2 && mouseY >= btnThemeY1 && mouseY <= btnThemeY2;
        DrawHelper.drawRect(btnThemeX1, btnThemeY1, btnThemeX2, btnThemeY2, themeHover ? 0x44FFFFFF : 0x22000000);
        mc.fontRenderer.drawStringWithShadow("Theme", btnThemeX1 + 4, btnThemeY1 + 3, 0xFFFFFFFF);

        boolean closeHover = mouseX >= btnCloseX1 && mouseX <= btnCloseX2 && mouseY >= btnCloseY1 && mouseY <= btnCloseY2;
        mc.fontRenderer.drawStringWithShadow("\u2715", btnCloseX1 + 3, btnCloseY1 + 3, closeHover ? 0xFFFF5555 : 0xFFAAAAAA);

        if (mc.isSingleplayer()) {
            boolean pauseHover = mouseX >= btnPauseX1 && mouseX <= btnPauseX2 && mouseY >= btnPauseY1 && mouseY <= btnPauseY2;
            DrawHelper.drawRect(btnPauseX1, btnPauseY1, btnPauseX2, btnPauseY2, pauseHover ? 0x44FFFFFF : 0x22FFFFFF);
            boolean paused = themeBridge.isEnablePause();
            mc.fontRenderer.drawStringWithShadow("P:" + (paused ? "ON" : "OFF"), btnPauseX1 + 4, btnPauseY1 + 3, paused ? 0xFF55FF55 : 0xFFFF5555);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            if (mouseX >= btnChangelogX1 && mouseX <= btnChangelogX2 && mouseY >= btnChangelogY1 && mouseY <= btnChangelogY2) {
                mc.displayGuiScreen(new ChangelogScreen(this));
                return;
            }
            if (mouseX >= btnThemeX1 && mouseX <= btnThemeX2 && mouseY >= btnThemeY1 && mouseY <= btnThemeY2) {
                mc.displayGuiScreen(new ThemeEditorGUI(this, themeBridge));
                return;
            }
            if (mouseX >= btnCloseX1 && mouseX <= btnCloseX2 && mouseY >= btnCloseY1 && mouseY <= btnCloseY2) {
                mc.displayGuiScreen(null);
                return;
            }
            if (mc.isSingleplayer() && mouseX >= btnPauseX1 && mouseX <= btnPauseX2 && mouseY >= btnPauseY1 && mouseY <= btnPauseY2) {
                themeBridge.setEnablePause(!themeBridge.isEnablePause());
                themeBridge.save();
                return;
            }
        }

        if (migrationBanner.hasNotices() && migrationBanner.onMouseClick(mouseX, mouseY, mouseButton)) return;
        if (sidebar.onMouseClick(mouseX, mouseY, mouseButton)) return;

        if (sidebar.getSelected() == Category.KEYBINDS) {
            keybindsContentPanel.onMouseClick(mouseX, mouseY, mouseButton);
        } else {
            contentPanel.onMouseClick(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (sidebar.getSelected() == Category.KEYBINDS) {
            keybindsContentPanel.onMouseRelease(mouseX, mouseY, state);
        } else {
            contentPanel.onMouseRelease(mouseX, mouseY, state);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        if (migrationBanner.hasNotices() && migrationBanner.contains(Mouse.getX() * this.width / mc.displayWidth, this.height - Mouse.getY() * this.height / mc.displayHeight - 1)) {
            migrationBanner.handleWheel(wheel);
            return;
        }
        if (sidebar.getSelected() == Category.KEYBINDS) {
            keybindsContentPanel.handleMouseWheel(wheel);
        } else {
            contentPanel.handleMouseWheel(wheel);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (sidebar.getSelected() == Category.KEYBINDS) {
            if (keybindsContentPanel.onKey(typedChar, keyCode)) return;
        } else {
            if (contentPanel.onKey(typedChar, keyCode)) return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        X4TweakerClient.getInstance().getConfigManager().save();
        themeBridge.save();
    }
}
