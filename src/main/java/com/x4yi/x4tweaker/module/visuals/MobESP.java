package com.x4yi.x4tweaker.module.visuals;

import com.x4yi.x4tweaker.setting.BooleanSetting;
import com.x4yi.x4tweaker.setting.NumberSetting;
import com.x4yi.x4tweaker.setting.StringListSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityFlying;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MobESP extends ESPBase {
    private final NumberSetting maxDistance = new NumberSetting("Max Distance", "Distancia maxima", 48.0D, 4.0D, 160.0D, 1.0D);
    private final BooleanSetting ignoreHostiles = new BooleanSetting("Ignore Hostiles", "Ignorar hostiles", false);
    private final BooleanSetting ignoreAnimals = new BooleanSetting("Ignore Animals", "Ignorar animales", false);
    private final BooleanSetting ignoreVillagers = new BooleanSetting("Ignore Villagers", "Ignorar aldeanos", false);
    private final BooleanSetting ignoreSlimes = new BooleanSetting("Ignore Slimes", "Ignorar slimes", false);
    private final BooleanSetting ignoreFlying = new BooleanSetting("Ignore Flying", "Ignorar voladores", false);
    private final BooleanSetting ignorePets = new BooleanSetting("Ignore Pets", "Ignorar mascotas domesticadas", true);
    private final BooleanSetting ignoreInvisible = new BooleanSetting("Ignore Invisible", "Ignorar invisibles", true);
    private final BooleanSetting ignoreArmorStands = new BooleanSetting("Ignore Armor Stands", "Ignorar armor stands", true);
    private final BooleanSetting ignorePlayers = new BooleanSetting("Ignore Players", "Ignorar jugadores", true);
    private final StringListSetting ignoreList = new StringListSetting("Ignore List", "Nombres o ids a ignorar");

    public MobESP() {
        super("MobESP", "ESP para mobs y entidades vivas");
        addSetting(maxDistance);
        addSetting(ignoreHostiles);
        addSetting(ignoreAnimals);
        addSetting(ignoreVillagers);
        addSetting(ignoreSlimes);
        addSetting(ignoreFlying);
        addSetting(ignorePets);
        addSetting(ignoreInvisible);
        addSetting(ignoreArmorStands);
        addSetting(ignorePlayers);
        addSetting(ignoreList);
    }

    @Override
    protected List<Entity> getEntities() {
        List<Entity> out = new ArrayList<Entity>();
        if (mc.world == null || mc.player == null) return out;

        double maxDistSq = maxDistance.getValue() * maxDistance.getValue();
        List<Entity> loaded = mc.world.loadedEntityList;
        for (int i = 0, size = loaded.size(); i < size; i++) {
            Entity e = loaded.get(i);
            if (!(e instanceof EntityLivingBase)) continue;
            if (e == mc.player || !e.isEntityAlive() || e.isDead) continue;
            if (mc.player.getDistanceSq(e) > maxDistSq) continue;
            if (!passesFilters((EntityLivingBase) e)) continue;
            out.add(e);
        }
        return out;
    }

    private boolean passesFilters(EntityLivingBase entity) {
        if (ignoreInvisible.getValue() && entity.isInvisible()) return false;
        if (entity instanceof EntityArmorStand && ignoreArmorStands.getValue()) return false;
        if (entity instanceof EntityPlayer && ignorePlayers.getValue()) return false;
        if (entity instanceof EntitySlime && ignoreSlimes.getValue()) return false;
        if (entity instanceof EntityVillager && ignoreVillagers.getValue()) return false;
        if (entity instanceof EntityTameable && ((EntityTameable) entity).isTamed() && ignorePets.getValue()) return false;
        if ((entity instanceof EntityFlying || entity instanceof EntityBat) && ignoreFlying.getValue()) return false;

        boolean isHostile = entity instanceof IMob;
        boolean isAnimal = entity instanceof IAnimals && !isHostile && !(entity instanceof EntityVillager);

        if (isHostile && ignoreHostiles.getValue()) return false;
        if (isAnimal && ignoreAnimals.getValue()) return false;

        if (!ignoreList.getValue().isEmpty()) {
            String name = entity.getName();
            if (name != null && ignoreList.contains(name)) return false;
            ResourceLocation key = EntityList.getKey(entity);
            if (key != null && ignoreList.contains(key.toString())) return false;
        }

        return true;
    }

    @Override
    protected float[] getColor() {
        return new float[]{0.16f, 0.95f, 0.58f, 1.0f};
    }
}
