package com.x4yi.x4tweaker.combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.entity.EntityPlayerSP;


public interface IAttackOrchestrator {

    void attack(EntityPlayerSP player, EntityLivingBase target);


    boolean canAttack(EntityPlayerSP player);


    void onUpdate(EntityPlayerSP player);


    void reset();


    float getAimThreshold();
}
