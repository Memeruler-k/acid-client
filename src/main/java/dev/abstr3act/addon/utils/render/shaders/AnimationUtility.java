package dev.abstr3act.addon.utils.render.shaders;

import dev.abstr3act.addon.utils.math.MathUtility;

public class AnimationUtility {
    public static float deltaTime() {
        return FrameRateCounter.INSTANCE.getFps() > 5 ? 1.0F / FrameRateCounter.INSTANCE.getFps() : 0.016F;
    }

    public static float fast(float end, float start, float multiple) {
        float clampedDelta = MathUtility.clamp(deltaTime() * multiple, 0.0F, 1.0F);
        return (1.0F - clampedDelta) * end + clampedDelta * start;
    }

    public static float ease(float start, float end, float multiple) {
        float clampedDelta = MathUtility.clamp(deltaTime() * multiple, 0.0F, 1.0F);
        return start + (end - start) * clampedDelta;
    }

    public static float ease2(float start, float end, float multiple) {
        float distance = Math.abs(end - start);
        float adjustedMultiple = multiple * (0.5F + 0.5F * distance);
        float clampedDelta = MathUtility.clamp(deltaTime() * adjustedMultiple, 0.0F, 1.0F);
        float t = 1.0F - (float) Math.pow(1.0F - clampedDelta, 3.0);
        return start + (end - start) * t;
    }

    public static float interpolate(float startValue, float endValue, float progress) {
        return startValue + (endValue - startValue) * progress;
    }

    private float easeIn(float startValue, float endValue, float progress) {
        progress *= progress;
        return startValue + (endValue - startValue) * progress;
    }

    private float easeOut(float startValue, float endValue, float progress) {
        progress = 1.0F - (1.0F - progress) * (1.0F - progress);
        return startValue + (endValue - startValue) * progress;
    }
}
