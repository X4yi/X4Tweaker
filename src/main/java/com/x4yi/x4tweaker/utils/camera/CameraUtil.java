package com.x4yi.x4tweaker.utils.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CameraUtil {
    private static Field renderViewEntityField;
    private static Method onUpdateWalkingPlayerMethod;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        try {
            renderViewEntityField = ReflectionHelper.findField(Minecraft.class, "renderViewEntity", "field_175622_Z");
            renderViewEntityField.setAccessible(true);

            onUpdateWalkingPlayerMethod = ReflectionHelper.findMethod(EntityPlayerSP.class, "onUpdateWalkingPlayer", "func_175161_p");
            onUpdateWalkingPlayerMethod.setAccessible(true);

            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void forceUpdateWalkingPlayer(Minecraft mc) {
        if (!initialized) init();
        if (mc.player == null || renderViewEntityField == null || onUpdateWalkingPlayerMethod == null) return;

        try {
            Entity backup = (Entity) renderViewEntityField.get(mc);
            if (backup != mc.player) {

                renderViewEntityField.set(mc, mc.player);


                onUpdateWalkingPlayerMethod.invoke(mc.player);


                renderViewEntityField.set(mc, backup);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
