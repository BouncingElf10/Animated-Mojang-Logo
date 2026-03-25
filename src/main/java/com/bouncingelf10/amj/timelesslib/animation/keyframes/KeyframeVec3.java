package com.bouncingelf10.amj.timelesslib.animation.keyframes;

import com.bouncingelf10.amj.timelesslib.animation.Interpolation;
import com.bouncingelf10.amj.timelesslib.animation.Easing;
import net.minecraft.world.phys.Vec3;

public class KeyframeVec3 {
    public final double timeSeconds;
    public final Vec3 value;
    public final Interpolation interpolation;
    public final Easing easing;

    public KeyframeVec3(double timeSeconds, Vec3 value, Interpolation interpolation, Easing easing) {
        if (timeSeconds < 0) throw new IllegalArgumentException("timeSeconds < 0");
        this.timeSeconds = timeSeconds;
        this.value = value;
        this.interpolation = interpolation;
        this.easing = easing;
    }

    public static KeyframeVec3 of(double timeSeconds, Vec3 value) {
        return new KeyframeVec3(timeSeconds, value, null, null);
    }

    public static KeyframeVec3 of(double timeSeconds, Vec3 value, Easing easing) {
        return new KeyframeVec3(timeSeconds, value, null, easing);
    }

    public static KeyframeVec3 of(double timeSeconds, Vec3 value, Interpolation interp) {
        return new KeyframeVec3(timeSeconds, value, interp, null);
    }

    public static KeyframeVec3 of(double timeSeconds, Vec3 value, Easing easing, Interpolation interp) {
        return new KeyframeVec3(timeSeconds, value, interp, easing);
    }
}
