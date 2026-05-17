package com.x4yi.x4tweaker.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.entity.EntityPlayerSP;

public class LegitAttackOrchestrator implements IAttackOrchestrator {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private float aimThreshold;

    public LegitAttackOrchestrator() {
        this(15.0f);
    }

    public LegitAttackOrchestrator(float aimThreshold) {
        this.aimThreshold = Math.max(0.1f, Math.min(180.0f, aimThreshold));
    }

    @Override
    public void attack(EntityPlayerSP player, EntityLivingBase target) {
        if (mc.playerController == null) return;
        mc.playerController.attackEntity(player, target);
    }

    @Override
    public boolean canAttack(EntityPlayerSP player) {
        return player.getCooledAttackStrength(0.5F) >= 1.0F;
    }

    @Override
    public void onUpdate(EntityPlayerSP player) {
    }

    @Override
    public void reset() {
    }

    @Override
    public float getAimThreshold() {
        return aimThreshold;
    }

    public void setAimThreshold(float threshold) {
        this.aimThreshold = Math.max(0.1f, Math.min(180.0f, threshold));
    }
}
