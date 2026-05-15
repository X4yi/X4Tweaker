package com.x4yi.x4tweaker.automation.task.afk;

import com.x4yi.x4tweaker.automation.BotTask;
import com.x4yi.x4tweaker.automation.context.AutomationContext;
import com.x4yi.x4tweaker.automation.control.*;
import com.x4yi.x4tweaker.combat.CritScheduler;
import com.x4yi.x4tweaker.combat.LegitAttackOrchestrator;
import com.x4yi.x4tweaker.combat.RotationCalculator;
import com.x4yi.x4tweaker.module.bots.BetterAFK;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class AFKBotTask extends BotTask {

    private enum Phase {
        IDLE,
        CHASING,
        ATTACKING,
        RETURNING,
        RECOVERING
    }

    private final AutomationContext context;
    private final InputController inputController;
    private final InventoryController inventoryController;
    private final BetterAFK config;

    private Phase phase = Phase.IDLE;
    private BotTask currentStep;

    private double afkX, afkY, afkZ;
    private float afkYaw, afkPitch;

    private EntityLivingBase attackTarget = null;
    private final Deque<EntityLivingBase> targetStack = new ArrayDeque<>();
    private static final int MAX_TARGET_STACK = 3;

    private RotationCalculator rotationCalc;
    private LegitAttackOrchestrator attackOrch;
    private BotMovementController movementController;
    private MovementConfig movementConfig;

    private boolean wasHurt = false;

    private static final double RETURN_THRESHOLD = 1.5;
    private static final double ATTACKER_SEARCH_RANGE = 8.0;

    public AFKBotTask(AutomationContext context, InputController inputController,
                      InventoryController inventoryController, BetterAFK config) {
        this.context = context;
        this.inputController = inputController;
        this.inventoryController = inventoryController;
        this.config = config;
    }

    @Override
    public String getName() { return "AFKBot"; }

    @Override
    public void onStart() {
        EntityPlayerSP player = mc.player;
        if (player == null) { stop(); return; }

        context.getSnapshot().capture(mc);
        inputController.lockInput();

        afkX = player.posX;
        afkY = player.posY;
        afkZ = player.posZ;
        afkYaw = player.rotationYaw;
        afkPitch = player.rotationPitch;

        phase = Phase.IDLE;
        attackTarget = null;
        targetStack.clear();
        wasHurt = false;

        float smooth = config != null ? config.getRotationSpeed() : 3.0f;
        rotationCalc = new RotationCalculator(smooth);
        attackOrch = new LegitAttackOrchestrator(25.0f);
        movementController = new BotMovementController();
        movementConfig = buildMovementConfig();

        currentStep = new WaitTask(getCheckInterval());
        currentStep.start();
        setStatusMessage("idle");
        context.setStatus("afk:idle");
    }

    @Override
    public void onUpdate() {
        EntityPlayerSP player = mc.player;
        if (player == null) { stop(); return; }

        switch (phase) {
            case IDLE:
                updateIdle(player);
                break;
            case CHASING:
                updateChasing(player);
                break;
            case ATTACKING:
                updateAttacking(player);
                break;
            case RETURNING:
                updateReturning(player);
                break;
            case RECOVERING:
                updateRecovering(player);
                break;
        }
    }

    private void updateIdle(EntityPlayerSP player) {
        if (config != null && config.isAutoDefendEnabled()) {
            EntityLivingBase attacker = detectAttacker(player);
            if (attacker != null) {
                startCombat(attacker);
                return;
            }
        }

        if (config != null && config.isAutoEatEnabled() && needsFood(player)) {
            phase = Phase.RECOVERING;
            setStatusMessage("eating");
            context.setStatus("afk:eating");
            return;
        }

        runIdleStep();
        setStatusMessage("idle");
        context.setStatus("afk:idle");
    }

    private EntityLivingBase detectAttacker(EntityPlayerSP player) {
        boolean isHurt = player.hurtTime > 0;

        if (isHurt && !wasHurt) {
            wasHurt = true;
            return findLikelyAttacker(player);
        }

        if (!isHurt) {
            wasHurt = false;
        }

        return null;
    }

    private EntityLivingBase findLikelyAttacker(EntityPlayerSP player) {
        if (mc.world == null) return null;

        EntityLivingBase best = null;
        double bestScore = -1;

        List<Entity> entities = mc.world.loadedEntityList;
        for (int i = 0, size = entities.size(); i < size; i++) {
            Entity entity = entities.get(i);
            if (entity == player) continue;
            if (!(entity instanceof EntityLivingBase)) continue;

            EntityLivingBase living = (EntityLivingBase) entity;
            if (living.isDead || !living.isEntityAlive()) continue;

            double dist = player.getDistance(living);
            if (dist > ATTACKER_SEARCH_RANGE) continue;

            double score = scoreAttacker(player, living, dist);
            if (score > bestScore) {
                bestScore = score;
                best = living;
            }
        }

        return best;
    }

    private double scoreAttacker(EntityPlayerSP player, EntityLivingBase entity, double dist) {
        double score = 0;

        score += (1.0 - dist / ATTACKER_SEARCH_RANGE) * 30.0;

        if (entity instanceof EntityCreeper) {
            EntityCreeper creeper = (EntityCreeper) entity;
            if (creeper.getCreeperState() > 0) {
                score += 50.0;
            } else {
                score += 40.0;
            }
        } else if (entity instanceof EntityEnderman) {
            EntityEnderman enderman = (EntityEnderman) entity;
            if (enderman.isScreaming()) {
                score += 45.0;
            } else {
                score += 15.0;
            }
        } else if (entity instanceof EntityWolf) {
            EntityWolf wolf = (EntityWolf) entity;
            if (wolf.isAngry()) {
                score += 38.0;
            } else {
                score += 3.0;
            }
        } else if (entity instanceof EntityMob) {
            score += 40.0;
        } else if (entity instanceof EntityPlayer) {
            score += 25.0;
        } else if (entity instanceof EntityAnimal) {
            score += 5.0;
        } else {
            score += 10.0;
        }

        Vec3d entityLook = entity.getLookVec();
        Vec3d toPlayer = new Vec3d(
            player.posX - entity.posX,
            player.posY - entity.posY,
            player.posZ - entity.posZ
        ).normalize();
        double dot = entityLook.x * toPlayer.x + entityLook.z * toPlayer.z;
        if (dot > 0.7) {
            score += 20.0;
        } else if (dot > 0.3) {
            score += 10.0;
        }

        if (entity.hurtTime > 0) {
            score += 5.0;
        }

        if (entity.getRevengeTarget() == player) {
            score += 15.0;
        }

        return score;
    }

    private void startCombat(EntityLivingBase target) {
        if (currentStep != null && currentStep.isActive()) {
            currentStep.stop();
        }

        if (attackTarget != null && isEntityValid(attackTarget) && attackTarget != target) {
            if (targetStack.size() < MAX_TARGET_STACK) {
                targetStack.push(attackTarget);
            }
        }

        attackTarget = target;
        rotationCalc.reset();

        double dist = mc.player.getDistance(target);
        double range = movementConfig.getAttackRange();

        if (dist > range) {
            phase = Phase.CHASING;
            setStatusMessage("chasing");
            context.setStatus("afk:chasing");
        } else {
            phase = Phase.ATTACKING;
            setStatusMessage("attacking");
            context.setStatus("afk:attacking");
        }
    }

    private void updateChasing(EntityPlayerSP player) {
        checkForRetarget(player);

        if (!isEntityValid(attackTarget)) {
            onTargetDead();
            return;
        }

        double dist = player.getDistance(attackTarget);
        double range = movementConfig.getAttackRange();

        if (dist > movementConfig.getMaxChaseDistance()) {
            onTargetDead();
            return;
        }

        rotationCalc.applySmoothRotation(player, attackTarget);
        movementController.moveToward(player, attackTarget.posX, attackTarget.posZ, movementConfig);

        if (dist <= range) {
            movementController.stop();
            phase = Phase.ATTACKING;
            setStatusMessage("attacking");
            context.setStatus("afk:attacking");
        }
    }

    private void updateAttacking(EntityPlayerSP player) {
        checkForRetarget(player);

        if (!isEntityValid(attackTarget)) {
            onTargetDead();
            return;
        }

        double dist = player.getDistance(attackTarget);
        double range = movementConfig.getAttackRange();

        float[] diff = rotationCalc.applySmoothRotation(player, attackTarget);

        if (dist > range * 1.3) {
            movementController.stop();
            phase = Phase.CHASING;
            setStatusMessage("chasing");
            context.setStatus("afk:chasing");
            return;
        }

        movementController.maintainCombatDistance(player, attackTarget, movementConfig);

        boolean aimReady = Math.abs(diff[0]) < attackOrch.getAimThreshold()
                        && Math.abs(diff[1]) < attackOrch.getAimThreshold();

        if (config != null && config.isJumpWhileAttacking()) {
            CritScheduler crit = movementController.getCritScheduler();
            if (crit.shouldAttackNow(player) && aimReady) {
                attackOrch.attack(player, attackTarget);
            }
        } else {
            if (attackOrch.canAttack(player) && aimReady) {
                attackOrch.attack(player, attackTarget);
            }
        }
    }

    private void checkForRetarget(EntityPlayerSP player) {
        if (config == null || !config.isAutoDefendEnabled()) return;

        EntityLivingBase newAttacker = detectAttacker(player);
        if (newAttacker != null && newAttacker != attackTarget) {
            double newDist = player.getDistance(newAttacker);
            double currentDist = attackTarget != null ? player.getDistance(attackTarget) : Double.MAX_VALUE;

            if (newDist < currentDist) {
                startCombat(newAttacker);
            }
        }
    }

    private void onTargetDead() {
        attackTarget = null;

        while (!targetStack.isEmpty()) {
            EntityLivingBase previous = targetStack.pop();
            if (isEntityValid(previous)) {
                startCombat(previous);
                return;
            }
        }

        movementController.stop();
        startReturning();
    }

    private void startReturning() {
        attackTarget = null;
        targetStack.clear();
        rotationCalc.reset();
        phase = Phase.RETURNING;
        setStatusMessage("returning");
        context.setStatus("afk:returning");
    }

    private void updateReturning(EntityPlayerSP player) {
        if (config != null && config.isAutoDefendEnabled()) {
            EntityLivingBase attacker = detectAttacker(player);
            if (attacker != null) {
                movementController.stop();
                startCombat(attacker);
                return;
            }
        }

        double dist = distToAfk(player);

        if (dist < RETURN_THRESHOLD) {
            movementController.stop();
            player.rotationYaw = afkYaw;
            player.rotationPitch = afkPitch;
            phase = Phase.IDLE;
            setStatusMessage("idle");
            context.setStatus("afk:idle");
            return;
        }

        movementController.moveToward(player, afkX, afkZ, movementConfig);
    }

    private void updateRecovering(EntityPlayerSP player) {
        if (config != null && config.isAutoDefendEnabled()) {
            EntityLivingBase attacker = detectAttacker(player);
            if (attacker != null) {
                if (currentStep != null && currentStep.isActive()) currentStep.stop();
                startCombat(attacker);
                return;
            }
        }

        if (currentStep instanceof EatFromHotbarTask) {
            if (currentStep.isActive()) {
                currentStep.onUpdate();
            } else {
                phase = Phase.IDLE;
            }
            return;
        }

        if (currentStep != null && currentStep.isActive()) {
            currentStep.stop();
        }

        int targetFood = config != null ? config.getEatUntil() : 18;
        currentStep = new EatFromHotbarTask(inventoryController, targetFood);
        currentStep.start();
    }

    private boolean isEntityValid(EntityLivingBase entity) {
        return entity != null
            && entity.isEntityAlive()
            && !entity.isDead
            && entity.getHealth() > 0;
    }

    private boolean needsFood(EntityPlayerSP player) {
        int threshold = config != null ? config.getHungerThreshold() : 12;
        return player.getFoodStats().getFoodLevel() <= threshold;
    }

    private void runIdleStep() {
        if (currentStep != null && currentStep.isActive()) {
            currentStep.onUpdate();
            return;
        }
        currentStep = new WaitTask(getCheckInterval());
        currentStep.start();
    }

    private double distToAfk(EntityPlayerSP player) {
        double dx = afkX - player.posX;
        double dz = afkZ - player.posZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private int getCheckInterval() {
        return config != null ? config.getCheckInterval() : 20;
    }

    private MovementConfig buildMovementConfig() {
        if (config == null) {
            return new MovementConfig.Builder().build();
        }
        return new MovementConfig.Builder()
            .attackRange(config.getAttackRange())
            .sprintEnabled(config.isSprintWhileChasing())
            .jumpEnabled(config.isJumpWhileAttacking())
            .strafeEnabled(config.isStrafeInCombat())
            .strafeInterval(config.getStrafeInterval())
            .maxChaseDistance(config.getMaxChaseDistance())
            .avoidMobsInPath(config.isAvoidMobsInPath())
            .build();
    }

    @Override
    public void onStop() {
        if (currentStep != null && currentStep.isActive()) {
            currentStep.stop();
        }

        if (movementController != null) {
            movementController.stop();
        }

        attackTarget = null;
        targetStack.clear();

        inventoryController.restoreSlot();
        context.getSnapshot().restore(mc);
        inputController.unlockInput();
        context.setStatus("afk:stopped");
    }
}
