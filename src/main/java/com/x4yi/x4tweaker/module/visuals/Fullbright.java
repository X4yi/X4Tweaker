package com.x4yi.x4tweaker.module.visuals;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;

public class Fullbright extends Module {
    private float oldGamma;

    public Fullbright() {
        super("Fullbright", "Increases brightness", Category.VISUALS);
    }

    @Override
    public void onEnable() {
        if (mc.gameSettings != null) {
            oldGamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 100f;
        }
    }

    @Override
    public void onDisable() {
        if (mc.gameSettings != null) {
            mc.gameSettings.gammaSetting = oldGamma;
        }
    }
}
