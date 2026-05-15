package com.x4yi.x4tweaker.gui;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.manager.ThemeManager;
import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import com.x4yi.x4tweaker.setting.ModeSetting;
import com.x4yi.x4tweaker.setting.StringListSetting;
import com.x4yi.x4tweaker.setting.Setting;
import com.x4yi.x4tweaker.utils.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClickGUI extends GuiScreen {
    private int ancho, alto, x, y;
    private static final int MIN_W = 340, MAX_W = 520, MIN_H = 220, MAX_H = 380;
    private static final int HEADER_H = 28;
    private static final int SIDEBAR_W = 100;
    private static final int MOD_H = 22;
    private static final int SET_H = 16;
    private static final float LERP_SPEED = 0.18f;

    private Category categoriaSeleccionada = Category.VISUALS;
    private float scrollY = 0;
    private int targetScrollY = 0;

    private Setting<?> focusedSetting = null;
    private String currentInput = "";

    private boolean isKeybindOverlayOpen = false;
    private int bindingKey = Keyboard.KEY_NONE;
    private Module bindingModule = null;
    private float overlayScrollY = 0;
    private int targetOverlayScrollY = 0;

    private Module hoveredModuleTooltip = null;
    private Setting<?> hoveredSettingTooltip = null;
    private NumberSetting draggingSlider = null;

    private int themeSelectedIndex = -1;
    private boolean themeDraggingHue = false;
    private boolean themeDraggingSatVal = false;
    private boolean themeDraggingAlpha = false;
    private float themeHue = 0.0f;
    private float themeSat = 1.0f;
    private float themeVal = 1.0f;
    private int themeAlpha = 255;
    private String inputR = "", inputG = "", inputB = "", inputA = "";
    private int activeInputIndex = -1;

    private List<String> migrationNotices = new ArrayList<String>();
    private boolean showMigrationNotice = false;
    private int migrationScroll = 0;
    private int migrationMaxScroll = 0;

    @Override
    public boolean doesGuiPauseGame() {
        return X4TweakerClient.getInstance().getThemeManager().isEnablePause() && mc.isSingleplayer();
    }

    @Override
    public void initGui() {
        ancho = Math.max(MIN_W, Math.min(MAX_W, (int)(this.width * 0.6f)));
        alto  = Math.max(MIN_H, Math.min(MAX_H, (int)(this.height * 0.55f)));
        x = (this.width - ancho) / 2;
        y = (this.height - alto) / 2;
        List<String> report = X4TweakerClient.getInstance().getConfigManager().consumeMigrationReport();
        migrationNotices = report.isEmpty() ? new ArrayList<String>() : new ArrayList<String>(report);
        showMigrationNotice = !migrationNotices.isEmpty();
        migrationScroll = 0;
        migrationMaxScroll = Math.max(0, migrationNotices.size() - 5);
        Keyboard.enableRepeatEvents(true);
    }

    private int getExpandedHeight(Module m) {
        int h = 0;
        for (Setting<?> s : m.getSettings()) {
            if (!s.isVisible()) continue;
            h += SET_H;
            if (s instanceof StringListSetting) {
                h += SET_H;
                h += ((StringListSetting) s).getValue().size() * 14;
            }
        }
        h += SET_H;
        return h;
    }

    private String formatCategory(Category cat) {
        String n = cat.name();
        return n.charAt(0) + n.substring(1).toLowerCase(Locale.ROOT);
    }

    private int computeMaxScroll() {
        if (categoriaSeleccionada == Category.THEME || categoriaSeleccionada == Category.KEYBINDS) {
            int total = 0;
            if (categoriaSeleccionada == Category.KEYBINDS) {
                total += 25;
                for (Module m : X4TweakerClient.getInstance().getModuleManager().getModules()) {
                    if (m.getKeybind() != Keyboard.KEY_NONE) total += 20;
                }
            } else {
                if (themeSelectedIndex == -1) {
                    ThemeManager t = X4TweakerClient.getInstance().getThemeManager();
                    total += t.getColorCount() * 22 + 20;
                } else {
                    total += 250;
                }
            }
            int viewH = alto - HEADER_H - 15;
            return Math.max(0, total - viewH);
        }
        int total = 0;
        for (Module m : X4TweakerClient.getInstance().getModuleManager().getModulesByCategory(categoriaSeleccionada)) {
            total += MOD_H + 2;
            if (m.isExpanded() && m.isImplemented()) {
                total += (int)(getExpandedHeight(m) * m.getExpandProgress()) + 4;
            }
        }
        int viewH = alto - HEADER_H - 15;
        return Math.max(0, total - viewH);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ThemeManager tema = X4TweakerClient.getInstance().getThemeManager();
        hoveredModuleTooltip = null;
        hoveredSettingTooltip = null;


        for (Category cat : Category.values()) {
            for (Module m : X4TweakerClient.getInstance().getModuleManager().getModulesByCategory(cat)) {
                float target = m.isExpanded() ? 1.0f : 0.0f;
                float cur = m.getExpandProgress();
                if (Math.abs(cur - target) > 0.001f) {
                    m.setExpandProgress(cur + (target - cur) * LERP_SPEED);
                } else {
                    m.setExpandProgress(target);
                }
            }
        }

        scrollY += (targetScrollY - scrollY) * LERP_SPEED;
        overlayScrollY += (targetOverlayScrollY - overlayScrollY) * LERP_SPEED;


        RenderUtils.dibujarRectBordeado(x - 1, y - 1, x + ancho + 1, y + alto + 1, 1.5f, tema.getColorFondoBorde().getRGB(), 0x00000000);
        RenderUtils.dibujarRect(x, y, x + ancho, y + alto, tema.getColorFondo().getRGB());


        RenderUtils.dibujarRectGradienteHorizontal(x, y, x + ancho, y + HEADER_H, tema.getColorBotonOscuro().getRGB(), tema.getColorBotonNormal().getRGB());
        mc.fontRenderer.drawStringWithShadow("X4Tweaker", x + 10, y + 10, 0xFFFFFFFF);

        String ver = "v1.2.2";
        int verW = mc.fontRenderer.getStringWidth(ver);
        mc.fontRenderer.drawStringWithShadow(ver, x + (ancho - verW) / 2, y + 10, 0x88AAAAAA);


        boolean closeHover = mouseX >= x + ancho - 22 && mouseX <= x + ancho - 6 && mouseY >= y + 6 && mouseY <= y + 22;
        mc.fontRenderer.drawStringWithShadow("\u2715", x + ancho - 18, y + 10, closeHover ? 0xFFFF5555 : 0xFFAAAAAA);


        if (mc.isSingleplayer()) {
            int pauseX = x + ancho - 80;
            int pauseEndX = x + ancho - 26;
            boolean pauseHover = mouseX >= pauseX && mouseX <= pauseEndX && mouseY >= y + 6 && mouseY <= y + 22;
            RenderUtils.dibujarRect(pauseX, y + 7, pauseEndX, y + 21, pauseHover ? 0x44FFFFFF : 0x22FFFFFF);
            boolean paused = tema.isEnablePause();
            mc.fontRenderer.drawStringWithShadow("P:" + (paused ? "ON" : "OFF"), pauseX + 4, y + 10, paused ? 0xFF55FF55 : 0xFFFF5555);
        }


        int catY = y + HEADER_H + 8;
        for (Category cat : Category.values()) {
            if (cat == Category.HIDDEN) continue;
            if (cat == Category.KEYBINDS) {
                catY = y + alto - 24;
            }

            boolean esSel = (cat == categoriaSeleccionada);
            boolean hover = mouseX >= x + 4 && mouseX <= x + SIDEBAR_W - 4 && mouseY >= catY && mouseY <= catY + 18;

            if (esSel) {
                RenderUtils.dibujarRect(x + 2, catY, x + 5, catY + 18, tema.getColorBotonNormal().getRGB());
                RenderUtils.dibujarRect(x + 5, catY, x + SIDEBAR_W - 4, catY + 18, 0x22FFFFFF);
            } else if (hover) {
                RenderUtils.dibujarRect(x + 5, catY, x + SIDEBAR_W - 4, catY + 18, 0x11FFFFFF);
            }

            mc.fontRenderer.drawStringWithShadow(formatCategory(cat), x + 12, catY + 5, esSel ? 0xFFFFFFFF : 0xFF999999);
            if (cat != Category.KEYBINDS) catY += 21;
        }


        RenderUtils.dibujarRect(x + SIDEBAR_W, y + HEADER_H, x + SIDEBAR_W + 1, y + alto, 0x33FFFFFF);


        RenderUtils.dibujarRect(x + SIDEBAR_W + 1, y + HEADER_H, x + ancho, y + alto, 0x0DFFFFFF);

        if (categoriaSeleccionada == Category.KEYBINDS) {
            drawKeybindsTab(mouseX, mouseY, tema);
        } else if (categoriaSeleccionada == Category.THEME) {
            drawThemeTab(mouseX, mouseY, tema);
        } else {
            drawModulesTab(mouseX, mouseY, tema);
        }

        manejarScroll();

        if (isKeybindOverlayOpen) {
            drawKeybindOverlay(mouseX, mouseY, tema);
        } else {
            super.drawScreen(mouseX, mouseY, partialTicks);
            drawMigrationNotice(mouseX, mouseY);
            if (hoveredModuleTooltip != null) {
                drawTooltip(mouseX, mouseY, hoveredModuleTooltip);
            } else if (hoveredSettingTooltip != null) {
                drawSettingTooltip(mouseX, mouseY, hoveredSettingTooltip);
            }
        }
    }

    private void drawTooltip(int mouseX, int mouseY, Module m) {
        String moduleKey = normalizeKey(m.getName());
        String descKey = "x4tweaker.module." + moduleKey + ".desc";
        String tooltip = I18n.hasKey(descKey) ? I18n.format(descKey) : m.getDescription();
        if (!m.isImplemented()) tooltip = "Coming Soon...";

        int textWidth = mc.fontRenderer.getStringWidth(tooltip);
        int tx = mouseX + 10;
        int ty = mouseY + 10;

        if (tx + textWidth + 8 > this.width) tx = this.width - textWidth - 8;
        if (ty + 16 > this.height) ty = this.height - 16;
        if (tx < 0) tx = 0;
        if (ty < 0) ty = 0;

        RenderUtils.dibujarRectBordeado(tx, ty, tx + textWidth + 8, ty + 14, 1.0f, 0xFF000000, 0xDD000000);
        mc.fontRenderer.drawStringWithShadow(tooltip, tx + 4, ty + 3, !m.isImplemented() ? 0xFFFF5555 : 0xFFFFFFFF);
    }

    private void drawSettingTooltip(int mouseX, int mouseY, Setting<?> s) {
        String tooltip = s.getDescription();
        if (tooltip == null || tooltip.isEmpty()) return;

        int textWidth = mc.fontRenderer.getStringWidth(tooltip);
        int tx = mouseX + 10;
        int ty = mouseY + 10;
        if (tx + textWidth + 8 > this.width) tx = this.width - textWidth - 8;
        if (ty + 16 > this.height) ty = this.height - 16;
        if (tx < 0) tx = 0;
        if (ty < 0) ty = 0;

        RenderUtils.dibujarRectBordeado(tx, ty, tx + textWidth + 8, ty + 14, 1.0f, 0xFF000000, 0xDD000000);
        mc.fontRenderer.drawStringWithShadow(tooltip, tx + 4, ty + 3, 0xFFFFFFFF);
    }

    private void drawModulesTab(int mouseX, int mouseY, ThemeManager tema) {
        int modX = x + SIDEBAR_W + 6;
        int modY = y + HEADER_H + 6 + (int)scrollY;
        int vpTop = y + HEADER_H;
        int vpBot = y + alto - 4;
        int panelRight = x + ancho - 6;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        GL11.glScissor((x + SIDEBAR_W + 1) * scale, mc.displayHeight - (y + alto) * scale, (ancho - SIDEBAR_W - 1) * scale, (alto - HEADER_H) * scale);

        for (Module m : X4TweakerClient.getInstance().getModuleManager().getModulesByCategory(categoriaSeleccionada)) {
            float ep = m.getExpandProgress();
            int expH = m.isImplemented() ? getExpandedHeight(m) : 0;
            int animH = (int)(expH * ep);
            int blockBottom = modY + MOD_H + (animH > 0 ? animH + 4 : 0);

            if (blockBottom < vpTop || modY > vpBot) {
                modY += MOD_H + 2;
                if (ep > 0.01f) modY += animH + 4;
                continue;
            }

            boolean hover = mouseX >= modX && mouseX <= panelRight && mouseY >= modY && mouseY <= modY + MOD_H && mouseY >= vpTop && mouseY <= vpBot;
            if (hover) hoveredModuleTooltip = m;


            int bgColor = hover ? 0x33FFFFFF : 0x22FFFFFF;
            if (!m.isImplemented()) bgColor = 0x11FFFFFF;
            RenderUtils.dibujarRect(modX, modY, panelRight, modY + MOD_H, bgColor);


            if (m.isImplemented() && !m.getSettings().isEmpty()) {
                String arrow = ep > 0.5f ? "\u25BC" : "\u25B6";
                mc.fontRenderer.drawStringWithShadow(arrow, modX + 3, modY + 7, 0xFF888888);
            }


            mc.fontRenderer.drawStringWithShadow(getModuleDisplayName(m), modX + 14, modY + 7, !m.isImplemented() ? 0xFF666666 : 0xFFEEEEEE);


            if (m.isImplemented()) {
                boolean on = m.isEnabled();
                String toggleTxt = on ? "ON" : "OFF";
                int tw = mc.fontRenderer.getStringWidth(toggleTxt) + 8;
                int toggleX = panelRight - tw - 4;
                int toggleColor = on ? 0xFF00AA44 : 0xFF882222;
                int toggleBorder = on ? 0xFF00DD55 : 0xFFAA3333;
                RenderUtils.dibujarRect(toggleX, modY + 4, panelRight - 4, modY + MOD_H - 4, toggleColor);
                RenderUtils.dibujarRectBordeado(toggleX, modY + 4, panelRight - 4, modY + MOD_H - 4, 1.0f, toggleBorder, 0x00000000);
                mc.fontRenderer.drawStringWithShadow(toggleTxt, toggleX + 4, modY + 7, 0xFFFFFFFF);
            }

            modY += MOD_H + 2;


            if (ep > 0.01f && m.isImplemented() && !m.getSettings().isEmpty()) {
                int settingsTop = modY;
                RenderUtils.dibujarRect(modX + 4, modY, panelRight, modY + animH, 0x22000000);


                if (ep < 0.99f) {
                    GL11.glScissor((modX + 4) * scale, mc.displayHeight - (modY + animH) * scale, (panelRight - modX - 4) * scale, animH * scale);
                }

                int setY = modY + 2;
                for (Setting<?> s : m.getSettings()) {
                    if (!s.isVisible()) continue;

                    if (s instanceof StringListSetting) {
                        StringListSetting sl = (StringListSetting) s;
                        boolean headHover = mouseX >= modX + 10 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 12;
                        if (headHover) hoveredSettingTooltip = s;
                        mc.fontRenderer.drawStringWithShadow(getSettingDisplayName(m, s) + ":", modX + 10, setY + 3, 0xFFDDDDDD);
                        setY += SET_H;

                        boolean inputHover = mouseX >= modX + 10 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 12;
                        if (inputHover) hoveredSettingTooltip = s;
                        RenderUtils.dibujarRect(modX + 10, setY, panelRight - 5, setY + 12, inputHover ? 0x44FFFFFF : 0x22FFFFFF);
                        String displayStr = (focusedSetting == s) ? currentInput + "_" : "Click to add...";
                        mc.fontRenderer.drawStringWithShadow(displayStr, modX + 14, setY + 2, 0xFFAAAAAA);
                        setY += SET_H;

                        for (String item : sl.getValue()) {
                            mc.fontRenderer.drawStringWithShadow("- " + item, modX + 15, setY + 2, 0xFFCCCCCC);
                            boolean delHover = mouseX >= panelRight - 20 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 10;
                            mc.fontRenderer.drawStringWithShadow("[x]", panelRight - 20, setY + 2, delHover ? 0xFFFF0000 : 0xFFFF5555);
                            setY += 14;
                        }
                    } else if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        boolean setHover = mouseX >= modX + 8 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 14;
                        if (setHover) hoveredSettingTooltip = s;

                        int sliderX = modX + 8;
                        int sliderRight = panelRight - 5;
                        int sliderW = sliderRight - sliderX;


                        RenderUtils.drawRoundedRect(sliderX, setY + 1, sliderRight, setY + 13, 3, 0x33000000);

                        double min = ns.getMin();
                        double max = ns.getMax();
                        double val = ns.getValue();
                        int fillWidth = (int)(sliderW * ((val - min) / (max - min)));


                        if (fillWidth > 0) {
                            RenderUtils.drawRoundedRect(sliderX, setY + 1, sliderX + fillWidth, setY + 13, 3,
                                tema.getColorBotonNormal().getRGB());
                        }


                        int nubX = sliderX + fillWidth;
                        RenderUtils.drawCircle(nubX, setY + 7, 4, 0xFFFFFFFF);
                        RenderUtils.drawCircle(nubX, setY + 7, 3, tema.getColorBotonNormal().getRGB());

                        if (Mouse.isButtonDown(0) && draggingSlider == ns) {
                            double clickRatio = Math.max(0, Math.min(1, (double)(mouseX - sliderX) / sliderW));
                            ns.setValue(min + clickRatio * (max - min));
                        }

                        String displayStr = getSettingDisplayName(m, s) + ": " + (ns.getIncrement() == 1.0 ? String.valueOf((int) val) : String.format("%.2f", val));
                        mc.fontRenderer.drawStringWithShadow(displayStr, modX + 12, setY + 3, 0xFFDDDDDD);
                        setY += SET_H;
                    } else if (s instanceof BooleanSetting) {
                        boolean setHover = mouseX >= modX + 8 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 14;
                        if (setHover) hoveredSettingTooltip = s;

                        mc.fontRenderer.drawStringWithShadow(getSettingDisplayName(m, s), modX + 10, setY + 3, 0xFFDDDDDD);


                        boolean on = ((BooleanSetting)s).getValue();
                        int swX = panelRight - 30;
                        int swBg = on ? 0xFF00AA44 : 0xFF555555;
                        RenderUtils.drawRoundedRect(swX, setY + 2, swX + 22, setY + 12, 5, swBg);
                        int handleX = on ? swX + 12 : swX + 2;
                        RenderUtils.drawCircle(handleX + 4, setY + 7, 4, 0xFFFFFFFF);

                        setY += SET_H;
                    } else if (s instanceof ModeSetting) {
                        boolean setHover = mouseX >= modX + 8 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 14;
                        if (setHover) hoveredSettingTooltip = s;

                        mc.fontRenderer.drawStringWithShadow(getSettingDisplayName(m, s) + ":", modX + 10, setY + 3, 0xFFDDDDDD);


                        String modeVal = String.valueOf(s.getValue());
                        int modeW = mc.fontRenderer.getStringWidth(modeVal);
                        int centerX = modX + (panelRight - modX) / 2;

                        boolean leftHov = mouseX >= centerX - modeW/2 - 14 && mouseX <= centerX - modeW/2 - 4 && mouseY >= setY && mouseY <= setY + 14;
                        boolean rightHov = mouseX >= centerX + modeW/2 + 4 && mouseX <= centerX + modeW/2 + 14 && mouseY >= setY && mouseY <= setY + 14;

                        mc.fontRenderer.drawStringWithShadow("\u25C0", centerX - modeW/2 - 12, setY + 3, leftHov ? 0xFFFFFFFF : 0xFF888888);
                        mc.fontRenderer.drawStringWithShadow(modeVal, centerX - modeW/2, setY + 3, 0xFFBBBBFF);
                        mc.fontRenderer.drawStringWithShadow("\u25B6", centerX + modeW/2 + 4, setY + 3, rightHov ? 0xFFFFFFFF : 0xFF888888);
                        setY += SET_H;
                    } else {
                        boolean setHover = mouseX >= modX + 8 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 14;
                        if (setHover) hoveredSettingTooltip = s;
                        if (setHover) RenderUtils.dibujarRect(modX + 8, setY, panelRight - 5, setY + 14, 0x22FFFFFF);
                        mc.fontRenderer.drawStringWithShadow(getSettingDisplayName(m, s) + ": " + s.getValue(), modX + 10, setY + 3, 0xFFDDDDDD);
                        setY += SET_H;
                    }
                }


                boolean resetHover = mouseX >= modX + 8 && mouseX <= modX + 60 && mouseY >= setY && mouseY <= setY + 12;
                RenderUtils.dibujarRect(modX + 8, setY, modX + 60, setY + 12, resetHover ? 0x44FF3333 : 0x22FF3333);
                mc.fontRenderer.drawStringWithShadow("[R] Reset", modX + 11, setY + 2, resetHover ? 0xFFFF5555 : 0xFF888888);


                if (ep < 0.99f) {
                    GL11.glScissor((x + SIDEBAR_W + 1) * scale, mc.displayHeight - (y + alto) * scale, (ancho - SIDEBAR_W - 1) * scale, (alto - HEADER_H) * scale);
                }

                modY += animH + 4;
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawKeybindsTab(int mouseX, int mouseY, ThemeManager tema) {
        int modX = x + SIDEBAR_W + 6;
        int panelRight = x + ancho - 6;
        int btnY = y + HEADER_H + 6 + (int)scrollY;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        GL11.glScissor((x + SIDEBAR_W + 1) * scale, mc.displayHeight - (y + alto) * scale, (ancho - SIDEBAR_W - 1) * scale, (alto - HEADER_H) * scale);

        boolean addHover = mouseX >= modX && mouseX <= panelRight && mouseY >= btnY && mouseY <= btnY + 20;
        RenderUtils.dibujarRectGradienteHorizontal(modX, btnY, panelRight, btnY + 20, addHover ? 0xFF00AA00 : 0xFF008800, 0xFF006600);
        mc.fontRenderer.drawStringWithShadow("[+] Add Keybind", modX + 8, btnY + 6, 0xFFFFFFFF);

        int listY = btnY + 25;
        for (Module m : X4TweakerClient.getInstance().getModuleManager().getModules()) {
            if (m.getKeybind() == Keyboard.KEY_NONE) continue;
            RenderUtils.dibujarRect(modX, listY, panelRight, listY + 18, 0x22000000);
            mc.fontRenderer.drawStringWithShadow(getModuleDisplayName(m), modX + 5, listY + 5, 0xFFFFFFFF);

            String keyName = "[" + Keyboard.getKeyName(m.getKeybind()) + "]";
            int keyWidth = mc.fontRenderer.getStringWidth(keyName);

            boolean delHover = mouseX >= panelRight - 20 && mouseX <= panelRight - 5 && mouseY >= listY + 4 && mouseY <= listY + 14;
            mc.fontRenderer.drawStringWithShadow("[x]", panelRight - 20, listY + 5, delHover ? 0xFFFF0000 : 0xFFFF5555);
            mc.fontRenderer.drawStringWithShadow(keyName, panelRight - 25 - keyWidth, listY + 5, 0xFF888888);
            listY += 20;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawKeybindOverlay(int mouseX, int mouseY, ThemeManager tema) {
        RenderUtils.dibujarRect(0, 0, this.width, this.height, 0xAA000000);

        int ox = this.width / 2 - 100;
        int oy = this.height / 2 - 100;
        int ow = 200;
        int oh = 200;

        RenderUtils.dibujarRectBordeado(ox - 1, oy - 1, ox + ow + 1, oy + oh + 1, 1.5f, tema.getColorFondoBorde().getRGB(), 0x00000000);
        RenderUtils.dibujarRect(ox, oy, ox + ow, oy + oh, tema.getColorFondo().getRGB());

        mc.fontRenderer.drawStringWithShadow("Add Keybind", ox + 10, oy + 10, 0xFFFFFFFF);

        String keyText = bindingKey == Keyboard.KEY_NONE ? "Press a key..." : "Key: " + Keyboard.getKeyName(bindingKey);
        mc.fontRenderer.drawStringWithShadow(keyText, ox + 10, oy + 25, bindingKey == Keyboard.KEY_NONE ? 0xFFFF5555 : 0xFF55FF55);

        int listTop = oy + 45;
        int listBot = oy + oh - 30;
        RenderUtils.dibujarRect(ox + 5, listTop, ox + ow - 5, listBot, 0x22000000);


        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        GL11.glScissor((ox + 5) * scale, mc.displayHeight - listBot * scale, (ow - 10) * scale, (listBot - listTop) * scale);

        int itemY = listTop + (int)overlayScrollY;
        for (Module m : X4TweakerClient.getInstance().getModuleManager().getModules()) {
            if (itemY + 14 > listTop && itemY < listBot) {
                boolean isSelected = bindingModule == m;
                boolean itemHover = mouseX >= ox + 5 && mouseX <= ox + ow - 5 && mouseY >= itemY && mouseY <= itemY + 14;
                if (isSelected) {
                    RenderUtils.dibujarRect(ox + 5, itemY, ox + ow - 5, itemY + 14, 0x4400FF00);
                } else if (itemHover) {
                    RenderUtils.dibujarRect(ox + 5, itemY, ox + ow - 5, itemY + 14, 0x22FFFFFF);
                }
                mc.fontRenderer.drawStringWithShadow(getModuleDisplayName(m), ox + 10, itemY + 3, isSelected ? 0xFFFFFFFF : 0xFFCCCCCC);
            }
            itemY += 14;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        boolean canConfirm = bindingKey != Keyboard.KEY_NONE && bindingModule != null;
        boolean confHover = canConfirm && mouseX >= ox + 10 && mouseX <= ox + 90 && mouseY >= oy + oh - 25 && mouseY <= oy + oh - 5;
        RenderUtils.dibujarRect(ox + 10, oy + oh - 25, ox + 90, oy + oh - 5, canConfirm ? (confHover ? 0xFF00AA00 : 0xFF008800) : 0xFF444444);
        mc.fontRenderer.drawStringWithShadow("Confirm", ox + 30, oy + oh - 18, canConfirm ? 0xFFFFFFFF : 0xFFAAAAAA);

        boolean canHover = mouseX >= ox + 110 && mouseX <= ox + 190 && mouseY >= oy + oh - 25 && mouseY <= oy + oh - 5;
        RenderUtils.dibujarRect(ox + 110, oy + oh - 25, ox + 190, oy + oh - 5, canHover ? 0xFFAA0000 : 0xFF880000);
        mc.fontRenderer.drawStringWithShadow("Cancel", ox + 135, oy + oh - 18, 0xFFFFFFFF);
    }

    private void drawThemeTab(int mouseX, int mouseY, ThemeManager tema) {
        int modX = x + SIDEBAR_W + 6;
        int panelRight = x + ancho - 6;
        int listY = y + HEADER_H + 6 + (int)scrollY;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();
        GL11.glScissor((x + SIDEBAR_W + 1) * scale, mc.displayHeight - (y + alto) * scale, (ancho - SIDEBAR_W - 1) * scale, (alto - HEADER_H) * scale);

        if (themeSelectedIndex == -1) {
            for (int i = 0; i < tema.getColorCount(); i++) {
                boolean hover = mouseX >= modX && mouseX <= panelRight && mouseY >= listY && mouseY <= listY + 20;
                RenderUtils.dibujarRect(modX, listY, panelRight, listY + 20, hover ? 0x44FFFFFF : 0x22000000);

                int rgb = tema.getColorByIndex(i).getRGB();
                RenderUtils.dibujarRectBordeado(modX + 5, listY + 5, modX + 15, listY + 15, 1.0f, 0xFF000000, rgb);
                mc.fontRenderer.drawStringWithShadow(tema.getColorName(i), modX + 20, listY + 6, 0xFFFFFFFF);

                String hex = String.format("#%08X", rgb);
                int textW = mc.fontRenderer.getStringWidth(hex);
                mc.fontRenderer.drawStringWithShadow(hex, panelRight - 5 - textW, listY + 6, 0xFFAAAAAA);
                listY += 22;
            }
            boolean resetHover = mouseX >= modX && mouseX <= modX + 80 && mouseY >= listY && mouseY <= listY + 15;
            RenderUtils.dibujarRect(modX, listY, modX + 80, listY + 15, resetHover ? 0xFFAA0000 : 0xFF880000);
            mc.fontRenderer.drawStringWithShadow("Reset Theme", modX + 5, listY + 4, 0xFFFFFFFF);
        } else {
            if (themeDraggingSatVal) {
                float sat = Math.max(0, Math.min(1, (float)(mouseX - modX) / 160f));
                float val = 1.0f - Math.max(0, Math.min(1, (float)(mouseY - listY - 15) / 100f));
                themeSat = sat;
                themeVal = val;
                updateSelectedThemeColor(tema);
            } else if (themeDraggingHue) {
                float hue = Math.max(0, Math.min(1, (float)(mouseX - modX) / 160f));
                themeHue = hue;
                updateSelectedThemeColor(tema);
            } else if (themeDraggingAlpha) {
                float alphaPct = Math.max(0, Math.min(1, (float)(mouseX - modX) / 160f));
                themeAlpha = (int)(alphaPct * 255);
                updateSelectedThemeColor(tema);
            }

            mc.fontRenderer.drawStringWithShadow("Editing: " + tema.getColorName(themeSelectedIndex), modX, listY, 0xFFFFFFFF);
            listY += 15;

            int currentRgb = tema.getColorByIndex(themeSelectedIndex).getRGB();
            RenderUtils.dibujarRectBordeado(modX + 170, listY, modX + 210, listY + 100, 1.0f, 0xFF000000, currentRgb);

            RenderUtils.dibujarRect(modX, listY, modX + 160, listY + 100, 0xFF000000);
            for (int dx = 0; dx < 160; dx += 2) {
                for (int dy = 0; dy < 100; dy += 2) {
                    float s = (float)dx / 160f;
                    float v = 1.0f - ((float)dy / 100f);
                    int rgb = java.awt.Color.HSBtoRGB(themeHue, s, v);
                    RenderUtils.dibujarRect(modX + dx, listY + dy, modX + dx + 2, listY + dy + 2, rgb | 0xFF000000);
                }
            }
            int px = modX + (int)(themeSat * 160);
            int py = listY + (int)((1.0f - themeVal) * 100);
            RenderUtils.dibujarRectBordeado(px - 2, py - 2, px + 2, py + 2, 1.0f, 0xFF000000, 0xFFFFFFFF);

            listY += 105;

            for (int dx = 0; dx < 160; dx++) {
                float h = (float)dx / 160f;
                int rgb = java.awt.Color.HSBtoRGB(h, 1.0f, 1.0f);
                RenderUtils.dibujarRect(modX + dx, listY, modX + dx + 1, listY + 10, rgb | 0xFF000000);
            }
            int hx = modX + (int)(themeHue * 160);
            RenderUtils.dibujarRectBordeado(hx - 2, listY - 1, hx + 2, listY + 11, 1.0f, 0xFF000000, 0xFFFFFFFF);
            listY += 15;

            RenderUtils.dibujarRect(modX, listY, modX + 160, listY + 10, 0xFF000000);
            RenderUtils.dibujarRectGradienteHorizontal(modX, listY, modX + 160, listY + 10, currentRgb & 0x00FFFFFF, currentRgb | 0xFF000000);
            int ax = modX + (int)((themeAlpha / 255f) * 160);
            RenderUtils.dibujarRectBordeado(ax - 2, listY - 1, ax + 2, listY + 11, 1.0f, 0xFF000000, 0xFFFFFFFF);
            listY += 15;

            java.awt.Color c = tema.getColorByIndex(themeSelectedIndex);
            drawThemeInput(modX, listY, "R", c.getRed(), 0, mouseX, mouseY);
            drawThemeInput(modX + 45, listY, "G", c.getGreen(), 1, mouseX, mouseY);
            drawThemeInput(modX + 90, listY, "B", c.getBlue(), 2, mouseX, mouseY);
            drawThemeInput(modX + 135, listY, "A", c.getAlpha(), 3, mouseX, mouseY);
            listY += 20;

            boolean backHover = mouseX >= modX && mouseX <= modX + 60 && mouseY >= listY && mouseY <= listY + 15;
            RenderUtils.dibujarRect(modX, listY, modX + 60, listY + 15, backHover ? 0xFF00AA00 : 0xFF008800);
            mc.fontRenderer.drawStringWithShadow("Save & Back", modX + 5, listY + 4, 0xFFFFFFFF);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawThemeInput(int xPos, int yPos, String label, int currentVal, int index, int mouseX, int mouseY) {
        mc.fontRenderer.drawStringWithShadow(label + ":", xPos, yPos + 3, 0xFFAAAAAA);
        boolean hover = mouseX >= xPos + 12 && mouseX <= xPos + 38 && mouseY >= yPos && mouseY <= yPos + 14;
        String activeInput = activeInputIndex == index ? (index == 0 ? inputR : index == 1 ? inputG : index == 2 ? inputB : inputA) : null;
        RenderUtils.dibujarRect(xPos + 12, yPos, xPos + 38, yPos + 14, activeInput != null ? 0x44FFFFFF : (hover ? 0x22FFFFFF : 0x22000000));
        String text = activeInput != null ? activeInput + "_" : String.valueOf(currentVal);
        mc.fontRenderer.drawStringWithShadow(text, xPos + 15, yPos + 3, 0xFFFFFFFF);
    }

    private void updateSelectedThemeColor(ThemeManager tema) {
        int rgb = java.awt.Color.HSBtoRGB(themeHue, themeSat, themeVal);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        tema.setColorByIndex(themeSelectedIndex, new java.awt.Color(r, g, b, themeAlpha));
    }

    private void manejarScroll() {
        int dWheel = Mouse.getDWheel();
        if (dWheel == 0) return;

        if (showMigrationNotice) {
            int boxX1 = x + SIDEBAR_W + 6;
            int boxY1 = y + HEADER_H + 6;
            int boxX2 = x + ancho - 8;
            int boxY2 = boxY1 + 78;
            int mx = Mouse.getX() * this.width / mc.displayWidth;
            int my = this.height - Mouse.getY() * this.height / mc.displayHeight - 1;
            if (mx >= boxX1 && mx <= boxX2 && my >= boxY1 && my <= boxY2) {
                if (dWheel < 0) migrationScroll = Math.min(migrationMaxScroll, migrationScroll + 1);
                else migrationScroll = Math.max(0, migrationScroll - 1);
                return;
            }
        }

        int maxScroll = computeMaxScroll();
        if (dWheel < 0) {
            if (isKeybindOverlayOpen) targetOverlayScrollY -= 20;
            else { targetScrollY -= 25; if (targetScrollY < -maxScroll) targetScrollY = -maxScroll; }
        } else {
            if (isKeybindOverlayOpen) {
                targetOverlayScrollY += 20;
                if (targetOverlayScrollY > 0) targetOverlayScrollY = 0;
            } else {
                targetScrollY += 25;
                if (targetScrollY > 0) targetScrollY = 0;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton != 0 && mouseButton != 1) return;

        if (isKeybindOverlayOpen) {
            handleOverlayClick(mouseX, mouseY, mouseButton);
            return;
        }

        focusedSetting = null;

        if (showMigrationNotice && mouseButton == 0) {
            int boxX1 = x + SIDEBAR_W + 6;
            int closeX1 = x + ancho - 24;
            int closeY1 = y + HEADER_H + 10;
            if (mouseX >= closeX1 && mouseX <= closeX1 + 12 && mouseY >= closeY1 && mouseY <= closeY1 + 12) {
                showMigrationNotice = false;
                return;
            }
            if (mouseX >= boxX1 && mouseX <= x + ancho - 8 && mouseY >= y + HEADER_H + 6 && mouseY <= y + HEADER_H + 84) {
                return;
            }
        }


        if (mouseButton == 0 && mouseX >= x + ancho - 22 && mouseX <= x + ancho - 6 && mouseY >= y + 6 && mouseY <= y + 22) {
            mc.displayGuiScreen(null);
            return;
        }


        if (mc.isSingleplayer() && mouseButton == 0) {
            int pauseX = x + ancho - 80;
            int pauseEndX = x + ancho - 26;
            if (mouseX >= pauseX && mouseX <= pauseEndX && mouseY >= y + 6 && mouseY <= y + 22) {
                ThemeManager tema = X4TweakerClient.getInstance().getThemeManager();
                tema.setEnablePause(!tema.isEnablePause());
                tema.save();
                return;
            }
        }


        int catY = y + HEADER_H + 8;
        for (Category cat : Category.values()) {
            if (cat == Category.HIDDEN) continue;
            if (cat == Category.KEYBINDS) catY = y + alto - 24;
            if (mouseX >= x + 4 && mouseX <= x + SIDEBAR_W - 4 && mouseY >= catY && mouseY <= catY + 18) {
                categoriaSeleccionada = cat;
                targetScrollY = 0;
                scrollY = 0;
                themeSelectedIndex = -1;
                activeInputIndex = -1;
                return;
            }
            if (cat != Category.KEYBINDS) catY += 21;
        }

        if (categoriaSeleccionada == Category.KEYBINDS) {
            handleKeybindsTabClick(mouseX, mouseY, mouseButton);
        } else if (categoriaSeleccionada == Category.THEME) {
            handleThemeTabClick(mouseX, mouseY, mouseButton);
        } else {
            handleModulesTabClick(mouseX, mouseY, mouseButton);
        }
    }

    private void handleOverlayClick(int mouseX, int mouseY, int mouseButton) {
        int ox = this.width / 2 - 100;
        int oy = this.height / 2 - 100;
        int ow = 200;
        int oh = 200;

        int listTop = oy + 45;
        int listBot = oy + oh - 30;
        int itemY = listTop + (int)overlayScrollY;
        for (Module m : X4TweakerClient.getInstance().getModuleManager().getModules()) {
            if (itemY + 14 > listTop && itemY < listBot) {
                if (mouseX >= ox + 5 && mouseX <= ox + ow - 5 && mouseY >= itemY && mouseY <= itemY + 14) {
                    bindingModule = m;
                    return;
                }
            }
            itemY += 14;
        }

        if (mouseButton == 0 && bindingKey != Keyboard.KEY_NONE && bindingModule != null) {
            if (mouseX >= ox + 10 && mouseX <= ox + 90 && mouseY >= oy + oh - 25 && mouseY <= oy + oh - 5) {
                bindingModule.setKeybind(bindingKey);
                X4TweakerClient.getInstance().getConfigManager().save();
                isKeybindOverlayOpen = false;
                return;
            }
        }

        if (mouseButton == 0 && mouseX >= ox + 110 && mouseX <= ox + 190 && mouseY >= oy + oh - 25 && mouseY <= oy + oh - 5) {
            isKeybindOverlayOpen = false;
        }
    }

    private void handleKeybindsTabClick(int mouseX, int mouseY, int mouseButton) {
        int modX = x + SIDEBAR_W + 6;
        int panelRight = x + ancho - 6;
        int btnY = y + HEADER_H + 6 + (int)scrollY;

        if (mouseX >= modX && mouseX <= panelRight && mouseY >= btnY && mouseY <= btnY + 20) {
            isKeybindOverlayOpen = true;
            bindingKey = Keyboard.KEY_NONE;
            bindingModule = null;
            targetOverlayScrollY = 0;
            overlayScrollY = 0;
            return;
        }

        int listY = btnY + 25;
        for (Module m : X4TweakerClient.getInstance().getModuleManager().getModules()) {
            if (m.getKeybind() == Keyboard.KEY_NONE) continue;
            if (mouseX >= panelRight - 20 && mouseX <= panelRight - 5 && mouseY >= listY + 4 && mouseY <= listY + 14) {
                if (m.getName().equalsIgnoreCase("ClickGUI")) return;
                m.setKeybind(Keyboard.KEY_NONE);
                X4TweakerClient.getInstance().getConfigManager().save();
                return;
            }
            listY += 20;
        }
    }

    private void handleModulesTabClick(int mouseX, int mouseY, int mouseButton) {
        int modX = x + SIDEBAR_W + 6;
        int panelRight = x + ancho - 6;
        int modY = y + HEADER_H + 6 + (int)scrollY;
        int vpTop = y + HEADER_H;
        int vpBot = y + alto - 4;

        for (Module m : X4TweakerClient.getInstance().getModuleManager().getModulesByCategory(categoriaSeleccionada)) {
            float ep = m.getExpandProgress();
            int expH = m.isImplemented() ? getExpandedHeight(m) : 0;
            int animH = (int)(expH * ep);


            if (modY + MOD_H >= vpTop && modY <= vpBot) {

                if (m.isImplemented()) {
                    boolean on = m.isEnabled();
                    String toggleTxt = on ? "ON" : "OFF";
                    int tw = mc.fontRenderer.getStringWidth(toggleTxt) + 8;
                    int toggleX = panelRight - tw - 4;
                    if (mouseButton == 0 && mouseX >= toggleX && mouseX <= panelRight - 4 && mouseY >= modY + 4 && mouseY <= modY + MOD_H - 4) {
                        m.toggle();
                        X4TweakerClient.getInstance().getConfigManager().save();
                        return;
                    }
                }

                if (mouseX >= modX && mouseX <= panelRight && mouseY >= modY && mouseY <= modY + MOD_H) {
                    if (!m.isImplemented()) return;
                    if (mouseButton == 0) m.toggle();
                    else if (mouseButton == 1) m.setExpanded(!m.isExpanded());
                    X4TweakerClient.getInstance().getConfigManager().save();
                    return;
                }
            }

            modY += MOD_H + 2;


            if (ep > 0.5f && m.isImplemented() && !m.getSettings().isEmpty()) {
                int setY = modY + 2;
                for (Setting<?> s : m.getSettings()) {
                    if (!s.isVisible()) continue;

                    if (s instanceof StringListSetting) {
                        StringListSetting sl = (StringListSetting) s;
                        setY += SET_H;
                        if (mouseButton == 0 && mouseX >= modX + 10 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 12) {
                            focusedSetting = s;
                            currentInput = "";
                            return;
                        }
                        setY += SET_H;
                        String toRemove = null;
                        for (String item : sl.getValue()) {
                            if (mouseButton == 0 && mouseX >= panelRight - 20 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 10) {
                                toRemove = item;
                            }
                            setY += 14;
                        }
                        if (toRemove != null) {
                            sl.removeString(toRemove);
                            X4TweakerClient.getInstance().getConfigManager().save();
                            return;
                        }
                    } else if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        int sliderX = modX + 8;
                        int sliderRight = panelRight - 5;
                        if (mouseButton == 0 && mouseX >= sliderX && mouseX <= sliderRight && mouseY >= setY && mouseY <= setY + 14) {
                            draggingSlider = ns;
                            int sliderW = sliderRight - sliderX;
                            double clickRatio = Math.max(0, Math.min(1, (double)(mouseX - sliderX) / sliderW));
                            ns.setValue(ns.getMin() + clickRatio * (ns.getMax() - ns.getMin()));
                            return;
                        }
                        setY += SET_H;
                    } else if (s instanceof BooleanSetting) {
                        if (mouseButton == 0 && mouseX >= modX + 8 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 14) {
                            ((BooleanSetting) s).toggle();
                            X4TweakerClient.getInstance().getConfigManager().save();
                            return;
                        }
                        setY += SET_H;
                    } else if (s instanceof ModeSetting) {
                        ModeSetting ms = (ModeSetting) s;
                        String modeVal = String.valueOf(s.getValue());
                        int modeW = mc.fontRenderer.getStringWidth(modeVal);
                        int centerX = modX + (panelRight - modX) / 2;

                        if (mouseY >= setY && mouseY <= setY + 14) {
                            if (mouseX >= centerX - modeW/2 - 14 && mouseX <= centerX - modeW/2 - 4) {
                                if (mouseButton == 0) ms.cycleBack();
                                X4TweakerClient.getInstance().getConfigManager().save();
                                return;
                            }
                            if (mouseX >= centerX + modeW/2 + 4 && mouseX <= centerX + modeW/2 + 14) {
                                if (mouseButton == 0) ms.cycle();
                                X4TweakerClient.getInstance().getConfigManager().save();
                                return;
                            }

                            if (mouseButton == 0 && mouseX >= modX + 8 && mouseX <= panelRight - 5) {
                                ms.cycle();
                                X4TweakerClient.getInstance().getConfigManager().save();
                                return;
                            }
                            if (mouseButton == 1 && mouseX >= modX + 8 && mouseX <= panelRight - 5) {
                                ms.cycleBack();
                                X4TweakerClient.getInstance().getConfigManager().save();
                                return;
                            }
                        }
                        setY += SET_H;
                    } else {
                        if (mouseButton == 0 && mouseX >= modX + 8 && mouseX <= panelRight - 5 && mouseY >= setY && mouseY <= setY + 14) {
                            if (s instanceof BooleanSetting) ((BooleanSetting) s).toggle();
                            X4TweakerClient.getInstance().getConfigManager().save();
                            return;
                        }
                        setY += SET_H;
                    }
                }


                if (mouseButton == 0 && mouseX >= modX + 8 && mouseX <= modX + 60 && mouseY >= setY && mouseY <= setY + 12) {
                    for (Setting<?> s : m.getSettings()) s.reset();
                    X4TweakerClient.getInstance().getConfigManager().save();
                    return;
                }

                modY += animH + 4;
            }
        }
    }

    private void handleThemeTabClick(int mouseX, int mouseY, int mouseButton) {
        int modX = x + SIDEBAR_W + 6;
        int panelRight = x + ancho - 6;
        int listY = y + HEADER_H + 6 + (int)scrollY;
        ThemeManager tema = X4TweakerClient.getInstance().getThemeManager();

        if (themeSelectedIndex == -1) {
            for (int i = 0; i < tema.getColorCount(); i++) {
                if (mouseX >= modX && mouseX <= panelRight && mouseY >= listY && mouseY <= listY + 20) {
                    themeSelectedIndex = i;
                    java.awt.Color c = tema.getColorByIndex(i);
                    float[] hsb = java.awt.Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                    themeHue = hsb[0];
                    themeSat = hsb[1];
                    themeVal = hsb[2];
                    themeAlpha = c.getAlpha();
                    targetScrollY = 0;
                    scrollY = 0;
                    return;
                }
                listY += 22;
            }
            if (mouseButton == 0 && mouseX >= modX && mouseX <= modX + 80 && mouseY >= listY && mouseY <= listY + 15) {
                tema.loadDefaultTheme();
                tema.save();
                return;
            }
        } else {
            listY += 15;

            if (mouseX >= modX && mouseX <= modX + 160 && mouseY >= listY && mouseY <= listY + 100) {
                themeDraggingSatVal = true;
                return;
            }
            listY += 105;

            if (mouseX >= modX && mouseX <= modX + 160 && mouseY >= listY && mouseY <= listY + 10) {
                themeDraggingHue = true;
                return;
            }
            listY += 15;

            if (mouseX >= modX && mouseX <= modX + 160 && mouseY >= listY && mouseY <= listY + 10) {
                themeDraggingAlpha = true;
                return;
            }
            listY += 15;

            if (checkThemeInputClick(mouseX, mouseY, modX, listY, 0)) return;
            if (checkThemeInputClick(mouseX, mouseY, modX + 45, listY, 1)) return;
            if (checkThemeInputClick(mouseX, mouseY, modX + 90, listY, 2)) return;
            if (checkThemeInputClick(mouseX, mouseY, modX + 135, listY, 3)) return;

            listY += 20;

            if (mouseX >= modX && mouseX <= modX + 60 && mouseY >= listY && mouseY <= listY + 15) {
                activeInputIndex = -1;
                themeSelectedIndex = -1;
                tema.save();
                return;
            }
            activeInputIndex = -1;
        }
    }

    private boolean checkThemeInputClick(int mouseX, int mouseY, int xPos, int yPos, int index) {
        if (mouseX >= xPos + 12 && mouseX <= xPos + 38 && mouseY >= yPos && mouseY <= yPos + 14) {
            activeInputIndex = index;
            java.awt.Color c = X4TweakerClient.getInstance().getThemeManager().getColorByIndex(themeSelectedIndex);
            if (index == 0) inputR = String.valueOf(c.getRed());
            else if (index == 1) inputG = String.valueOf(c.getGreen());
            else if (index == 2) inputB = String.valueOf(c.getBlue());
            else if (index == 3) inputA = String.valueOf(c.getAlpha());
            return true;
        }
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isKeybindOverlayOpen) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                isKeybindOverlayOpen = false;
            } else {
                bindingKey = keyCode;
            }
            return;
        }

        if (focusedSetting instanceof StringListSetting) {
            if (keyCode == Keyboard.KEY_ESCAPE) { focusedSetting = null; return; }
            if (keyCode == Keyboard.KEY_RETURN) {
                if (!currentInput.isEmpty()) {
                    ((StringListSetting) focusedSetting).addString(currentInput);
                    X4TweakerClient.getInstance().getConfigManager().save();
                    currentInput = "";
                }
                focusedSetting = null;
                return;
            }
            if (keyCode == Keyboard.KEY_BACK) {
                if (!currentInput.isEmpty()) currentInput = currentInput.substring(0, currentInput.length() - 1);
                return;
            }
            if (net.minecraft.util.ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                currentInput += typedChar;
            }
            return;
        }

        if (themeSelectedIndex != -1 && activeInputIndex != -1) {
            if (keyCode == Keyboard.KEY_ESCAPE) { activeInputIndex = -1; return; }
            if (keyCode == Keyboard.KEY_RETURN) { applyThemeInput(); activeInputIndex = -1; return; }
            if (keyCode == Keyboard.KEY_BACK) {
                if (activeInputIndex == 0 && !inputR.isEmpty()) inputR = inputR.substring(0, inputR.length() - 1);
                else if (activeInputIndex == 1 && !inputG.isEmpty()) inputG = inputG.substring(0, inputG.length() - 1);
                else if (activeInputIndex == 2 && !inputB.isEmpty()) inputB = inputB.substring(0, inputB.length() - 1);
                else if (activeInputIndex == 3 && !inputA.isEmpty()) inputA = inputA.substring(0, inputA.length() - 1);
                return;
            }
            if (Character.isDigit(typedChar)) {
                if (activeInputIndex == 0 && inputR.length() < 3) inputR += typedChar;
                else if (activeInputIndex == 1 && inputG.length() < 3) inputG += typedChar;
                else if (activeInputIndex == 2 && inputB.length() < 3) inputB += typedChar;
                else if (activeInputIndex == 3 && inputA.length() < 3) inputA += typedChar;
            }
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    private void applyThemeInput() {
        try {
            int r = inputR.isEmpty() ? 0 : Integer.parseInt(inputR);
            int g = inputG.isEmpty() ? 0 : Integer.parseInt(inputG);
            int b = inputB.isEmpty() ? 0 : Integer.parseInt(inputB);
            int a = inputA.isEmpty() ? 0 : Integer.parseInt(inputA);
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));
            a = Math.max(0, Math.min(255, a));
            java.awt.Color c = new java.awt.Color(r, g, b, a);
            X4TweakerClient.getInstance().getThemeManager().setColorByIndex(themeSelectedIndex, c);
            float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
            themeHue = hsb[0];
            themeSat = hsb[1];
            themeVal = hsb[2];
            themeAlpha = a;
            X4TweakerClient.getInstance().getThemeManager().save();
        } catch (Exception ignored) {}
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggingSlider != null) {
            draggingSlider = null;
            X4TweakerClient.getInstance().getConfigManager().save();
        }
        if (themeDraggingHue || themeDraggingSatVal || themeDraggingAlpha) {
            themeDraggingHue = false;
            themeDraggingSatVal = false;
            themeDraggingAlpha = false;
            X4TweakerClient.getInstance().getThemeManager().save();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onGuiClosed() {
        draggingSlider = null;
        Keyboard.enableRepeatEvents(false);
        X4TweakerClient.getInstance().getConfigManager().save();
    }

    private String getModuleDisplayName(Module module) {
        String key = "x4tweaker.module." + normalizeKey(module.getName()) + ".name";
        return I18n.hasKey(key) ? I18n.format(key) : module.getName();
    }

    private String getSettingDisplayName(Module module, Setting<?> setting) {
        String key = "x4tweaker.setting." + normalizeKey(module.getName()) + "." + normalizeKey(setting.getName()) + ".name";
        return I18n.hasKey(key) ? I18n.format(key) : setting.getName();
    }

    private String normalizeKey(String input) {
        String normalized = input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        while (normalized.startsWith("_")) normalized = normalized.substring(1);
        while (normalized.endsWith("_")) normalized = normalized.substring(0, normalized.length() - 1);
        return normalized;
    }

    private void drawMigrationNotice(int mouseX, int mouseY) {
        if (!showMigrationNotice || migrationNotices.isEmpty()) return;

        int bx1 = x + SIDEBAR_W + 6;
        int by1 = y + HEADER_H + 6;
        int bx2 = x + ancho - 8;
        int by2 = by1 + 78;

        RenderUtils.dibujarRect(bx1, by1, bx2, by2, 0xCC101010);
        RenderUtils.dibujarRectBordeado(bx1, by1, bx2, by2, 1.0f, 0xFF3A9D5D, 0x00000000);
        mc.fontRenderer.drawStringWithShadow("Config actualizada - revisa ajustes", bx1 + 6, by1 + 6, 0xFFFFFFFF);

        int closeX = bx2 - 16;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + 12 && mouseY >= by1 + 4 && mouseY <= by1 + 16;
        mc.fontRenderer.drawStringWithShadow("x", closeX, by1 + 6, closeHover ? 0xFFFF6666 : 0xFFBBBBBB);

        int maxLines = 5;
        int baseY = by1 + 20;
        for (int i = 0; i < maxLines; i++) {
            int idx = i + migrationScroll;
            if (idx >= migrationNotices.size()) break;
            String msg = migrationNotices.get(idx);
            if (msg.length() > 46) msg = msg.substring(0, 43) + "...";
            mc.fontRenderer.drawStringWithShadow("- " + msg, bx1 + 8, baseY + i * 11, 0xFFD6D6D6);
        }

        if (migrationMaxScroll > 0) {
            String footer = (migrationScroll + 1) + "/" + (migrationMaxScroll + 1);
            mc.fontRenderer.drawStringWithShadow(footer, bx2 - mc.fontRenderer.getStringWidth(footer) - 6, by2 - 10, 0xFFAAAAAA);
        }
    }
}
