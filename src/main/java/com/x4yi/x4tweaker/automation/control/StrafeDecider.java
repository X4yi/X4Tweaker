package com.x4yi.x4tweaker.automation.control;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;

public class StrafeDecider {

    public enum StrafeDirection {
        NONE, LEFT, RIGHT
    }

    private StrafeDirection lastDirection = StrafeDirection.LEFT;
    private int ticksSinceLastChange = 0;

    public StrafeDirection evaluate(EntityPlayerSP player, EntityLivingBase target,
                                     EnvironmentScanner scanner, int strafeInterval,
                                     boolean avoidMobsInPath) {

        ticksSinceLastChange++;

        boolean obstacleAhead = scanner.hasObstacleBetween(player, target);
        if (obstacleAhead) {
            return findBestSide(player, scanner);
        }

        boolean needsJump = scanner.needsJump(player, 0);
        if (needsJump) {
            boolean leftClear = scanner.isPathClear(player, -90, 2.0);
            boolean rightClear = scanner.isPathClear(player, 90, 2.0);
            if (leftClear && !rightClear) return StrafeDirection.LEFT;
            if (rightClear && !leftClear) return StrafeDirection.RIGHT;
            if (leftClear) return StrafeDirection.LEFT;
            return StrafeDirection.NONE;
        }

        if (avoidMobsInPath) {
            boolean hasMobsInPath = !scanner.getEntitiesBetween(player, target, 1.5).isEmpty();
            if (hasMobsInPath) {
                return findBestSide(player, scanner);
            }
        }

        if (ticksSinceLastChange >= strafeInterval) {
            ticksSinceLastChange = 0;
            lastDirection = (lastDirection == StrafeDirection.LEFT)
                ? StrafeDirection.RIGHT : StrafeDirection.LEFT;

            float yawOffset = (lastDirection == StrafeDirection.LEFT) ? -90 : 90;
            if (!scanner.isPathClear(player, yawOffset, 1.5)) {
                lastDirection = (lastDirection == StrafeDirection.LEFT)
                    ? StrafeDirection.RIGHT : StrafeDirection.LEFT;
            }
            return lastDirection;
        }

        return lastDirection;
    }

    private StrafeDirection findBestSide(EntityPlayerSP player, EnvironmentScanner scanner) {
        boolean leftClear = scanner.isPathClear(player, -90, 2.0);
        boolean rightClear = scanner.isPathClear(player, 90, 2.0);

        if (leftClear && !rightClear) return StrafeDirection.LEFT;
        if (rightClear && !leftClear) return StrafeDirection.RIGHT;
        if (leftClear) return lastDirection;
        return StrafeDirection.NONE;
    }

    public void reset() {
        lastDirection = StrafeDirection.LEFT;
        ticksSinceLastChange = 0;
    }
}
