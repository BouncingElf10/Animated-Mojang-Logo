package com.bouncingelf10.amj.timelesslib.animation.channels;

import com.bouncingelf10.amj.timelesslib.animation.Interpolation;
import com.bouncingelf10.amj.timelesslib.animation.*;
import com.bouncingelf10.amj.timelesslib.animation.keyframes.KeyframeDouble;
import com.bouncingelf10.amj.timelesslib.Duration;

import java.util.*;
import java.util.function.Consumer;

public class ChannelDouble {
    private final String name;
    private final List<KeyframeDouble> keyframes = new ArrayList<>();
    private Interpolation defaultInterpolation = null;
    private Easing defaultEasing = null;
    private Consumer<Double> boundConsumer = value -> {};

    public ChannelDouble(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String name() {
        return name;
    }

    /**
     * Adds a keyframe at the specified time in seconds.
     */
    public ChannelDouble keyframe(double timeSeconds, double value) {
        return addKeyframe(KeyframeDouble.of(timeSeconds, value));
    }
    /**
     * Adds a keyframe at the specified time in seconds.
     * Also sets the easing for the keyframe. <br>
     * Note: Keyframes can override all previous defaults and follow a hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelDouble} > {@link KeyframeDouble}
     */
    public ChannelDouble keyframe(double timeSeconds, double value, Easing easing) {
        return addKeyframe(KeyframeDouble.of(timeSeconds, value, easing));
    }
    /**
     * Adds a keyframe at the specified time in seconds.
     * Also sets the interpolation for the keyframe. <br>
     * Note: Keyframes can override all previous defaults and follow a hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelDouble} > {@link KeyframeDouble}
     */
    public ChannelDouble keyframe(double timeSeconds, double value, Interpolation interpolation) {
        return addKeyframe(KeyframeDouble.of(timeSeconds, value, interpolation));
    }
    /**
     * Adds a keyframe at the specified time in seconds.
     * Also sets the interpolation and easing for the keyframe. <br>
     * Note: Keyframes can override all previous defaults and follow a hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelDouble} > {@link KeyframeDouble}
     */
    public ChannelDouble keyframe(double timeSeconds, double value, Interpolation interpolation, Easing easing) {
        return addKeyframe(KeyframeDouble.of(timeSeconds, value, easing, interpolation));
    }
    /**
     * Adds a keyframe at the specified time in seconds.
     * Also sets the easing and interpolation for the keyframe. <br>
     * Note: Keyframes can override all previous defaults and follow a hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelDouble} > {@link KeyframeDouble}
     */
    public ChannelDouble keyframe(double timeSeconds, double value, Easing easing, Interpolation interpolation) {
        return addKeyframe(KeyframeDouble.of(timeSeconds, value, easing, interpolation));
    }

    public ChannelDouble keyframe(Duration duration, double value) {
        double seconds = duration.toNanos() / 1e9;
        return keyframe(seconds, value);
    }

    public ChannelDouble keyframe(Duration duration, double value, Easing easing) {
        double seconds = duration.toNanos() / 1e9;
        return keyframe(seconds, value, easing);
    }

    public ChannelDouble keyframe(Duration duration, double value, Interpolation interpolation) {
        double seconds = duration.toNanos() / 1e9;
        return keyframe(seconds, value, interpolation);
    }

    public ChannelDouble keyframe(Duration duration, double value, Easing easing, Interpolation interpolation) {
        double seconds = duration.toNanos() / 1e9;
        return keyframe(seconds, value, interpolation, easing);
    }

    /**
     * Adds a keyframe using a keyframe object. <br>
     * I advise you use the provided methods to create keyframes. ({@link #keyframe(double, double)}, etc.)
     */
    public ChannelDouble addKeyframe(KeyframeDouble keyframe) {
        keyframes.add(keyframe);
        keyframes.sort(Comparator.comparingDouble(k -> k.timeSeconds));
        return this;
    }
    /**
     * Sets the default interpolation for the channel. <br>
     * Note: Channel defaults override timeline defaults but can still be overwritten by keyframes following this hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelDouble} > {@link KeyframeDouble}
     */
    public ChannelDouble defaultInterpolation(Interpolation interpolation) {
        this.defaultInterpolation = Objects.requireNonNull(interpolation);
        return this;
    }
    /**
     * Sets the default easing for the channel. <br>
     * Note: Channel defaults override timeline defaults but can still be overwritten by keyframes following this hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelDouble} > {@link KeyframeDouble}
     */
    public ChannelDouble defaultEasing(Easing easing) {
        this.defaultEasing = Objects.requireNonNull(easing);
        return this;
    }

    /**
     * Binds the channel to a consumer. <br>
     * This is the way to assign a variable to the output of the channel. <br>
     * E.g. {@code channel.bind(value -> System.out.println(value));} or<br>
     * {@code channel.bind(myVariable::setValue);}
     */
    public ChannelDouble bind(Consumer<Double> consumer) {
        this.boundConsumer = Objects.requireNonNull(consumer);
        return this;
    }

    public double computeDurationSeconds() {
        if (keyframes.isEmpty()) return 0.0;
        return keyframes.get(keyframes.size() - 1).timeSeconds;
    }

    /**
     * Evaluates the channel at the specified time in seconds. You mostly shouldn't call this method directly.
     */
    public void evaluateAt(double timeSeconds, Interpolation timelineDefaultInterpolation, Easing timelineDefaultEasing) {
        if (keyframes.isEmpty()) {
            boundConsumer.accept(0.0);
            return;
        }

        if (timeSeconds <= keyframes.get(0).timeSeconds) {
            boundConsumer.accept(keyframes.get(0).value);
            return;
        }

        if (timeSeconds >= keyframes.get(keyframes.size() - 1).timeSeconds) {
            boundConsumer.accept(keyframes.get(keyframes.size() - 1).value);
            return;
        }

        int index = Collections.binarySearch(keyframes, KeyframeDouble.of(timeSeconds, 0), Comparator.comparingDouble(k -> k.timeSeconds));
        if (index >= 0) {
            KeyframeDouble exact = keyframes.get(index);
            boundConsumer.accept(exact.value);
            return;
        }
        int insertionPoint = -(index + 1);

        KeyframeDouble leftFrame = keyframes.get(insertionPoint - 1);
        KeyframeDouble rightFrame = keyframes.get(insertionPoint);

        double span = rightFrame.timeSeconds - leftFrame.timeSeconds;
        double t = span == 0.0 ? 0.0 : (timeSeconds - leftFrame.timeSeconds) / span;

        Interpolation segmentInterpolation = leftFrame.interpolation != null ? leftFrame.interpolation : (defaultInterpolation != null ? defaultInterpolation : timelineDefaultInterpolation);
        Easing easing = leftFrame.easing != null ? leftFrame.easing : (defaultEasing != null ? defaultEasing : timelineDefaultEasing);

        double outputValue;
        switch (segmentInterpolation) {
            case STEP -> outputValue = leftFrame.value;
            case LINEAR -> outputValue = lerp(leftFrame.value, rightFrame.value, t);
            case EASE -> {
                double easedT = easing == null ? Easing.LINEAR.apply(t) : easing.apply(t);
                outputValue = lerp(leftFrame.value, rightFrame.value, easedT);
            }
            case CATMULL -> {
                int i = insertionPoint - 1;
                int size = keyframes.size();

                int i0 = Math.max(0, i - 1);
                int i2 = i + 1;
                int i3 = Math.min(size - 1, i + 2);

                double p0 = keyframes.get(i0).value;
                double p1 = keyframes.get(i).value;
                double p2 = keyframes.get(i2).value;
                double p3 = keyframes.get(i3).value;

                outputValue = catmullRom(p0, p1, p2, p3, t);
            }
            default -> throw new IllegalStateException("Invalid interpolation type: " + segmentInterpolation);
        }

        boundConsumer.accept(outputValue);
    }
    
    private double catmullRom(double p0, double p1, double p2, double p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2*p0 - 5*p1 + 4*p2 - p3) * t2 + (-p0 + 3*p1 - 3*p2 + p3) * t3);
    }

    private static double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }
}