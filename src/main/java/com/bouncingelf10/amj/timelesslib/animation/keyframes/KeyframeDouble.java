package com.bouncingelf10.amj.timelesslib.animation.keyframes;

import com.bouncingelf10.amj.timelesslib.animation.Easing;
import com.bouncingelf10.amj.timelesslib.animation.Interpolation;

public class KeyframeDouble {
    public final double timeSeconds;
    public final double value;
    public final Interpolation interpolation;
    public final Easing easing;

    public KeyframeDouble(double timeSeconds, double value, Interpolation interpolation, Easing easing) {
        if (timeSeconds < 0) throw new IllegalArgumentException("timeSeconds < 0");
        this.timeSeconds = timeSeconds;
        this.value = value;
        this.interpolation = interpolation;
        this.easing = easing;
    }

    public static KeyframeDouble of(double timeSeconds, double value) {
        return new KeyframeDouble(timeSeconds, value, null, null);
    }

    public static KeyframeDouble of(double timeSeconds, double value, Easing easing) {
        return new KeyframeDouble(timeSeconds, value, null, easing);
    }

    public static KeyframeDouble of(double timeSeconds, double value, Interpolation interp) {
        return new KeyframeDouble(timeSeconds, value, interp, null);
    }

    public static KeyframeDouble of(double timeSeconds, double value,  Easing easing, Interpolation interp) {
        return new KeyframeDouble(timeSeconds, value, interp, easing);
    }
}
