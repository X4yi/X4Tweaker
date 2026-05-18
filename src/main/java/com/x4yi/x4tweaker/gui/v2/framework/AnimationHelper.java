package com.x4yi.x4tweaker.gui.v2.framework;

public final class AnimationHelper {
    private AnimationHelper() {}

    public static float lerp(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;
        return current + diff * speed;
    }

    public static int lerpInt(int current, int target, float speed) {
        return (int) lerp((float) current, (float) target, speed);
    }

    public static float easeInOut(float t) {
        return t < 0.5f ? 2.0f * t * t : -1.0f + (4.0f - 2.0f * t) * t;
    }

    public static float smoothDamp(float current, float target, float[] velocityRef, float smoothTime, float deltaTime) {
        float omega = 2.0f / smoothTime;
        float x = omega * deltaTime;
        float exp = 1.0f / (1.0f + x + 0.48f * x * x + 0.235f * x * x * x);
        float change = current - target;
        float maxChange = velocityRef[0] * smoothTime;
        change = clamp(change, -maxChange, maxChange);
        target = current - change;
        float temp = (velocityRef[0] + omega * change) * deltaTime;
        velocityRef[0] = (velocityRef[0] - omega * temp) * exp;
        return target + (change + temp) * exp;
    }

    private static float clamp(float val, float min, float max) {
        return val < min ? min : (val > max ? max : val);
    }
}
