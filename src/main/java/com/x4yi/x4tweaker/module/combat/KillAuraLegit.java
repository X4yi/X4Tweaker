package com.x4yi.x4tweaker.module.combat;

import com.x4yi.x4tweaker.combat.*;
import com.x4yi.x4tweaker.module.Category;
import com.x4yi.x4tweaker.module.Module;
import com.x4yi.x4tweaker.module.bots.BetterAFK;
import com.x4yi.x4tweaker.setting.*;

import java.util.*;
import java.util.function.Supplier;


public class KillAuraLegit extends Module {
    private final NumberSetting range         = new NumberSetting("Attack Range", "Distancia máxima para atacar", 3.5, 1.0, 6.0, 0.1);
    private final NumberSetting smooth        = new NumberSetting("Rotation Speed", "Velocidad de giro de cámara (mayor = más lento)", 2.0, 1.0, 10.0, 0.5);
    private final ModeSetting priority        = new ModeSetting("Target Priority", "Cómo elegir el objetivo", "Distance", "Distance", "Health", "Angle");
    private final NumberSetting aimThreshold  = new NumberSetting("Aim Precision", "Precisión requerida para atacar (grados)", 15.0, 1.0, 45.0, 1.0);
    private final NumberSetting angleFilter   = new NumberSetting("FOV Filter", "Campo de visión para buscar objetivos", 180.0, 1.0, 360.0, 1.0);

    private final BooleanSetting ignorePlayers   = new BooleanSetting("Skip Players",    "No atacar jugadores",             false);
    private final BooleanSetting ignoreMonsters  = new BooleanSetting("Skip Monsters",   "No atacar mobs hostiles",         false);
    private final BooleanSetting ignoreAnimals   = new BooleanSetting("Skip Animals",    "No atacar animales pacíficos",    false);
    private final BooleanSetting ignoreSlimes    = new BooleanSetting("Skip Slimes",     "No atacar slimes",                false);
    private final BooleanSetting ignorePets      = new BooleanSetting("Skip Pets",       "No atacar mascotas domesticadas", true);
    private final BooleanSetting ignoreFlying    = new BooleanSetting("Skip Flying",     "No atacar entidades voladoras",   false);
    private final BooleanSetting ignoreSleeping  = new BooleanSetting("Skip Sleeping",   "No atacar jugadores durmiendo",   true);
    private final BooleanSetting ignoreEnderman  = new BooleanSetting("Skip Enderman",   "No atacar endermen",              false);

    private final StringListSetting customFilter = new StringListSetting("Ignore List", "Nombres de entidades a ignorar");

    private CombatController combatController = null;
    private boolean settingsDirty = true;

    private List<Class<? extends Module>> cachedIncompatibilities = null;

    public KillAuraLegit() {
        super("KillAuraLegit", "Ataca entidades con rotaciones y paquetes legítimos", Category.COMBAT);

        angleFilter.withVisibilityCondition(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return "Angle".equalsIgnoreCase(priority.getValue());
            }
        });

        addSetting(range);
        addSetting(smooth);
        addSetting(priority);
        addSetting(aimThreshold);
        addSetting(angleFilter);
        addSetting(ignorePlayers);
        addSetting(ignoreMonsters);
        addSetting(ignoreAnimals);
        addSetting(ignoreSlimes);
        addSetting(ignorePets);
        addSetting(ignoreFlying);
        addSetting(ignoreSleeping);
        addSetting(ignoreEnderman);
        addSetting(customFilter);
    }

    @Override
    public void onEnable() {
        settingsDirty = true;
    }

    @Override
    public void onDisable() {
        if (combatController != null) {
            combatController.stop();
            combatController = null;
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null) return;

        if (settingsDirty || combatController == null) {
            rebuildCombatController();
            settingsDirty = false;
        }

        combatController.updateAndAttack(mc.player);
    }

    @Override
    public void onSettingChanged() {
        settingsDirty = true;
    }

    @Override
    public List<Class<? extends Module>> getIncompatibilities() {
        if (cachedIncompatibilities != null) return cachedIncompatibilities;
        List<Class<? extends Module>> list = new ArrayList<>();
        list.add(BetterAFK.class);
        cachedIncompatibilities = Collections.unmodifiableList(list);
        return cachedIncompatibilities;
    }

    private void rebuildCombatController() {
        if (combatController != null) {
            combatController.stop();
        }

        String priorityStr = priority.getValue();
        TargetSelector.PriorityMode priorityMode;
        if ("Health".equals(priorityStr)) {
            priorityMode = TargetSelector.PriorityMode.HEALTH;
        } else if ("Angle".equals(priorityStr)) {
            priorityMode = TargetSelector.PriorityMode.ANGLE;
        } else {
            priorityMode = TargetSelector.PriorityMode.DISTANCE;
        }

        TargetSelector.Builder selectorBuilder = new TargetSelector.Builder()
            .maxRange(range.getValue())
            .maxAngle(angleFilter.getValue())
            .priorityMode(priorityMode)
            .ignorePlayers(ignorePlayers.getValue())
            .ignoreMonsters(ignoreMonsters.getValue())
            .ignoreAnimals(ignoreAnimals.getValue())
            .ignoreSlimes(ignoreSlimes.getValue())
            .ignorePets(ignorePets.getValue())
            .ignoreFlying(ignoreFlying.getValue())
            .ignoreSleeping(ignoreSleeping.getValue())
            .ignoreEnderman(ignoreEnderman.getValue());

        if (!customFilter.getValue().isEmpty()) {
            selectorBuilder.customIgnoreList(new HashSet<>(customFilter.getValue()));
        }

        TargetSelector targetSelector = selectorBuilder.build();
        RotationCalculator rotationCalculator = new RotationCalculator(smooth.getValue().floatValue());
        IAttackOrchestrator orchestrator = AttackOrchestrator.createLegit(aimThreshold.getValue().floatValue());

        combatController = new CombatController(targetSelector, rotationCalculator, orchestrator);
    }

    public CombatController getCombatController() {
        return combatController;
    }
}
