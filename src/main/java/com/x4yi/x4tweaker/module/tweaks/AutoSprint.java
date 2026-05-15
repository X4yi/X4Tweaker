package com.x4yi.x4tweaker.module.tweaks;

import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;

public class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint", "Sprints automatically", Category.TWEAKS);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.moveForward > 0 && !mc.player.isSneaking() && !mc.player.collidedHorizontally && mc.player.getFoodStats().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
