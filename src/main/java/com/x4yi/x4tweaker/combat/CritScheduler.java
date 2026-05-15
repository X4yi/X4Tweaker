package com.x4yi.x4tweaker.combat;

import net.minecraft.client.entity.EntityPlayerSP;

public class CritScheduler {

    public enum CritPhase {
        IDLE,
        WAITING_COOLDOWN,
        JUMP,
        ASCENDING,
        DESCENDING
    }

    private CritPhase phase = CritPhase.IDLE;
    private int phaseTicks = 0;

    private static final int MAX_ASCENDING_TICKS = 7;
    private static final int MAX_DESCENDING_TICKS = 6;

    public void tick(EntityPlayerSP player) {
        if (player == null) return;
        phaseTicks++;

        switch (phase) {
            case IDLE:
                if (player.getCooledAttackStrength(0.5F) >= 0.85F) {
                    phase = CritPhase.WAITING_COOLDOWN;
                    phaseTicks = 0;
                }
                break;

            case WAITING_COOLDOWN:
                if (player.getCooledAttackStrength(0.5F) >= 1.0F && player.onGround) {
                    phase = CritPhase.JUMP;
                    phaseTicks = 0;
                }
                break;

            case JUMP:
                phase = CritPhase.ASCENDING;
                phaseTicks = 0;
                break;

            case ASCENDING:
                if (player.motionY <= 0 && !player.onGround) {
                    phase = CritPhase.DESCENDING;
                    phaseTicks = 0;
                } else if (phaseTicks > MAX_ASCENDING_TICKS || player.onGround) {
                    phase = CritPhase.IDLE;
                    phaseTicks = 0;
                }
                break;

            case DESCENDING:
                if (player.onGround || phaseTicks > MAX_DESCENDING_TICKS) {
                    phase = CritPhase.IDLE;
                    phaseTicks = 0;
                }
                break;
        }
    }

    public boolean shouldJump(EntityPlayerSP player) {
        if (player == null) return false;
        return phase == CritPhase.JUMP;
    }

    public boolean shouldAttackNow(EntityPlayerSP player) {
        if (player == null) return false;
        if (phase != CritPhase.DESCENDING) return false;

        return player.motionY < 0
            && !player.onGround
            && !player.isInWater()
            && !player.isOnLadder()
            && player.getCooledAttackStrength(0.5F) >= 0.9F;
    }

    public boolean isInCritCycle() {
        return phase == CritPhase.JUMP
            || phase == CritPhase.ASCENDING
            || phase == CritPhase.DESCENDING;
    }

    public CritPhase getPhase() {
        return phase;
    }

    public void reset() {
        phase = CritPhase.IDLE;
        phaseTicks = 0;
    }
}
