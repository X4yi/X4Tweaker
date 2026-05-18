package com.x4yi.x4tweaker.module.client;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.gui.v2.clickgui.ClickGUI;
import org.lwjgl.input.Keyboard;

public class ClickGUIModule extends Module {
    private ClickGUI clickGUI;

    public ClickGUIModule() {
        super("ClickGUI", "Abre esta interfaz de configuración", Category.UTILITY);
        setKeybind(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        if (clickGUI == null) {
            clickGUI = new ClickGUI();
        }
        mc.displayGuiScreen(clickGUI);
        setEnabled(false);
    }
}
