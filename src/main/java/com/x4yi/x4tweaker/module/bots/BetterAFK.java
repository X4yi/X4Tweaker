package com.x4yi.x4tweaker.module.bots;

import com.x4yi.x4tweaker.automation.BotTask;
import com.x4yi.x4tweaker.automation.TaskPriority;
import com.x4yi.x4tweaker.automation.task.afk.AFKBotTask;
import com.x4yi.x4tweaker.core.X4TweakerClient;
import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.module.combat.KillAuraLegit;
import com.x4yi.x4tweaker.setting.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class BetterAFK extends Module implements BotModule {

    private final NumberSetting checkInterval = new NumberSetting(
        "Check Interval", "Ticks entre cada chequeo del bot", 20, 5, 100, 1);

    private final BooleanSetting autoEat = new BooleanSetting(
        "Auto Eat", "Comer automáticamente cuando hay hambre", true);
    private final NumberSetting hungerThreshold = new NumberSetting(
        "Hunger Threshold", "Nivel de hambre para empezar a comer", 12, 1, 18, 1);
    private final NumberSetting eatUntil = new NumberSetting(
        "Eat Until", "Nivel de hambre objetivo al comer", 18, 10, 20, 1);

    private final BooleanSetting autoDefend = new BooleanSetting(
        "Auto Defend", "Contraatacar automáticamente cuando te golpean", false);
    private final NumberSetting attackRange = new NumberSetting(
        "Attack Range", "Distancia máxima para atacar al agresor", 3.5, 1.0, 6.0, 0.1);
    private final NumberSetting rotationSpeed = new NumberSetting(
        "Rotation Speed", "Velocidad de giro de cámara (mayor = más lento)", 3.0, 1.0, 10.0, 0.5);
    private final BooleanSetting sprintWhileChasing = new BooleanSetting(
        "Sprint While Chasing", "Correr al perseguir al agresor", true);
    private final BooleanSetting jumpWhileAttacking = new BooleanSetting(
        "Jump While Attacking", "Saltar durante combate para hacer críticos", true);
    private final BooleanSetting strafeInCombat = new BooleanSetting(
        "Strafe In Combat", "Esquivar lateralmente durante combate", true);
    private final NumberSetting strafeInterval = new NumberSetting(
        "Strafe Interval", "Ticks entre cada cambio de dirección de strafe", 15, 5, 60, 1);
    private final NumberSetting maxChaseDistance = new NumberSetting(
        "Max Chase Distance", "Distancia máxima de persecución desde el punto AFK", 20, 5, 50, 1);
    private final BooleanSetting avoidMobsInPath = new BooleanSetting(
        "Avoid Mobs In Path", "Esquivar otros mobs al perseguir al agresor", true);

    private final Supplier<Boolean> isDefendVisible = () -> autoDefend.getValue();
    private final Supplier<Boolean> isEatVisible = () -> autoEat.getValue();
    private final Supplier<Boolean> isStrafeVisible = () -> autoDefend.getValue() && strafeInCombat.getValue();

    private AFKBotTask currentTask = null;

    public BetterAFK() {
        super("BetterAFK", "Bot AFK inteligente: come y se defiende solo", Category.BOTS);

        hungerThreshold.withVisibilityCondition(isEatVisible);
        eatUntil.withVisibilityCondition(isEatVisible);
        attackRange.withVisibilityCondition(isDefendVisible);
        rotationSpeed.withVisibilityCondition(isDefendVisible);
        sprintWhileChasing.withVisibilityCondition(isDefendVisible);
        jumpWhileAttacking.withVisibilityCondition(isDefendVisible);
        strafeInCombat.withVisibilityCondition(isDefendVisible);
        strafeInterval.withVisibilityCondition(isStrafeVisible);
        maxChaseDistance.withVisibilityCondition(isDefendVisible);
        avoidMobsInPath.withVisibilityCondition(isDefendVisible);

        addSetting(checkInterval);
        addSetting(autoEat);
        addSetting(hungerThreshold);
        addSetting(eatUntil);
        addSetting(autoDefend);
        addSetting(attackRange);
        addSetting(rotationSpeed);
        addSetting(sprintWhileChasing);
        addSetting(jumpWhileAttacking);
        addSetting(strafeInCombat);
        addSetting(strafeInterval);
        addSetting(maxChaseDistance);
        addSetting(avoidMobsInPath);
    }

    @Override
    public void onEnable() {
        currentTask = (AFKBotTask) createTask();
        X4TweakerClient.getInstance().getAutomationManager().startTask(currentTask, TaskPriority.HIGH, true);
    }

    @Override
    public void onDisable() {
        if (currentTask != null && currentTask.isActive()) {
            X4TweakerClient.getInstance().getAutomationManager().stopCurrentTask();
        }
        currentTask = null;
    }

    @Override
    public String getBotStatus() {
        if (!isEnabled()) return "off";
        if (currentTask != null) return currentTask.getStatusMessage();
        return "idle";
    }

    @Override
    public BotTask createTask() {
        return new AFKBotTask(
            X4TweakerClient.getInstance().getAutomationManager().getContext(),
            X4TweakerClient.getInstance().getAutomationManager().getInputController(),
            X4TweakerClient.getInstance().getAutomationManager().getInventoryController(),
            this
        );
    }

    @Override
    public boolean isTaskRunning() {
        return currentTask != null && currentTask.isActive();
    }

    @Override
    public List<Class<? extends Module>> getIncompatibilities() {
        List<Class<? extends Module>> list = new ArrayList<>();
        list.add(KillAuraLegit.class);
        List<Module> modules = X4TweakerClient.getInstance().getModuleManager().getModules();
        for (int i = 0, size = modules.size(); i < size; i++) {
            Module m = modules.get(i);
            if (m instanceof BotModule && m.getClass() != this.getClass()) {
                list.add(m.getClass());
            }
        }
        return Collections.unmodifiableList(list);
    }

    public int getCheckInterval()       { return checkInterval.getValue().intValue(); }
    public boolean isAutoEatEnabled()   { return autoEat.getValue(); }
    public int getHungerThreshold()     { return hungerThreshold.getValue().intValue(); }
    public int getEatUntil()            { return eatUntil.getValue().intValue(); }
    public boolean isAutoDefendEnabled(){ return autoDefend.getValue(); }
    public double getAttackRange()      { return attackRange.getValue(); }
    public float getRotationSpeed()     { return rotationSpeed.getValue().floatValue(); }
    public boolean isSprintWhileChasing() { return sprintWhileChasing.getValue(); }
    public boolean isJumpWhileAttacking() { return jumpWhileAttacking.getValue(); }
    public boolean isStrafeInCombat()   { return strafeInCombat.getValue(); }
    public int getStrafeInterval()      { return strafeInterval.getValue().intValue(); }
    public double getMaxChaseDistance()  { return maxChaseDistance.getValue(); }
    public boolean isAvoidMobsInPath()  { return avoidMobsInPath.getValue(); }
}
