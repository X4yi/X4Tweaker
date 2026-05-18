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
import com.x4yi.x4tweaker.utils.UpdateChecker;
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
    private static final int FOOTER_H = 28;
    private static final int SIDEBAR_W = 100;

    private int windowX, windowY, windowW, windowH;
    private ThemeBridge themeBridge;
    private SidebarPanel sidebar;
    private ContentPanel contentPanel;
    private KeybindsContentPanel keybindsContentPanel;
    private MigrationBanner migrationBanner;

    private int btnCloseX1, btnCloseY1, btnCloseX2, btnCloseY2;
    private int btnPauseX1, btnPauseY1, btnPauseX2, btnPauseY2;

    private int btnBottomChangelogX1, btnBottomChangelogY1, btnBottomChangelogX2, btnBottomChangelogY2;
    private int btnBottomThemeX1, btnBottomThemeY1, btnBottomThemeX2, btnBottomThemeY2;
    private int btnBottomUpdateX1, btnBottomUpdateY1, btnBottomUpdateX2, btnBottomUpdateY2;

    private int lastMouseX = -1;
    private int lastMouseY = -1;

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
        sidebar.setBounds(windowX, windowY + HEADER_H, SIDEBAR_W, windowH - HEADER_H - FOOTER_H);

        contentPanel = new ContentPanel(themeBridge, Category.VISUALS);
        contentPanel.setBounds(windowX + SIDEBAR_W, windowY + HEADER_H, windowW - SIDEBAR_W, windowH - HEADER_H);
        contentPanel.rebuildRows();

        keybindsContentPanel = new KeybindsContentPanel(themeBridge, null);
        keybindsContentPanel.setBounds(windowX + SIDEBAR_W, windowY + HEADER_H, windowW - SIDEBAR_W, windowH - HEADER_H);
        keybindsContentPanel.setVisible(false);

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

        int footerY = windowY + windowH - FOOTER_H;
        btnBottomChangelogX1 = windowX + 10;
        btnBottomChangelogY1 = footerY + 7;
        btnBottomChangelogX2 = btnBottomChangelogX1 + 70;
        btnBottomChangelogY2 = btnBottomChangelogY1 + 14;

        btnBottomThemeX1 = btnBottomChangelogX2 + 6;
        btnBottomThemeY1 = footerY + 7;
        btnBottomThemeX2 = btnBottomThemeX1 + 50;
        btnBottomThemeY2 = btnBottomThemeY1 + 14;

        btnBottomUpdateX1 = windowX + windowW - 100;
        btnBottomUpdateY1 = footerY + 5;
        btnBottomUpdateX2 = btnBottomUpdateX1 + 80;
        btnBottomUpdateY2 = btnBottomUpdateY1 + 18;

        migrationBanner = new MigrationBanner(themeBridge);
        List<String> report = X4TweakerClient.getInstance().getConfigManager().consumeMigrationReport();
        migrationBanner.setNotices(report.isEmpty() ? null : new ArrayList<String>(report));

        int bannerH = migrationBanner.hasNotices() ? 78 : 0;
        int contentTop = windowY + HEADER_H + bannerH;
        int contentH = windowH - HEADER_H - FOOTER_H - bannerH;
        contentPanel.setBounds(windowX + SIDEBAR_W, contentTop, windowW - SIDEBAR_W, contentH);
        keybindsContentPanel.setBounds(windowX + SIDEBAR_W, contentTop, windowW - SIDEBAR_W, contentH);

        if (migrationBanner.hasNotices()) {
            migrationBanner.setBounds(windowX + SIDEBAR_W + 6, contentTop + 6, windowW - SIDEBAR_W - 14, 78);
        }

        UpdateChecker.getInstance().check();

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int dx = mouseX - lastMouseX;
        int dy = mouseY - lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        DrawHelper.drawBorderedRect(windowX - 1, windowY - 1, windowX + windowW + 1, windowY + windowH + 1, 1.5f, themeBridge.getBorderColor(), themeBridge.getBgColor());
        DrawHelper.drawRect(windowX, windowY, windowX + windowW, windowY + windowH, themeBridge.getBgColor());
        DrawHelper.drawGradientRectH(windowX, windowY, windowX + windowW, windowY + HEADER_H, themeBridge.getAccentDarkColor(), themeBridge.getAccentColor());
        DrawHelper.drawGradientRectH(windowX, windowY + windowH - FOOTER_H, windowX + windowW, windowY + windowH, themeBridge.getAccentColor(), themeBridge.getAccentDarkColor());

        mc.fontRenderer.drawStringWithShadow("X4Tweaker", windowX + 10, windowY + 10, 0xFFFFFFFF);
        String ver = X4Tweaker.VERSION;
        int verW = mc.fontRenderer.getStringWidth(ver);
        mc.fontRenderer.drawStringWithShadow(ver, windowX + (windowW - verW) / 2, windowY + 10, 0x88AAAAAA);

        drawHeaderButtons(mouseX, mouseY);
        drawFooterButtons(mouseX, mouseY);

        if (UpdateChecker.getInstance().getState() == UpdateChecker.State.UPDATE_AVAILABLE) {
            boolean updateHover = mouseX >= btnBottomUpdateX1 && mouseX <= btnBottomUpdateX2 && mouseY >= btnBottomUpdateY1 && mouseY <= btnBottomUpdateY2;
            if (updateHover) {
                String latestVer = UpdateChecker.getInstance().getLatestVersion();
                String type = UpdateChecker.getInstance().isPrerelease() ? "Beta" : "Release";
                List<String> tooltip = new ArrayList<String>();
                tooltip.add("Update available: " + latestVer);
                tooltip.add("Type: " + type);
                tooltip.add("Click to open");
                Tooltip.render(tooltip, mouseX, mouseY, this.width, this.height, themeBridge.getBgColor(), themeBridge.getBorderColor());
            }
        }

        sidebar.render(mouseX, mouseY, partialTicks);

        if (sidebar.getSelected() == Category.KEYBINDS) {
            keybindsContentPanel.render(mouseX, mouseY, partialTicks);
        } else {
            contentPanel.onMouseMove(mouseX, mouseY, dx, dy);
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
        boolean closeHover = mouseX >= btnCloseX1 && mouseX <= btnCloseX2 && mouseY >= btnCloseY1 && mouseY <= btnCloseY2;
        mc.fontRenderer.drawStringWithShadow("\u2715", btnCloseX1 + 3, btnCloseY1 + 3, closeHover ? 0xFFFF5555 : 0xFFAAAAAA);

        if (mc.isSingleplayer()) {
            boolean pauseHover = mouseX >= btnPauseX1 && mouseX <= btnPauseX2 && mouseY >= btnPauseY1 && mouseY <= btnPauseY2;
            boolean paused = themeBridge.isEnablePause();
            int pauseBg = paused ? (pauseHover ? 0x6655FF55 : 0x4455FF55) : (pauseHover ? 0x66FFFFFF : 0x22FFFFFF);
            DrawHelper.drawRect(btnPauseX1, btnPauseY1, btnPauseX2, btnPauseY2, pauseBg);
            mc.fontRenderer.drawStringWithShadow(paused ? "\u25B6" : "\u23F8", btnPauseX1 + 14, btnPauseY1 + 2, paused ? 0xFF55FF55 : 0xFFFF5555);
            mc.fontRenderer.drawStringWithShadow("Pause", btnPauseX1 + 4, btnPauseY1 + 2, 0xFFAAAAAA);
        }
    }

    private void drawFooterButtons(int mouseX, int mouseY) {
        boolean changelogHover = mouseX >= btnBottomChangelogX1 && mouseX <= btnBottomChangelogX2 && mouseY >= btnBottomChangelogY1 && mouseY <= btnBottomChangelogY2;
        DrawHelper.drawRect(btnBottomChangelogX1, btnBottomChangelogY1, btnBottomChangelogX2, btnBottomChangelogY2, changelogHover ? 0x44FFFFFF : 0x22000000);
        mc.fontRenderer.drawStringWithShadow("Changelog", btnBottomChangelogX1 + 4, btnBottomChangelogY1 + 3, 0xFFFFFFFF);

        boolean themeHover = mouseX >= btnBottomThemeX1 && mouseX <= btnBottomThemeX2 && mouseY >= btnBottomThemeY1 && mouseY <= btnBottomThemeY2;
        DrawHelper.drawRect(btnBottomThemeX1, btnBottomThemeY1, btnBottomThemeX2, btnBottomThemeY2, themeHover ? 0x44FFFFFF : 0x22000000);
        mc.fontRenderer.drawStringWithShadow("Theme", btnBottomThemeX1 + 4, btnBottomThemeY1 + 3, 0xFFFFFFFF);

        UpdateChecker.State updateState = UpdateChecker.getInstance().getState();
        if (updateState == UpdateChecker.State.UPDATE_AVAILABLE) {
            boolean updateHover = mouseX >= btnBottomUpdateX1 && mouseX <= btnBottomUpdateX2 && mouseY >= btnBottomUpdateY1 && mouseY <= btnBottomUpdateY2;
            String ver = UpdateChecker.getInstance().getLatestVersion();
            boolean isBeta = UpdateChecker.getInstance().isPrerelease();
            int bgColor = isBeta ? (updateHover ? 0xFFDDCC00 : 0xFFCCAA00) : (updateHover ? 0xFF66FF66 : 0xFF55FF55);
            DrawHelper.drawBorderedRect(btnBottomUpdateX1, btnBottomUpdateY1, btnBottomUpdateX2, btnBottomUpdateY2, 1.0f, 0xFF888888, bgColor);
            mc.fontRenderer.drawStringWithShadow("\u2191 " + ver, btnBottomUpdateX1 + 6, btnBottomUpdateY1 + 3, 0xFF000000);
        } else if (updateState == UpdateChecker.State.CHECKING) {
            DrawHelper.drawBorderedRect(btnBottomUpdateX1, btnBottomUpdateY1, btnBottomUpdateX2, btnBottomUpdateY2, 1.0f, 0xFF666666, 0x44888888);
            mc.fontRenderer.drawStringWithShadow("\u27F3 Checking...", btnBottomUpdateX1 + 6, btnBottomUpdateY1 + 3, 0xFFCCCCCC);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            if (mouseX >= btnBottomChangelogX1 && mouseX <= btnBottomChangelogX2 && mouseY >= btnBottomChangelogY1 && mouseY <= btnBottomChangelogY2) {
                mc.displayGuiScreen(new ChangelogScreen(this));
                return;
            }
            if (mouseX >= btnBottomThemeX1 && mouseX <= btnBottomThemeX2 && mouseY >= btnBottomThemeY1 && mouseY <= btnBottomThemeY2) {
                mc.displayGuiScreen(new ThemeEditorGUI(this, themeBridge));
                return;
            }
            if (mouseX >= btnBottomUpdateX1 && mouseX <= btnBottomUpdateX2 && mouseY >= btnBottomUpdateY1 && mouseY <= btnBottomUpdateY2) {
                String url = UpdateChecker.getInstance().getLatestUrl();
                if (url != null) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception ignored) {}
                }
                return;
            }
            if (mc.isSingleplayer() && mouseX >= btnPauseX1 && mouseX <= btnPauseX2 && mouseY >= btnPauseY1 && mouseY <= btnPauseY2) {
                themeBridge.setEnablePause(!themeBridge.isEnablePause());
                themeBridge.save();
                return;
            }
            if (mouseX >= btnCloseX1 && mouseX <= btnCloseX2 && mouseY >= btnCloseY1 && mouseY <= btnCloseY2) {
                mc.displayGuiScreen(null);
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

    public void mouseMoved(int mouseX, int mouseY) {
        int dx = mouseX - lastMouseX;
        int dy = mouseY - lastMouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        if (sidebar.getSelected() == Category.KEYBINDS) {
            keybindsContentPanel.onMouseMove(mouseX, mouseY, dx, dy);
        } else {
            contentPanel.onMouseMove(mouseX, mouseY, dx, dy);
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
