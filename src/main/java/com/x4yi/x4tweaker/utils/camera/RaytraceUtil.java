package com.x4yi.x4tweaker.utils.camera;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import javax.annotation.Nullable;
import java.util.List;

public class RaytraceUtil {
    private static final Predicate<Entity> COLLIDABLE_PREDICATE = new Predicate<Entity>() {
        public boolean apply(@Nullable Entity p_apply_1_) {
            return p_apply_1_ != null && p_apply_1_.canBeCollidedWith();
        }
    };
    private static final Predicate<Entity> TARGET_PREDICATE = Predicates.and(EntitySelectors.NOT_SPECTATING, COLLIDABLE_PREDICATE);



    public static void updateMouseOver(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) return;

        Entity entity = mc.player;
        double d0 = mc.playerController.getBlockReachDistance();
        mc.objectMouseOver = entity.rayTrace(d0, partialTicks);

        Vec3d vec3d = entity.getPositionEyes(partialTicks);
        boolean flag = false;
        double d1 = d0;

        if (mc.playerController.extendedReach()) {
            d1 = 6.0D;
            d0 = d1;
        } else {
            if (d0 > 3.0D) flag = true;
        }

        if (mc.objectMouseOver != null) {
            d1 = mc.objectMouseOver.hitVec.distanceTo(vec3d);
        }

        Vec3d vec3d1 = entity.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0);
        mc.pointedEntity = null;
        Vec3d vec3d3 = null;

        List<Entity> list = mc.world.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().expand(vec3d1.x * d0, vec3d1.y * d0, vec3d1.z * d0).grow(1.0D, 1.0D, 1.0D), TARGET_PREDICATE);

        double d2 = d1;

        for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);
            if (entity1 instanceof com.x4yi.x4tweaker.utils.camera.FakeCameraEntity) continue;
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double)entity1.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (axisalignedbb.contains(vec3d)) {
                if (d2 >= 0.0D) {
                    mc.pointedEntity = entity1;
                    vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                    d2 = 0.0D;
                }
            } else if (raytraceresult != null) {
                double d3 = vec3d.distanceTo(raytraceresult.hitVec);
                if (d3 < d2 || d2 == 0.0D) {
                    if (entity1.getLowestRidingEntity() == entity.getLowestRidingEntity() && !entity1.canRiderInteract()) {
                        if (d2 == 0.0D) {
                            mc.pointedEntity = entity1;
                            vec3d3 = raytraceresult.hitVec;
                        }
                    } else {
                        mc.pointedEntity = entity1;
                        vec3d3 = raytraceresult.hitVec;
                        d2 = d3;
                    }
                }
            }
        }

        if (mc.pointedEntity != null && vec3d3 != null && flag && vec3d.distanceTo(vec3d3) > 3.0D) {
            mc.pointedEntity = null;
            mc.objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, (EnumFacing)null, new BlockPos(vec3d3));
        }

        if (mc.pointedEntity != null && vec3d3 != null && (d2 < d1 || mc.objectMouseOver == null)) {
            mc.objectMouseOver = new RayTraceResult(mc.pointedEntity, vec3d3);
        }
    }
}
