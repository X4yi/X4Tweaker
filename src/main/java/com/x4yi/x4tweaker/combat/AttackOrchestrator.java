package com.x4yi.x4tweaker.combat;


/**
 * Factory class for attack orchestrators.
 * Currently only provides LegitAttackOrchestrator, but serves as an extension point
 * for future bot intelligence (e.g., RageAttackOrchestrator, PacketAttackOrchestrator,
 * or advanced defending logic for BotAFK).
 */
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
