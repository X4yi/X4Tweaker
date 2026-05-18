package com.x4yi.x4tweaker.gui.v2.clickgui;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.gui.v2.framework.GuiComponent;
import com.x4yi.x4tweaker.gui.v2.framework.AnimationHelper;
import com.x4yi.x4tweaker.gui.v2.framework.ThemeBridge;
import com.x4yi.x4tweaker.gui.v2.utils.DrawHelper;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.module.client.ClickGUIModule;
import com.x4yi.x4tweaker.utils.I18nUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.List;

public class ModuleRow implements GuiComponent {
    private static final int HEIGHT = 22;
    private static final int TOGGLE_W = 26;

    private int x, y, width, height;
    private boolean visible = true;
    private final Module module;
    private final ThemeBridge theme;
    private final Minecraft mc;
    private boolean hovered = false;
    private float expandAnim = 0;
    private int priority = 0;

    public ModuleRow(Module module, ThemeBridge theme) {
        this.module = module;
        this.theme = theme;
        this.mc = Minecraft.getMinecraft();
        this.height = HEIGHT;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        float target = module.isExpanded() ? 1.0f : 0.0f;
        expandAnim = AnimationHelper.lerp(expandAnim, target, 0.18f);
        hovered = contains(mouseX, mouseY);

        int bgColor = hovered ? theme.getSurfaceHoverColor() : theme.getSurfaceColor();
        if (!module.isImplemented()) bgColor = 0x11FFFFFF;
        DrawHelper.drawRect(x, y, x + width, y + HEIGHT, bgColor);
        DrawHelper.drawBorderedRect(x, y, x + width, y + HEIGHT, 1.0f, theme.getSeparatorColor(), 0x00000000);

        if (module.isImplemented() && !module.getSettings().isEmpty()) {
            String arrow = expandAnim > 0.5f ? "\u25BC" : "\u25B6";
            mc.fontRenderer.drawStringWithShadow(arrow, x + 4, y + 7, 0xFF888888);
        }

        int textColor = !module.isImplemented() ? 0xFF666666 :
            (module.getReleaseState() == Module.ReleaseState.EXPERIMENTAL ? 0xFFFFD27F : 0xFFEEEEEE);
        mc.fontRenderer.drawStringWithShadow(getDisplayName(), x + 16, y + 7, textColor);

        String badge = getBadge();
        if (!badge.isEmpty()) {
            int bw = mc.fontRenderer.getStringWidth(badge);
            mc.fontRenderer.drawStringWithShadow(badge, x + width - bw - TOGGLE_W - 6, y + 7, getStatusColor());
        }

        if (module.isImplemented()) {
            boolean on = module.isEnabled();
            String toggleTxt = on ? "ON" : "OFF";
            int tw = mc.fontRenderer.getStringWidth(toggleTxt) + 8;
            int toggleX = x + width - tw - 4;
            int toggleBg = on ? theme.getEnabledColor() : theme.getDisabledColor();
            int toggleBorder = on ? theme.getEnabledDarkColor() : theme.getDisabledDarkColor();
            DrawHelper.drawBorderedRect(toggleX, y + 4, x + width - 4, y + HEIGHT - 4, 1.0f, toggleBorder, toggleBg);
            mc.fontRenderer.drawStringWithShadow(toggleTxt, toggleX + 4, y + 7, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int button) {
        if (!visible || !contains(mouseX, mouseY)) return false;
        if (!module.isImplemented()) return false;

        if (button == 0) {
            boolean on = module.isEnabled();
            String toggleTxt = on ? "ON" : "OFF";
            int tw = mc.fontRenderer.getStringWidth(toggleTxt) + 8;
            int toggleX = x + width - tw - 4;
            if (mouseX >= toggleX && mouseX <= x + width - 4 && mouseY >= y + 4 && mouseY <= y + HEIGHT - 4) {
                module.toggle();
                X4TweakerClient.getInstance().getConfigManager().save();
                return true;
            }
            module.toggle();
            X4TweakerClient.getInstance().getConfigManager().save();
            return true;
        }

        if (button == 1 && !module.getSettings().isEmpty()) {
            module.setExpanded(!module.isExpanded());
            X4TweakerClient.getInstance().getConfigManager().save();
            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int button) { return false; }
    @Override
    public boolean onMouseMove(int mouseX, int mouseY, int dx, int dy) { return false; }
    @Override
    public boolean onKey(char typedChar, int keyCode) { return false; }
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
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEIGHT;
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
    public int getPriority() { return priority; }
    @Override
    public void setPriority(int p) { this.priority = p; }

    public Module getModule() { return module; }
    public boolean isHovered() { return hovered; }
    public float getExpandAnim() { return expandAnim; }

    public boolean shouldBeHidden() {
        return module instanceof ClickGUIModule;
    }

    private String getDisplayName() {
        String key = "x4tweaker.module." + normalizeKey(module.getName()) + ".name";
        return I18n.hasKey(key) ? I18n.format(key) : module.getName();
    }

    private String getBadge() {
        if (module.getReleaseState() == Module.ReleaseState.EXPERIMENTAL) return "[EXP]";
        if (module.getReleaseState() == Module.ReleaseState.COMING_SOON) return "[SOON]";
        return "";
    }

    private int getStatusColor() {
        if (module.getReleaseState() == Module.ReleaseState.EXPERIMENTAL) return 0xFFFFB347;
        if (module.getReleaseState() == Module.ReleaseState.COMING_SOON) return 0xFFFF6666;
        return 0xFFFFFFFF;
    }

    private String normalizeKey(String input) {
        return I18nUtils.normalizeKey(input);
    }

    public List<String> getTooltipLines() {
        List<String> lines = new ArrayList<String>();
        String moduleKey = normalizeKey(module.getName());
        String descKey = "x4tweaker.module." + moduleKey + ".desc";
        String baseDesc = I18n.hasKey(descKey) ? I18n.format(descKey) : module.getDescription();
        if (baseDesc == null || baseDesc.isEmpty()) baseDesc = module.getDescription();

        if (module.getReleaseState() == Module.ReleaseState.COMING_SOON) {
            lines.add("Coming Soon");
        } else if (module.getReleaseState() == Module.ReleaseState.EXPERIMENTAL) {
            lines.add("Experimental");
            lines.add(baseDesc);
            lines.add("May change without notice.");
        } else {
            lines.add(baseDesc);
        }

        List<String> incompats = getIncompatibilityNames();
        if (!incompats.isEmpty()) {
            StringBuilder sb = new StringBuilder("Incompatible: ");
            for (int i = 0; i < incompats.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(incompats.get(i));
            }
            lines.add(sb.toString());
        }
        return lines;
    }

    private List<String> getIncompatibilityNames() {
        List<String> names = new ArrayList<String>();
        List<Class<? extends Module>> list = module.getIncompatibilities();
        if (list == null || list.isEmpty()) return names;
        for (int i = 0; i < list.size(); i++) {
            Module target = X4TweakerClient.getInstance().getModuleManager().getModule(list.get(i));
            if (target == null) continue;
            String tKey = "x4tweaker.module." + normalizeKey(target.getName()) + ".name";
            names.add(I18n.hasKey(tKey) ? I18n.format(tKey) : target.getName());
        }
        return names;
    }
}
