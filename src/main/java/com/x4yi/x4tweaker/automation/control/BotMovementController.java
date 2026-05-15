package com.x4yi.x4tweaker.automation.control;

import com.x4yi.x4tweaker.combat.CritScheduler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class BotMovementController {
    private final KeybindDriver driver;
    private final EnvironmentScanner scanner;
    private final StrafeDecider strafeDecider;
    private final CritScheduler critScheduler;

    private boolean active = false;

    private double lastPosX, lastPosZ;
    private int stuckTicks = 0;
    private int stuckResolvePhase = 0;
    private int resolveTicksRemaining = 0;
    private float resolveYawOffset = 0;

    private static final double STUCK_THRESHOLD = 0.05;
    private static final int STUCK_DETECT_TICKS = 10;
    private static final int RESOLVE_PHASE_DURATION = 20;

    public BotMovementController() {
        this.driver = new KeybindDriver();
        this.scanner = new EnvironmentScanner();
        this.strafeDecider = new StrafeDecider();
        this.critScheduler = new CritScheduler();
    }

    public void moveToward(EntityPlayerSP player, double targetX, double targetZ, MovementConfig config) {
        active = true;

        if (resolveTicksRemaining > 0) {
            executeStuckResolve(player, config);
            updateStuckDetection(player);
            return;
        }

        updateStuckDetection(player);

        if (stuckTicks >= STUCK_DETECT_TICKS) {
            startStuckResolve(player);
            return;
        }

        facePosition(player, targetX, targetZ, 3.0f);

        if (scanner.hasDangerousDropAhead(player, 0, 2.0)) {
            float bestOffset = scanner.findBestYawOffset(player, 4.0);
            if (bestOffset != 0) {
                player.rotationYaw += bestOffset / 3.0f;
            }
            driver.setForward(true);
            driver.setJump(false);
            return;
        }

        boolean pathClear = scanner.isPathClear(player, 0, 2.0);

        if (!pathClear) {
            boolean canJump = scanner.needsJump(player, 0);

            if (canJump && config.isJumpEnabled() && player.onGround) {
                driver.setJump(true);
                driver.setForward(true);
            } else {
                float bestOffset = scanner.findBestYawOffset(player, 4.0);
                if (bestOffset < 0) {
                    driver.setStrafeLeft(true);
                    driver.setStrafeRight(false);
                } else if (bestOffset > 0) {
                    driver.setStrafeRight(true);
                    driver.setStrafeLeft(false);
                }
                driver.setForward(true);

                if (config.isJumpEnabled() && player.onGround && scanner.needsJump(player, bestOffset)) {
                    driver.setJump(true);
                } else {
                    driver.setJump(false);
                }
            }
        } else {
            driver.setForward(true);
            driver.setStrafeLeft(false);
            driver.setStrafeRight(false);

            if (scanner.needsJump(player, 0) && config.isJumpEnabled() && player.onGround) {
                driver.setJump(true);
            } else {
                driver.setJump(false);
            }
        }

        double dx = targetX - player.posX;
        double dz = targetZ - player.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (config.isSprintEnabled() && dist > 3.0) {
            driver.setSprint(true);
        } else {
            driver.setSprint(false);
        }
    }

    public void maintainCombatDistance(EntityPlayerSP player, EntityLivingBase target, MovementConfig config) {
        active = true;

        critScheduler.tick(player);

        double dist = player.getDistance(target);
        double range = config.getAttackRange();

        if (dist > range) {
            driver.setForward(true);
            driver.setBack(false);
            if (config.isSprintEnabled() && dist > range * 1.5) {
                driver.setSprint(true);
            } else {
                driver.setSprint(false);
            }
        } else if (dist < range * 0.3) {
            driver.setBack(true);
            driver.setForward(false);
            driver.setSprint(false);
        } else {
            driver.setForward(false);
            driver.setBack(false);
            driver.setSprint(false);
        }

        if (config.isStrafeEnabled() && dist <= range) {
            StrafeDecider.StrafeDirection dir = strafeDecider.evaluate(
                player, target, scanner, config.getStrafeInterval(), config.isAvoidMobsInPath());
            applyStrafe(dir);
        } else {
            driver.setStrafeLeft(false);
            driver.setStrafeRight(false);
        }

        if (config.isJumpEnabled() && dist <= range && dist > range * 0.3) {
            if (scanner.needsJump(player, 0) && player.onGround) {
                driver.setJump(true);
            } else if (critScheduler.shouldJump(player)) {
                driver.setJump(true);
            } else {
                driver.setJump(false);
            }
        } else {
            driver.setJump(false);
        }
    }

    public void stop() {
        driver.releaseAll();
        strafeDecider.reset();
        critScheduler.reset();
        stuckTicks = 0;
        stuckResolvePhase = 0;
        resolveTicksRemaining = 0;
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public KeybindDriver getDriver() {
        return driver;
    }

    public EnvironmentScanner getScanner() {
        return scanner;
    }

    public CritScheduler getCritScheduler() {
        return critScheduler;
    }

    private void updateStuckDetection(EntityPlayerSP player) {
        double movedX = player.posX - lastPosX;
        double movedZ = player.posZ - lastPosZ;
        double movedDist = Math.sqrt(movedX * movedX + movedZ * movedZ);

        if (movedDist < STUCK_THRESHOLD) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
            stuckResolvePhase = 0;
        }

        lastPosX = player.posX;
        lastPosZ = player.posZ;
    }

    private void startStuckResolve(EntityPlayerSP player) {
        stuckTicks = 0;
        resolveTicksRemaining = RESOLVE_PHASE_DURATION;
        resolveYawOffset = scanner.findBestYawOffset(player, 4.0);
        stuckResolvePhase++;

        if (stuckResolvePhase > 3) {
            stuckResolvePhase = 1;
        }
    }

    private void executeStuckResolve(EntityPlayerSP player, MovementConfig config) {
        resolveTicksRemaining--;

        switch (stuckResolvePhase) {
            case 1:
                if (resolveYawOffset <= 0) {
                    driver.setStrafeLeft(true);
                    driver.setStrafeRight(false);
                } else {
                    driver.setStrafeRight(true);
                    driver.setStrafeLeft(false);
                }
                driver.setForward(true);
                driver.setBack(false);
                break;

            case 2:
                if (resolveYawOffset <= 0) {
                    driver.setStrafeLeft(true);
                    driver.setStrafeRight(false);
                } else {
                    driver.setStrafeRight(true);
                    driver.setStrafeLeft(false);
                }
                driver.setBack(true);
                driver.setForward(false);
                break;

            case 3:
                if (resolveYawOffset <= 0) {
                    driver.setStrafeLeft(true);
                    driver.setStrafeRight(false);
                } else {
                    driver.setStrafeRight(true);
                    driver.setStrafeLeft(false);
                }
                driver.setForward(true);
                driver.setBack(false);
                if (config.isJumpEnabled() && player.onGround) {
                    driver.setJump(true);
                }
                break;

            default:
                driver.setForward(true);
                break;
        }

        if (resolveTicksRemaining <= 0) {
            driver.setStrafeLeft(false);
            driver.setStrafeRight(false);
            driver.setBack(false);
            driver.setJump(false);
        }
    }

    private void applyStrafe(StrafeDecider.StrafeDirection dir) {
        switch (dir) {
            case LEFT:
                driver.setStrafeLeft(true);
                driver.setStrafeRight(false);
                break;
            case RIGHT:
                driver.setStrafeRight(true);
                driver.setStrafeLeft(false);
                break;
            case NONE:
            default:
                driver.setStrafeLeft(false);
                driver.setStrafeRight(false);
                break;
        }
    }

    private void facePosition(EntityPlayerSP player, double targetX, double targetZ, float smooth) {
        double dx = targetX - player.posX;
        double dz = targetZ - player.posZ;
        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0f);
        float yawDiff = MathHelper.wrapDegrees(targetYaw - player.rotationYaw);
        player.rotationYaw += yawDiff / Math.max(1.0f, smooth);
    }
}
