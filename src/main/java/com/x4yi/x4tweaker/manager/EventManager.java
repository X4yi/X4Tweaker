package com.x4yi.x4tweaker.manager;

import com.x4yi.x4tweaker.event.*;
import com.x4yi.x4tweaker.core.PacketHandler;
import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.gui.ClickGUI;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

public class EventManager {
    private static final int GUI_BUTTON_ID = 4444;

    private final ModuleEventDispatcher moduleEventDispatcher = new ModuleEventDispatcher();
    private boolean packetHandlerInjected = false;

    public EventManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        packetHandlerInjected = false;
    }

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiInventory) {
            event.getButtonList().add(new GuiButton(GUI_BUTTON_ID, 5, event.getGui().height - 25, 30, 20, "[X4]"));
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.getGui() instanceof GuiInventory && event.getButton().id == GUI_BUTTON_ID) {
            Minecraft.getMinecraft().displayGuiScreen(new ClickGUI());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        injectPacketHandler();

        UpdateEvent updateEvent = new UpdateEvent();
        moduleEventDispatcher.dispatchUpdate(updateEvent);
        X4TweakerClient.getInstance().getAutomationManager().onUpdate();
    }

    @SubscribeEvent
    public void onRender2D(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;
        moduleEventDispatcher.dispatchRender2D(new Render2DEvent(event.getPartialTicks()));
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        moduleEventDispatcher.dispatchRender3D(new Render3DEvent(event.getPartialTicks()));
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState()) return;
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_NONE) return;
        X4TweakerClient.getInstance().getKeybindManager().handleKey(key);
        moduleEventDispatcher.dispatchEventOnly(new KeyEvent(key));
    }

    private void injectPacketHandler() {
        if (packetHandlerInjected) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.player.connection == null) return;

        try {
            Channel channel = mc.player.connection.getNetworkManager().channel();
            if (channel != null && channel.pipeline().get(PacketHandler.getHandlerName()) == null) {
                channel.pipeline().addBefore("packet_handler", PacketHandler.getHandlerName(),
                    new PacketHandler(moduleEventDispatcher));
                packetHandlerInjected = true;
            }
        } catch (Exception e) {
            System.err.println("[X4Tweaker] Error inyectando PacketHandler: " + e.getMessage());
        }
    }

    public ModuleEventDispatcher getModuleEventDispatcher() {
        return moduleEventDispatcher;
    }
}
