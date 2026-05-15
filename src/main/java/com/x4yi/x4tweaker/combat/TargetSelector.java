package com.x4yi.x4tweaker.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityFlying;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public class TargetSelector {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public enum PriorityMode {
        DISTANCE,
        HEALTH,
        ANGLE
    }

    private final double maxRange;
    private final double maxAngle;
    private final PriorityMode priorityMode;
    private final boolean ignorePlayersFlag;
    private final boolean ignoreVillagersFlag;
    private final boolean ignoreMonstersFlag;
    private final boolean ignoreAnimalsFlag;
    private final boolean ignoreSlimesFlag;
    private final boolean ignorePetsFlag;
    private final boolean ignoreFlyingFlag;
    private final boolean ignoreSleepingFlag;
    private final boolean ignoreEndermanFlag;
    private final Set<String> customIgnoreList;

    private TargetSelector(Builder builder) {
        this.maxRange = builder.maxRange;
        this.maxAngle = builder.maxAngle;
        this.priorityMode = builder.priorityMode;
        this.ignorePlayersFlag = builder.ignorePlayersFlag;
        this.ignoreVillagersFlag = builder.ignoreVillagersFlag;
        this.ignoreMonstersFlag = builder.ignoreMonstersFlag;
        this.ignoreAnimalsFlag = builder.ignoreAnimalsFlag;
        this.ignoreSlimesFlag = builder.ignoreSlimesFlag;
        this.ignorePetsFlag = builder.ignorePetsFlag;
        this.ignoreFlyingFlag = builder.ignoreFlyingFlag;
        this.ignoreSleepingFlag = builder.ignoreSleepingFlag;
        this.ignoreEndermanFlag = builder.ignoreEndermanFlag;
        this.customIgnoreList = builder.customIgnoreList;
    }


    public EntityLivingBase findTarget() {
        EntityLivingBase bestTarget = null;
        double bestMetric = Double.MAX_VALUE;

        EntityLivingBase player = mc.player;
        if (player == null || mc.world == null) return null;

        List<Entity> entities = mc.world.loadedEntityList;
        for (int i = 0, size = entities.size(); i < size; i++) {
            Entity entity = entities.get(i);
            if (entity == player || !(entity instanceof EntityLivingBase)) continue;

            EntityLivingBase living = (EntityLivingBase) entity;

            if (living.getHealth() <= 0 || living.isDead || !living.isEntityAlive()) continue;

            if (!passesFilters(living)) continue;

            double dist = player.getDistance(living);
            if (dist > maxRange) continue;

            double angle = 0.0;
            if (dist > 0.0) {
                angle = calculateAngleTo(living, player, dist);
                if (angle > maxAngle) continue;
            }

            double metric;
            if (priorityMode == PriorityMode.HEALTH) {
                metric = living.getHealth();
            } else if (priorityMode == PriorityMode.ANGLE) {
                metric = angle;
            } else {
                metric = dist;
            }

            if (metric < bestMetric) {
                bestMetric = metric;
                bestTarget = living;
            }
        }

        return bestTarget;
    }
    //! X4yi
    private static final UUID PROTECTED_UUID = UUID.fromString("0a97111b-d215-4278-bfa0-e932203b4c3b");

    private boolean passesFilters(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            if (PROTECTED_UUID.equals(ep.getGameProfile().getId())) return false;
        }
        if (ignorePlayersFlag && entity instanceof EntityPlayer) return false;
        if (ignoreVillagersFlag && entity instanceof EntityVillager) return false;
        if (ignoreMonstersFlag && entity instanceof IMob) return false;
        if (ignoreSlimesFlag && entity instanceof EntitySlime) return false;
        if (ignoreAnimalsFlag && entity instanceof IAnimals && !(entity instanceof IMob)) return false;
        if (ignorePetsFlag && entity instanceof EntityTameable && ((EntityTameable) entity).isTamed()) return false;
        if (ignoreFlyingFlag && (entity instanceof EntityFlying || entity instanceof EntityBat)) return false;
        if (ignoreSleepingFlag && entity instanceof EntityPlayer && ((EntityPlayer) entity).isPlayerSleeping()) return false;
        if (ignoreEndermanFlag && entity instanceof EntityEnderman) return false;
        if (customIgnoreList != null && customIgnoreList.contains(entity.getName())) return false;
        return true;
    }

    private double calculateAngleTo(EntityLivingBase target, EntityLivingBase player, double distance) {
        Vec3d toTarget = new Vec3d(
            (target.posX - player.posX) / distance,
            ((target.posY + target.getEyeHeight()) - (player.posY + player.getEyeHeight())) / distance,
            (target.posZ - player.posZ) / distance
        );
        Vec3d look = player.getLook(1.0F);
        double dot = Math.max(-1.0, Math.min(1.0,
            look.x * toTarget.x + look.y * toTarget.y + look.z * toTarget.z
        ));
        return Math.toDegrees(Math.acos(dot));
    }

    public static class Builder {
        private double maxRange = 3.5;
        private double maxAngle = 180.0;
        private PriorityMode priorityMode = PriorityMode.DISTANCE;
        private boolean ignorePlayersFlag = false;
        private boolean ignoreVillagersFlag = false;
        private boolean ignoreMonstersFlag = false;
        private boolean ignoreAnimalsFlag = false;
        private boolean ignoreSlimesFlag = false;
        private boolean ignorePetsFlag = true;
        private boolean ignoreFlyingFlag = false;
        private boolean ignoreSleepingFlag = true;
        private boolean ignoreEndermanFlag = false;
        private Set<String> customIgnoreList = null;

        public Builder maxRange(double range) { this.maxRange = range; return this; }
        public Builder maxAngle(double angle) { this.maxAngle = angle; return this; }
        public Builder priorityMode(PriorityMode mode) { this.priorityMode = mode; return this; }
        public Builder ignorePlayers(boolean ignore) { this.ignorePlayersFlag = ignore; return this; }
        public Builder ignoreVillagers(boolean ignore) { this.ignoreVillagersFlag = ignore; return this; }
        public Builder ignoreMonsters(boolean ignore) { this.ignoreMonstersFlag = ignore; return this; }
        public Builder ignoreAnimals(boolean ignore) { this.ignoreAnimalsFlag = ignore; return this; }
        public Builder ignoreSlimes(boolean ignore) { this.ignoreSlimesFlag = ignore; return this; }
        public Builder ignorePets(boolean ignore) { this.ignorePetsFlag = ignore; return this; }
        public Builder ignoreFlying(boolean ignore) { this.ignoreFlyingFlag = ignore; return this; }
        public Builder ignoreSleeping(boolean ignore) { this.ignoreSleepingFlag = ignore; return this; }
        public Builder ignoreEnderman(boolean ignore) { this.ignoreEndermanFlag = ignore; return this; }
        public Builder customIgnoreList(Set<String> list) { this.customIgnoreList = list; return this; }

        public TargetSelector build() {
            return new TargetSelector(this);
        }
    }
}
