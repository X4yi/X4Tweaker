package com.x4yi.x4tweaker.gui;

import com.x4yi.x4tweaker.X4Tweaker;
import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class HUDOverlay extends Gui {
    private final Minecraft mc = Minecraft.getMinecraft();

    public HUDOverlay() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;

        mc.fontRenderer.drawStringWithShadow(X4Tweaker.NAME + " v" + X4Tweaker.VERSION, 2, 2, 0xFF00FF00);

        mc.fontRenderer.drawStringWithShadow("FPS: " + Minecraft.getDebugFPS(), 2, 12, 0xFFFFFFFF);
        if (mc.player != null) {
            String coords = String.format("XYZ: %.1f %.1f %.1f", mc.player.posX, mc.player.posY, mc.player.posZ);
            mc.fontRenderer.drawStringWithShadow(coords, 2, 22, 0xFFFFFFFF);
        }
        List<Module> activeModules = new ArrayList<>();
        for (Module m : X4TweakerClient.getInstance().getModuleManager().getModules()) {
            if (m.isEnabled() && m.isVisible()) {
                activeModules.add(m);
            }
        }

        activeModules.sort((m1, m2) -> Integer.compare(mc.fontRenderer.getStringWidth(m2.getName()), mc.fontRenderer.getStringWidth(m1.getName())));

        int y = 2;
        int screenWidth = event.getResolution().getScaledWidth();
        for (Module m : activeModules) {
            int width = mc.fontRenderer.getStringWidth(m.getName());
            mc.fontRenderer.drawStringWithShadow(m.getName(), screenWidth - width - 2, y, X4TweakerClient.getInstance().getThemeManager().getColorToggleEncendido().getRGB());
            y += 10;
        }
    }
}
