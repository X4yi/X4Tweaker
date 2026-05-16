package com.x4yi.x4tweaker.module.visuals;

import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.manager.ThemeManager;
import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.module.bots.BotModule;
import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.ModeSetting;
import com.x4yi.x4tweaker.utils.I18nUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ActiveTweaks extends Module {
    private final ModeSetting position = new ModeSetting("HUD Position", "Posición del HUD en pantalla", "Top Left", "Top Left", "Top Right", "Bottom Left", "Bottom Right");
    private final BooleanSetting showLogo = new BooleanSetting("Show Title", "Mostrar nombre X4Tweaker en el HUD", true);

    public ActiveTweaks() {
        super("ActiveTweaks", "Muestra los módulos activos en pantalla", Category.UTILITY);
        addSetting(position);
        addSetting(showLogo);
        setDefaultEnabled(true);
    }

    @Override
    public void onRender2D() {
        if (mc.player == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        ThemeManager theme = X4TweakerClient.getInstance().getThemeManager();

        List<Module> activeModules = new ArrayList<>();
        List<BotStatusEntry> activeBots = new ArrayList<>();

        List<Module> allModules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = allModules.size(); i < size; i++) {
            Module m = allModules.get(i);
            if (!m.isEnabled() || !m.isVisible() || m == this) continue;
            activeModules.add(m);
            if (m instanceof BotModule) {
                activeBots.add(new BotStatusEntry(m, (BotModule) m));
            }
        }

        activeModules.sort(Comparator.comparingInt(m -> -mc.fontRenderer.getStringWidth(getDisplayName(m))));

        String pos = position.getValue();
        boolean isRight  = pos.contains("Right");
        boolean isBottom = pos.contains("Bottom");
        int yOffset = isBottom ? sr.getScaledHeight() - 2 : 2;

        if (showLogo.getValue()) {
            String logo = "X4Tweaker";
            int width = mc.fontRenderer.getStringWidth(logo);
            int x = isRight ? sr.getScaledWidth() - width - 2 : 2;
            int renderY = isBottom ? yOffset - 10 : yOffset;
            mc.fontRenderer.drawStringWithShadow(logo, x, renderY, theme.getColorBotonNormal().getRGB());
            yOffset = isBottom ? yOffset - 12 : yOffset + 12;
        }

        int activeColor = theme.getColorToggleEncendido().getRGB();
        for (int i = 0, size = activeModules.size(); i < size; i++) {
            Module m = activeModules.get(i);
            String name = getDisplayName(m);
            int width = mc.fontRenderer.getStringWidth(name);
            int x = isRight ? sr.getScaledWidth() - width - 2 : 2;
            int renderY = isBottom ? yOffset - 10 : yOffset;
            mc.fontRenderer.drawStringWithShadow(name, x, renderY, activeColor);
            yOffset = isBottom ? yOffset - 10 : yOffset + 10;
        }

        for (int i = 0, size = activeBots.size(); i < size; i++) {
            BotStatusEntry entry = activeBots.get(i);
            String line = getDisplayName(entry.module) + " [" + entry.botModule.getBotStatus() + "]";
            int width = mc.fontRenderer.getStringWidth(line);
            int x = isRight ? sr.getScaledWidth() - width - 2 : 2;
            int renderY = isBottom ? yOffset - 10 : yOffset;
            mc.fontRenderer.drawStringWithShadow(line, x, renderY, 0xFF88CCFF);
            yOffset = isBottom ? yOffset - 10 : yOffset + 10;
        }
    }

    private String getDisplayName(Module module) {
        String key = "x4tweaker.module." + normalizeKey(module.getName()) + ".name";
        return I18n.hasKey(key) ? I18n.format(key) : module.getName();
    }

    private String normalizeKey(String input) {
        return I18nUtils.normalizeKey(input);
    }

    private static final class BotStatusEntry {
        final Module module;
        final BotModule botModule;

        BotStatusEntry(Module module, BotModule botModule) {
            this.module    = module;
            this.botModule = botModule;
        }
    }
}
