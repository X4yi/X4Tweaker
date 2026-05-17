package com.x4yi.x4tweaker.module.visuals;

import com.x4yi.x4tweaker.utils.camera.FakeCameraEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerESP extends ESPBase {
    private final List<Entity> cachedEntities = new ArrayList<>();

    public PlayerESP() {
        super("PlayerESP", "Dibuja cajas alrededor de jugadores");
    }

    @Override
    protected List<Entity> getEntities() {
        cachedEntities.clear();
        if (mc.world == null) return cachedEntities;
        List<Entity> loaded = mc.world.loadedEntityList;
        for (int i = 0, size = loaded.size(); i < size; i++) {
            Entity e = loaded.get(i);
            if (e instanceof EntityPlayer && e != mc.player && !(e instanceof FakeCameraEntity)) cachedEntities.add(e);
        }
        return cachedEntities;
    }

    @Override
    protected void fillColor(float[] out) {
        out[0] = 1.0f; out[1] = 0.0f; out[2] = 0.0f; out[3] = 1.0f;
    }
}
