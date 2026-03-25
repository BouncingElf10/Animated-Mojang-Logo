package com.bouncingelf10.amj.timelesslib.animation.channels;

import com.bouncingelf10.amj.timelesslib.animation.*;
import com.bouncingelf10.amj.timelesslib.animation.keyframes.KeyframeVec3;
import com.bouncingelf10.amj.timelesslib.Duration;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Consumer;

public class ChannelVec3 {
    private final String name;
    private final List<KeyframeVec3> keyframes = new ArrayList<>();
    private Interpolation defaultInterpolation = Interpolation.EASE;
    private Easing defaultEasing = Easing.LINEAR;
    private Consumer<Vec3> boundConsumer = vec -> {};

    public ChannelVec3(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String name() {
        return name;
    }

    /**
     * Adds a keyframe at the specified time in seconds.
     */
    public ChannelVec3 keyframe(double timeSeconds, Vec3 value) {
        return addKeyframe(KeyframeVec3.of(timeSeconds, value));
    }
    /**
     * Adds a keyframe at the specified time in seconds.
     * Also sets the easing for the keyframe. <br>
     * Note: Keyframes can override all previous defaults and follow a hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelVec3} > {@link KeyframeVec3}
     */
    public ChannelVec3 keyframe(double timeSeconds, Vec3 value, Easing easing) {
        return addKeyframe(KeyframeVec3.of(timeSeconds, value, easing));
    }
    /**
     * Adds a keyframe at the specified time in seconds.
     * Also sets the interpolation for the keyframe. <br>
     * Note: Keyframes can override all previous defaults and follow a hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelVec3} > {@link KeyframeVec3}
     */
    public ChannelVec3 keyframe(double timeSeconds, Vec3 value, Interpolation interpolation) {
        return addKeyframe(KeyframeVec3.of(timeSeconds, value, interpolation));
    }
    /**
     * Adds a keyframe at the specified time in seconds.
     * Also sets the interpolation and easing for the keyframe. <br>
     * Note: Keyframes can override all previous defaults and follow a hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelVec3} > {@link KeyframeVec3}
     */
    public ChannelVec3 keyframe(double timeSeconds, Vec3 value, Interpolation interpolation, Easing easing) {
        return addKeyframe(KeyframeVec3.of(timeSeconds, value, easing, interpolation));
    }
    /**
     * Adds a keyframe at the specified time in seconds.
     * Also sets the easing and interpolation for the keyframe. <br>
     * Note: Keyframes can override all previous defaults and follow a hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelVec3} > {@link KeyframeVec3}
     */
    public ChannelVec3 keyframe(double timeSeconds, Vec3 value, Easing easing, Interpolation interpolation) {
        return addKeyframe(KeyframeVec3.of(timeSeconds, value, easing, interpolation));
    }

    public ChannelVec3 keyframe(Duration duration, Vec3 value) {
        double seconds = duration.toNanos() / 1e9;
        return keyframe(seconds, value);
    }

    public ChannelVec3 keyframe(Duration duration, Vec3 value, Easing easing) {
        double seconds = duration.toNanos() / 1e9;
        return keyframe(seconds, value, easing);
    }

    public ChannelVec3 keyframe(Duration duration, Vec3 value, Interpolation interpolation) {
        double seconds = duration.toNanos() / 1e9;
        return keyframe(seconds, value, interpolation);
    }

    public ChannelVec3 keyframe(Duration duration, Vec3 value, Easing easing, Interpolation interpolation) {
        double seconds = duration.toNanos() / 1e9;
        return keyframe(seconds, value, interpolation, easing);
    }
    /**
     * Adds a keyframe using a keyframe object. <br>
     * I advise you use the provided methods to create keyframes. ({@link #keyframe(double, Vec3)}, etc.)
     */
    public ChannelVec3 addKeyframe(KeyframeVec3 keyframe) {
        keyframes.add(keyframe);
        keyframes.sort(Comparator.comparingDouble(k -> k.timeSeconds));
        return this;
    }
    /**
     * Sets the default interpolation for the channel. <br>
     * Note: Channel defaults override timeline defaults but can still be overwritten by keyframes following this hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelVec3} > {@link KeyframeVec3}
     */
    public ChannelVec3 defaultInterpolation(Interpolation interpolation) {
        this.defaultInterpolation = Objects.requireNonNull(interpolation);
        return this;
    }
    /**
     * Sets the default easing for the channel. <br>
     * Note: Channel defaults override timeline defaults but can still be overwritten by keyframes following this hierarchy: <br>
     * {@link AnimationTimeline} > {@link ChannelVec3} > {@link KeyframeVec3}
     */
    public ChannelVec3 defaultEasing(Easing easing) {
        this.defaultEasing = Objects.requireNonNull(easing);
        return this;
    }
    /**
     * Binds the channel to a consumer. <br>
     * This is the way to assign a variable to the output of the channel. <br>
     * E.g. {@code channel.bind(value -> System.out.println(value));} or<br>
     * {@code channel.bind(myVariable::setValue);}
     */
    public ChannelVec3 bind(Consumer<Vec3> consumer) {
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
            boundConsumer.accept(new Vec3(0.0, 0.0, 0.0));
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

        int index = Collections.binarySearch(keyframes, KeyframeVec3.of(timeSeconds, Vec3.ZERO), Comparator.comparingDouble(k -> k.timeSeconds));
        if (index >= 0) {
            KeyframeVec3 exact = keyframes.get(index);
            boundConsumer.accept(exact.value);
            return;
        }
        int insertionPoint = -(index + 1);

        KeyframeVec3 leftFrame = keyframes.get(insertionPoint - 1);
        KeyframeVec3 rightFrame = keyframes.get(insertionPoint);

        double span = rightFrame.timeSeconds - leftFrame.timeSeconds;
        double t = span == 0.0 ? 0.0 : (timeSeconds - leftFrame.timeSeconds) / span;

        Interpolation segmentInterpolation = leftFrame.interpolation != null ? leftFrame.interpolation : (defaultInterpolation != null ? defaultInterpolation : timelineDefaultInterpolation);
        Easing easing = leftFrame.easing != null ? leftFrame.easing : (defaultEasing != null ? defaultEasing : timelineDefaultEasing);

        Vec3 outputValue;
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

                Vec3 p0 = keyframes.get(i0).value;
                Vec3 p1 = keyframes.get(i).value;
                Vec3 p2 = keyframes.get(i2).value;
                Vec3 p3 = keyframes.get(i3).value;

                outputValue = new Vec3(
                        catmullRom(p0.x, p1.x, p2.x, p3.x, t),
                        catmullRom(p0.y, p1.y, p2.y, p3.y, t),
                        catmullRom(p0.z, p1.z, p2.z, p3.z, t)
                );
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

    private static Vec3 lerp(Vec3 start, Vec3 end, double t) {
        return new Vec3(
                start.x + (end.x - start.x) * t,
                start.y + (end.y - start.y) * t,
                start.z + (end.z - start.z) * t
        );
    }
}