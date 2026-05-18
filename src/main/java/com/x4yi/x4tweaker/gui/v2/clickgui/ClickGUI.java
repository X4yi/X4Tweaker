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

    private int panelX, panelY, panelW, panelH;
    private ThemeBridge themeBridge;
    private SidebarPanel sidebar;
    private ContentPanel content;
    private KeybindOverlay keybindOverlay;
    private MigrationBanner migrationBanner;

    private int changelogBtnX1, changelogBtnY1, changelogBtnX2, changelogBtnY2;
    private int themeBtnX1, themeBtnY1, themeBtnX2, themeBtnY2;
    private int closeBtnX1, closeBtnY1, closeBtnX2, closeBtnY2;
    private int pauseBtnX1, pauseBtnY1, pauseBtnX2, pauseBtnY2;
    private int keybindBtnX1, keybindBtnY1, keybindBtnX2, keybindBtnY2;

    @Override
    public boolean doesGuiPauseGame() {
        return themeBridge.isEnablePause() && mc.isSingleplayer();
    }

    @Override
    public void initGui() {
        ThemeManager tm = X4TweakerClient.getInstance().getThemeManager();
        themeBridge = new ThemeBridge(tm);

        panelW = Math.max(MIN_W, Math.min(MAX_W, (int)(this.width * 0.65f)));
        panelH = Math.max(MIN_H, Math.min(MAX_H, (int)(this.height * 0.6f)));
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        sidebar = new SidebarPanel(themeBridge, Category.VISUALS, new SidebarPanel.CategoryCallback() {
            @Override
            public void onCategorySelected(Category cat) {
                content.setCategory(cat);
                migrationBanner.setVisible(false);
            }
        });
        sidebar.setBounds(panelX, panelY + HEADER_H, SIDEBAR_W, panelH - HEADER_H);

        content = new ContentPanel(themeBridge, Category.VISUALS);
        content.setBounds(panelX + SIDEBAR_W, panelY + HEADER_H, panelW - SIDEBAR_W, panelH - HEADER_H);
        content.rebuildRows();

        changelogBtnX1 = panelX + panelW - 180;
        changelogBtnY1 = panelY + 7;
        changelogBtnX2 = changelogBtnX1 + 70;
        changelogBtnY2 = changelogBtnY1 + 14;

        themeBtnX1 = changelogBtnX2 + 6;
        themeBtnY1 = panelY + 7;
        themeBtnX2 = themeBtnX1 + 50;
        themeBtnY2 = themeBtnY1 + 14;

        keybindBtnX1 = panelX + SIDEBAR_W + 6;
        keybindBtnY1 = panelY + panelH - 22;
        keybindBtnX2 = keybindBtnX1 + 80;
        keybindBtnY2 = keybindBtnY1 + 16;

        closeBtnX1 = panelX + panelW - 18;
        closeBtnY1 = panelY + 6;
        closeBtnX2 = closeBtnX1 + 14;
        closeBtnY2 = closeBtnY1 + 16;

        if (mc.isSingleplayer()) {
            pauseBtnX1 = panelX + panelW - 70;
            pauseBtnY1 = panelY + 7;
            pauseBtnX2 = pauseBtnX1 + 44;
            pauseBtnY2 = pauseBtnY1 + 14;
        }

        migrationBanner = new MigrationBanner(themeBridge);
        List<String> report = X4TweakerClient.getInstance().getConfigManager().consumeMigrationReport();
        migrationBanner.setNotices(report.isEmpty() ? null : new ArrayList<String>(report));

        int bannerH = migrationBanner.hasNotices() ? 78 : 0;
        int contentTop = panelY + HEADER_H + bannerH;
        int contentH = panelH - HEADER_H - bannerH;
        content.setBounds(panelX + SIDEBAR_W, contentTop, panelW - SIDEBAR_W, contentH);

        if (migrationBanner.hasNotices()) {
            migrationBanner.setBounds(panelX + SIDEBAR_W + 6, contentTop + 6, panelW - SIDEBAR_W - 14, 78);
        }

        keybindOverlay = null;
        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        DrawHelper.drawBorderedRect(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, 1.5f, themeBridge.getBorderColor(), themeBridge.getBgColor());
        DrawHelper.drawRect(panelX, panelY, panelX + panelW, panelY + panelH, themeBridge.getBgColor());
        DrawHelper.drawGradientRectH(panelX, panelY, panelX + panelW, panelY + HEADER_H, themeBridge.getAccentDarkColor(), themeBridge.getAccentColor());

        mc.fontRenderer.drawStringWithShadow("X4Tweaker", panelX + 10, panelY + 10, 0xFFFFFFFF);
        String ver = X4Tweaker.VERSION;
        int verW = mc.fontRenderer.getStringWidth(ver);
        mc.fontRenderer.drawStringWithShadow(ver, panelX + (panelW - verW) / 2, panelY + 10, 0x88AAAAAA);

        drawHeaderButtons(mouseX, mouseY);
        sidebar.render(mouseX, mouseY, partialTicks);
        content.render(mouseX, mouseY, partialTicks);

        if (migrationBanner.hasNotices()) {
            migrationBanner.render(mouseX, mouseY, partialTicks);
        }

        if (sidebar.getSelected() == Category.KEYBINDS) {
            drawKeybindButton(mouseX, mouseY);
        }

        if (keybindOverlay != null) {
            keybindOverlay.render(mouseX, mouseY, partialTicks);
        }

        ModuleRow hovered = content.getHoveredRow();
        if (hovered != null) {
            List<String> lines = hovered.getTooltipLines();
            if (!lines.isEmpty()) {
                Tooltip.render(lines, mouseX, mouseY, this.width, this.height, themeBridge.getBgColor(), themeBridge.getBorderColor());
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawHeaderButtons(int mouseX, int mouseY) {
        boolean changelogHover = mouseX >= changelogBtnX1 && mouseX <= changelogBtnX2 && mouseY >= changelogBtnY1 && mouseY <= changelogBtnY2;
        DrawHelper.drawRect(changelogBtnX1, changelogBtnY1, changelogBtnX2, changelogBtnY2, changelogHover ? 0x44FFFFFF : 0x22000000);
        mc.fontRenderer.drawStringWithShadow("Changelog", changelogBtnX1 + 4, changelogBtnY1 + 3, 0xFFFFFFFF);

        boolean themeHover = mouseX >= themeBtnX1 && mouseX <= themeBtnX2 && mouseY >= themeBtnY1 && mouseY <= themeBtnY2;
        DrawHelper.drawRect(themeBtnX1, themeBtnY1, themeBtnX2, themeBtnY2, themeHover ? 0x44FFFFFF : 0x22000000);
        mc.fontRenderer.drawStringWithShadow("Theme", themeBtnX1 + 4, themeBtnY1 + 3, 0xFFFFFFFF);

        boolean closeHover = mouseX >= closeBtnX1 && mouseX <= closeBtnX2 && mouseY >= closeBtnY1 && mouseY <= closeBtnY2;
        mc.fontRenderer.drawStringWithShadow("\u2715", closeBtnX1 + 3, closeBtnY1 + 3, closeHover ? 0xFFFF5555 : 0xFFAAAAAA);

        if (mc.isSingleplayer()) {
            boolean pauseHover = mouseX >= pauseBtnX1 && mouseX <= pauseBtnX2 && mouseY >= pauseBtnY1 && mouseY <= pauseBtnY2;
            DrawHelper.drawRect(pauseBtnX1, pauseBtnY1, pauseBtnX2, pauseBtnY2, pauseHover ? 0x44FFFFFF : 0x22FFFFFF);
            boolean paused = themeBridge.isEnablePause();
            mc.fontRenderer.drawStringWithShadow("P:" + (paused ? "ON" : "OFF"), pauseBtnX1 + 4, pauseBtnY1 + 3, paused ? 0xFF55FF55 : 0xFFFF5555);
        }
    }

    private void drawKeybindButton(int mouseX, int mouseY) {
        boolean hover = mouseX >= keybindBtnX1 && mouseX <= keybindBtnX2 && mouseY >= keybindBtnY1 && mouseY <= keybindBtnY2;
        DrawHelper.drawBorderedRect(keybindBtnX1, keybindBtnY1, keybindBtnX2, keybindBtnY2, 1.0f, themeBridge.getSeparatorColor(), hover ? themeBridge.getEnabledColor() : themeBridge.getEnabledDarkColor());
        mc.fontRenderer.drawStringWithShadow("[+] Add", keybindBtnX1 + 6, keybindBtnY1 + 4, 0xFFFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (keybindOverlay != null) {
            keybindOverlay.onMouseClick(mouseX, mouseY, mouseButton);
            return;
        }

        if (mouseButton == 0) {
            if (mouseX >= changelogBtnX1 && mouseX <= changelogBtnX2 && mouseY >= changelogBtnY1 && mouseY <= changelogBtnY2) {
                mc.displayGuiScreen(new ChangelogScreen(this));
                return;
            }
            if (mouseX >= themeBtnX1 && mouseX <= themeBtnX2 && mouseY >= themeBtnY1 && mouseY <= themeBtnY2) {
                mc.displayGuiScreen(new ThemeEditorGUI(this, themeBridge));
                return;
            }
            if (mouseX >= closeBtnX1 && mouseX <= closeBtnX2 && mouseY >= closeBtnY1 && mouseY <= closeBtnY2) {
                mc.displayGuiScreen(null);
                return;
            }
            if (mc.isSingleplayer() && mouseX >= pauseBtnX1 && mouseX <= pauseBtnX2 && mouseY >= pauseBtnY1 && mouseY <= pauseBtnY2) {
                themeBridge.setEnablePause(!themeBridge.isEnablePause());
                themeBridge.save();
                return;
            }
            if (sidebar.getSelected() == Category.KEYBINDS && mouseX >= keybindBtnX1 && mouseX <= keybindBtnX2 && mouseY >= keybindBtnY1 && mouseY <= keybindBtnY2) {
                keybindOverlay = new KeybindOverlay(themeBridge, new Runnable() {
                    @Override
                    public void run() { keybindOverlay = null; }
                });
                return;
            }
        }

        if (migrationBanner.hasNotices() && migrationBanner.onMouseClick(mouseX, mouseY, mouseButton)) return;
        if (sidebar.onMouseClick(mouseX, mouseY, mouseButton)) return;
        content.onMouseClick(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (keybindOverlay != null) { keybindOverlay.onMouseRelease(mouseX, mouseY, state); return; }
        content.onMouseRelease(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;
        if (keybindOverlay != null) { keybindOverlay.handleMouseWheel(wheel); return; }
        if (migrationBanner.hasNotices() && migrationBanner.contains(Mouse.getX() * this.width / mc.displayWidth, this.height - Mouse.getY() * this.height / mc.displayHeight - 1)) {
            migrationBanner.handleWheel(wheel);
            return;
        }
        content.handleMouseWheel(wheel);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keybindOverlay != null) { keybindOverlay.onKey(typedChar, keyCode); return; }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        content.onKey(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        X4TweakerClient.getInstance().getConfigManager().save();
        themeBridge.save();
    }
}
