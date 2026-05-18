package com.x4yi.x4tweaker.gui.v2.utils;

public final class MathHelper {
    private MathHelper() {}

    public static float clamp(float val, float min, float max) {
        return val < min ? min : (val > max ? max : val);
    }

    public static double clamp(double val, double min, double max) {
        return val < min ? min : (val > max ? max : val);
    }

    public static int clamp(int val, int min, int max) {
        return val < min ? min : (val > max ? max : val);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float map(float val, float inMin, float inMax, float outMin, float outMax) {
        return outMin + (outMax - outMin) * ((val - inMin) / (inMax - inMin));
    }

    public static float easeInOut(float t) {
        return t < 0.5f ? 2.0f * t * t : -1.0f + (4.0f - 2.0f * t) * t;
    }
}
