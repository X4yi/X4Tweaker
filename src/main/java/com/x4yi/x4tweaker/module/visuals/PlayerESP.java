package com.x4yi.x4tweaker.module.visuals;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class PlayerESP extends ESPBase {
    public PlayerESP() {
        super("PlayerESP", "Dibuja cajas alrededor de jugadores");
    }

    @Override
    protected List<Entity> getEntities() {
        List<Entity> players = new ArrayList<>();
        if (mc.world == null) return players;
        List<Entity> loaded = mc.world.loadedEntityList;
        for (int i = 0, size = loaded.size(); i < size; i++) {
            Entity e = loaded.get(i);
            if (e instanceof EntityPlayer && e != mc.player) players.add(e);
        }
        return players;
    }

    @Override
    protected float[] getColor() {
        return new float[]{1.0f, 0.0f, 0.0f, 1.0f};
    }
}
