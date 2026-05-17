package com.x4yi.x4tweaker.module;

import com.x4yi.x4tweaker.event.Event;
import com.x4yi.x4tweaker.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    public enum ReleaseState {
        STABLE,
        EXPERIMENTAL,
        COMING_SOON
    }

    private final String name;
    private final String description;
    private final Category category;

    private boolean enabled;
    private boolean visible    = true;
    private boolean expanded   = false;
    private ReleaseState releaseState = ReleaseState.STABLE;
    private int keybind        = Keyboard.KEY_NONE;
    private float expandProgress = 0.0f;

    private final List<Setting<?>> settings = new ArrayList<>();

    protected static final Minecraft mc = Minecraft.getMinecraft();

    public Module(String name, String description, Category category) {
        this.name        = name;
        this.description = description;
        this.category    = category;
    }

    protected void addSetting(Setting<?> setting) {
        setting.setOwner(this);
        settings.add(setting);
    }

    public void toggle() {
        if (enabled) disable(); else enable();
    }

    public void enable() {
        if (!isImplemented()) return;
        if (enabled) return;
        try {
            enabled = true;
            MinecraftForge.EVENT_BUS.register(this);
            com.x4yi.x4tweaker.core.X4TweakerClient.getInstance().getModuleManager().onModuleEnabled(this);
            onEnable();
        } catch (Exception e) {
            enabled = false;
            try { MinecraftForge.EVENT_BUS.unregister(this); } catch (Exception ignored) {}
            System.err.println("[X4Tweaker] Error activando módulo '" + name + "': " + e.getMessage());
        }
    }

    public void disable() {
        if (!enabled) return;
        try {
            enabled = false;
            MinecraftForge.EVENT_BUS.unregister(this);
            onDisable();
        } catch (Exception e) {
            System.err.println("[X4Tweaker] Error desactivando módulo '" + name + "': " + e.getMessage());
        }
    }

    public List<Class<? extends Module>> getIncompatibilities() {
        return Collections.emptyList();
    }

    public void onEnable()  {}
    public void onDisable() {}
    public void onUpdate()  {}
    public void onRender2D(){}
    public void onRender3D(){}
    public void onEvent(Event event) {}
    public void onSettingChanged() {}

    public String   getName()        { return name; }
    public String   getDescription() { return description; }
    public Category getCategory()    { return category; }
    public boolean  isEnabled()      { return enabled; }

    public void setEnabled(boolean value) {
        if (enabled != value) toggle();
    }


    protected void setDefaultEnabled(boolean value) {
        this.enabled = value;
    }

    public boolean isVisible()              { return visible; }
    public void setVisible(boolean v)       { visible = v; }
    public boolean isExpanded()             { return expanded; }
    public void setExpanded(boolean v)      { expanded = v; }
    public boolean isImplemented()          { return releaseState != ReleaseState.COMING_SOON; }
    public void setImplemented(boolean v)   { releaseState = v ? ReleaseState.STABLE : ReleaseState.COMING_SOON; }
    public ReleaseState getReleaseState()   { return releaseState; }
    public void setReleaseState(ReleaseState state) {
        if (state != null) {
            releaseState = state;
        }
    }
    public int  getKeybind()                { return keybind; }
    public void setKeybind(int k)           { keybind = k; }
    public float getExpandProgress()        { return expandProgress; }
    public void setExpandProgress(float v)  { expandProgress = v; }
    public List<Setting<?>> getSettings()   { return settings; }
}
