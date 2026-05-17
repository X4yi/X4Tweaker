package com.x4yi.x4tweaker.automation.context;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class PlayerStateSnapshot {
    private boolean captured;
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
    private int hotbarSlot;
    private boolean sprinting;

    public void capture(Minecraft mc) {
        EntityPlayerSP player = mc.player;
        if (player == null) return;
        captured = true;
        posX = player.posX;
        posY = player.posY;
        posZ = player.posZ;
        yaw = player.rotationYaw;
        pitch = player.rotationPitch;
        hotbarSlot = player.inventory.currentItem;
        sprinting = player.isSprinting();
    }

    public void restore(Minecraft mc) {
        if (!captured) return;
        EntityPlayerSP player = mc.player;
        if (player == null) return;
        player.rotationYaw = yaw;
        player.rotationPitch = pitch;
        player.inventory.currentItem = hotbarSlot;
        player.setSprinting(sprinting);
    }

    public boolean isCaptured() {
        return captured;
    }

    public double getPosX() { return posX; }
    public double getPosY() { return posY; }
    public double getPosZ() { return posZ; }
}
