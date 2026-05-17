package com.x4yi.x4tweaker.combat;


public final class AttackOrchestrator {
    private AttackOrchestrator() {
    }

    public static LegitAttackOrchestrator createLegit(float aimThreshold) {
        return new LegitAttackOrchestrator(aimThreshold);
    }

    public static LegitAttackOrchestrator createLegit() {
        return new LegitAttackOrchestrator();
    }
}
