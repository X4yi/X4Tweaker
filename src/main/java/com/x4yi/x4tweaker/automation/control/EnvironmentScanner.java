package com.x4yi.x4tweaker.automation.control;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentScanner {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final float[] SCAN_OFFSETS = { 0, 45, -45, 90, -90, 135, -135, 180 };

    public boolean isPathClear(EntityPlayerSP player, float yawOffset, double distance) {
        float yaw = player.rotationYaw + yawOffset;
        double radYaw = Math.toRadians(yaw);
        double dx = -Math.sin(radYaw);
        double dz = Math.cos(radYaw);

        double stepSize = 0.5;
        int steps = (int) Math.ceil(distance / stepSize);

        for (int i = 1; i <= steps; i++) {
            double checkX = player.posX + dx * stepSize * i;
            double checkZ = player.posZ + dz * stepSize * i;

            BlockPos feetPos = new BlockPos(checkX, player.posY, checkZ);
            BlockPos headPos = feetPos.up();

            if (isSolid(feetPos) && isSolid(headPos)) {
                return false;
            }
        }
        return true;
    }

    public boolean needsJump(EntityPlayerSP player, float yawOffset) {
        float yaw = player.rotationYaw + yawOffset;
        double radYaw = Math.toRadians(yaw);
        double dx = -Math.sin(radYaw);
        double dz = Math.cos(radYaw);

        double checkX = player.posX + dx * 1.0;
        double checkZ = player.posZ + dz * 1.0;

        BlockPos feetPos = new BlockPos(checkX, player.posY, checkZ);
        BlockPos aboveFeet = feetPos.up();
        BlockPos twoAbove = feetPos.up(2);

        boolean feetBlocked = isSolid(feetPos);
        boolean headClear = !isSolid(aboveFeet);
        boolean aboveHeadClear = !isSolid(twoAbove);

        if (feetBlocked && headClear && aboveHeadClear) return true;

        if (player.isInWater()) return true;

        if (feetBlocked && headClear) return true;

        return false;
    }

    public boolean hasObstacleBetween(EntityPlayerSP player, EntityLivingBase target) {
        double dx = target.posX - player.posX;
        double dz = target.posZ - player.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < 1.0) return false;

        double stepX = dx / dist;
        double stepZ = dz / dist;
        int steps = MathHelper.ceil(dist);

        for (int i = 1; i < steps; i++) {
            double checkX = player.posX + stepX * i;
            double checkZ = player.posZ + stepZ * i;

            BlockPos feetPos = new BlockPos(checkX, player.posY, checkZ);
            BlockPos headPos = feetPos.up();

            if (isSolid(feetPos) && isSolid(headPos)) {
                return true;
            }
        }
        return false;
    }

    public List<EntityLivingBase> getEntitiesBetween(EntityPlayerSP player, EntityLivingBase target, double width) {
        List<EntityLivingBase> result = new ArrayList<>();
        if (mc.world == null) return result;

        double dx = target.posX - player.posX;
        double dz = target.posZ - player.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.5) return result;

        double nx = dx / dist;
        double nz = dz / dist;

        List<Entity> entities = mc.world.loadedEntityList;
        for (int i = 0, size = entities.size(); i < size; i++) {
            Entity e = entities.get(i);
            if (e == player || e == target) continue;
            if (!(e instanceof EntityLivingBase)) continue;

            EntityLivingBase living = (EntityLivingBase) e;
            if (living.isDead || !living.isEntityAlive()) continue;

            double ex = living.posX - player.posX;
            double ez = living.posZ - player.posZ;

            double projection = ex * nx + ez * nz;
            if (projection < 0 || projection > dist) continue;

            double perpDist = Math.abs(ex * nz - ez * nx);
            if (perpDist < width) {
                result.add(living);
            }
        }
        return result;
    }

    public float findBestYawOffset(EntityPlayerSP player, double checkDist) {
        float bestOffset = 0;
        double bestScore = -1;

        for (float offset : SCAN_OFFSETS) {
            double score = measureClearDistance(player, offset, checkDist);

            if (!hasDangerousDropAhead(player, offset, 2.0)) {
                score += 1.0;
            }

            if (score > bestScore) {
                bestScore = score;
                bestOffset = offset;
            }
        }
        return bestOffset;
    }

    public boolean hasDangerousDropAhead(EntityPlayerSP player, float yawOffset, double checkDist) {
        float yaw = player.rotationYaw + yawOffset;
        double radYaw = Math.toRadians(yaw);
        double dx = -Math.sin(radYaw);
        double dz = Math.cos(radYaw);

        double checkX = player.posX + dx * checkDist;
        double checkZ = player.posZ + dz * checkDist;

        BlockPos pos = new BlockPos(checkX, player.posY, checkZ);

        if (isSolid(pos)) return false;

        int airCount = 0;
        for (int y = 0; y < 5; y++) {
            BlockPos below = pos.down(y);
            if (isSolid(below)) {
                return airCount > 3;
            }
            airCount++;
        }
        return true;
    }

    public boolean hasSolidGround(BlockPos pos) {
        if (mc.world == null) return false;
        return isSolid(pos.down());
    }

    private double measureClearDistance(EntityPlayerSP player, float yawOffset, double maxDist) {
        float yaw = player.rotationYaw + yawOffset;
        double radYaw = Math.toRadians(yaw);
        double dx = -Math.sin(radYaw);
        double dz = Math.cos(radYaw);

        double stepSize = 0.5;
        int steps = (int) Math.ceil(maxDist / stepSize);

        for (int i = 1; i <= steps; i++) {
            double checkX = player.posX + dx * stepSize * i;
            double checkZ = player.posZ + dz * stepSize * i;

            BlockPos feetPos = new BlockPos(checkX, player.posY, checkZ);
            BlockPos headPos = feetPos.up();

            if (isSolid(feetPos) && isSolid(headPos)) {
                return stepSize * (i - 1);
            }
        }
        return maxDist;
    }

    /**
     * Checks if a block is considered solid for navigation purposes.
     * TODO: This currently uses isFullBlock(), which fails to detect
     * partial solid blocks like fence gates, slabs, and stairs.
     * Future refactors should use getCollisionBoundingBox() for accurate
     * collision detection.
     */
    private boolean isSolid(BlockPos pos) {
        if (mc.world == null) return false;
        IBlockState state = mc.world.getBlockState(pos);
        return state.getMaterial() != Material.AIR
            && state.getMaterial() != Material.WATER
            && state.getMaterial() != Material.PLANTS
            && state.isFullBlock();
    }
}
