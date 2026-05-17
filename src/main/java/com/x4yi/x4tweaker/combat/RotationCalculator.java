package com.x4yi.x4tweaker.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.entity.EntityPlayerSP;


public class RotationCalculator {
    private static final float DEFAULT_SMOOTH_DIVISOR = 2.0f;

    private float smoothDivisor;
    private float currentYaw = 0f;
    private float currentPitch = 0f;
    private boolean hasInitialized = false;

    public RotationCalculator() {
        this(DEFAULT_SMOOTH_DIVISOR);
    }

    public RotationCalculator(float smoothDivisor) {
        this.smoothDivisor = Math.max(1.0f, smoothDivisor);
    }

    public void setSmoothDivisor(float smoothDivisor) {
        this.smoothDivisor = Math.max(1.0f, smoothDivisor);
    }

    public float getSmoothDivisor() {
        return smoothDivisor;
    }


    public float[] calculateRotations(EntityLivingBase player, EntityLivingBase target) {
        double x = target.posX - player.posX;
        double y = (target.posY + target.getEyeHeight()) - (player.posY + player.getEyeHeight());
        double z = target.posZ - player.posZ;

        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float) (Math.toDegrees(Math.atan2(z, x)) - 90.0F);
        float pitch = MathHelper.clamp((float) -Math.toDegrees(Math.atan2(y, dist)), -90.0f, 90.0f);

        return new float[]{yaw, pitch};
    }


    public float[] applySmoothRotation(EntityPlayerSP player, EntityLivingBase target) {
        float[] targetRotations = calculateRotations(player, target);

        if (!hasInitialized) {
            currentYaw = player.rotationYaw;
            currentPitch = player.rotationPitch;
            hasInitialized = true;
        }

        float yawDiff = MathHelper.wrapDegrees(targetRotations[0] - currentYaw);
        float pitchDiff = targetRotations[1] - currentPitch;

        currentYaw += yawDiff / smoothDivisor;
        currentPitch = MathHelper.clamp(currentPitch + pitchDiff / smoothDivisor, -90.0f, 90.0f);

        player.rotationYaw = currentYaw;
        player.rotationPitch = currentPitch;

        return new float[]{yawDiff, pitchDiff};
    }

    public void reset() {
        hasInitialized = false;
        currentYaw = 0f;
        currentPitch = 0f;
    }
}
