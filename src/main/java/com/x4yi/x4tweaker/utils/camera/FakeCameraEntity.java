package com.x4yi.x4tweaker.utils.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovementInput;

public class FakeCameraEntity extends EntityOtherPlayerMP {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public FakeCameraEntity() {
        super(mc.world, mc.player.getGameProfile());


        this.copyLocationAndAnglesFrom(mc.player);
        this.rotationYawHead = mc.player.rotationYawHead;
        this.renderYawOffset = mc.player.renderYawOffset;

        this.noClip = true;
        this.stepHeight = 0.0F;


        this.setCustomNameTag(mc.player.getName() + " (Camera)");
        this.setInvisible(true);
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {

    }

    @Override
    public void applyEntityCollision(Entity entityIn) {

    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public net.minecraft.util.math.AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Override
    public net.minecraft.util.math.AxisAlignedBB getCollisionBox(Entity entityIn) {
        return null;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationYawHead = this.rotationYawHead;
    }

    public void moveCamera(MovementInput input, float speed, float verticalSpeed, boolean isSprinting) {
        float f = input.moveStrafe;
        float f1 = input.moveForward;
        float up = 0;

        if (input.jump) up += 1;
        if (input.sneak) up -= 1;

        if (f != 0.0F || f1 != 0.0F) {
            float moveSpeed = isSprinting ? speed * 2.0f : speed;
            float yaw = this.rotationYaw;

            if (f1 != 0.0F) {
                if (f > 0.0F) yaw += (f1 > 0.0F ? -45 : 45);
                else if (f < 0.0F) yaw += (f1 > 0.0F ? 45 : -45);
                f = 0.0F;
                if (f1 > 0.0F) f1 = 1.0F;
                else if (f1 < 0.0F) f1 = -1.0F;
            }

            double rad = Math.toRadians(yaw + 90.0F);
            this.posX += (f1 * Math.cos(rad) + f * Math.sin(rad)) * moveSpeed;
            this.posZ += (f1 * Math.sin(rad) - f * Math.cos(rad)) * moveSpeed;
        }

        if (up != 0) {
            this.posY += up * verticalSpeed;
        }
    }
}
