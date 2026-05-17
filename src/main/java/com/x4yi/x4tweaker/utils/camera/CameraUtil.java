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

    /**
     * Calculates mouse delta scaled by Minecraft's sensitivity settings.
     * Returns {scaledDX, scaledDY} respecting invert mouse setting.
     * Extracted from Freecam/CameraDetach to eliminate duplication.
     *
     * @param rawDX raw mouse delta X from Mouse.getDX()
     * @param rawDY raw mouse delta Y from Mouse.getDY()
     * @return float[2] with {scaledDX, scaledDY} (invert-mouse already applied to DY)
     */
    public static float[] calculateMouseDelta(int rawDX, int rawDY) {
        Minecraft mc = Minecraft.getMinecraft();
        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float sensitivity = f * f * f * 8.0F;
        float scaledDX = (float) rawDX * sensitivity;
        float scaledDY = (float) rawDY * sensitivity;
        int invert = mc.gameSettings.invertMouse ? -1 : 1;
        return new float[]{scaledDX, scaledDY * (float) invert};
    }
}
