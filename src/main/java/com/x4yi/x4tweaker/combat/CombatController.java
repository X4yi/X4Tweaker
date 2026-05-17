package com.x4yi.x4tweaker.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.entity.EntityPlayerSP;


public class CombatController {
    private final TargetSelector targetSelector;
    private final RotationCalculator rotationCalculator;
    private final IAttackOrchestrator attackOrchestrator;

    private EntityLivingBase currentTarget = null;

    public CombatController(TargetSelector targetSelector,
                           RotationCalculator rotationCalculator,
                           IAttackOrchestrator attackOrchestrator) {
        this.targetSelector = targetSelector;
        this.rotationCalculator = rotationCalculator;
        this.attackOrchestrator = attackOrchestrator;
    }


    public boolean updateAndAttack(EntityPlayerSP player) {
        if (player == null) return false;

        attackOrchestrator.onUpdate(player);

        EntityLivingBase newTarget = targetSelector.findTarget();
        if (newTarget != currentTarget) {
            currentTarget = newTarget;
            rotationCalculator.reset();
        }

        if (currentTarget == null) {
            return false;
        }

        if (currentTarget.isDead || currentTarget.getHealth() <= 0) {
            currentTarget = null;
            return false;
        }

        float[] rotationDiff = rotationCalculator.applySmoothRotation(player, currentTarget);

        float threshold = attackOrchestrator.getAimThreshold();
        if (attackOrchestrator.canAttack(player)
                && Math.abs(rotationDiff[0]) < threshold
                && Math.abs(rotationDiff[1]) < threshold) {
            attackOrchestrator.attack(player, currentTarget);
            return true;
        }

        return false;
    }

    public EntityLivingBase getCurrentTarget() {
        return currentTarget;
    }

    public void stop() {
        currentTarget = null;
        rotationCalculator.reset();
        attackOrchestrator.reset();
    }
}
