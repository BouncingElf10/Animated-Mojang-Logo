package com.bouncingelf10.amj.timelesslib.animation;

import java.util.function.DoubleUnaryOperator;

/**
 * A collection of easing functions taken from <a href="https://easings.net/">easings.net</a>.
 */
public abstract class Easing {
    private final DoubleUnaryOperator operator;

    private Easing(DoubleUnaryOperator operator) { this.operator = operator; }

    public double apply(double x) {
        return clamp01(operator.applyAsDouble(clamp01(x)));
    }

    private static double clamp01(double a) { return a < 0 ? 0 : a > 1 ? 1 : a; }

    public static final Easing LINEAR = of(x -> x);

    public static final Easing EASE_IN_SINE = of(x -> 1 - Math.cos((x * Math.PI) / 2.0));
    public static final Easing EASE_OUT_SINE = of(x -> Math.sin((x * Math.PI) / 2.0));
    public static final Easing EASE_IN_OUT_SINE = of(x -> -(Math.cos(Math.PI * x) - 1) / 2.0);

    public static final Easing EASE_IN_QUAD = of(x -> x * x);
    public static final Easing EASE_OUT_QUAD = of(x -> 1 - (1 - x) * (1 - x));
    public static final Easing EASE_IN_OUT_QUAD = of(x -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2);

    public static final Easing EASE_IN_CUBIC = of(x -> x * x * x);
    public static final Easing EASE_OUT_CUBIC = of(x -> 1 - Math.pow(1 - x, 3));
    public static final Easing EASE_IN_OUT_CUBIC = of(x -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2);

    public static final Easing EASE_IN_QUART = of(x -> x * x * x * x);
    public static final Easing EASE_OUT_QUART = of(x -> 1 - Math.pow(1 - x, 4));
    public static final Easing EASE_IN_OUT_QUART = of(x -> x < 0.5 ? 8 * Math.pow(x, 4) : 1 - Math.pow(-2 * x + 2, 4) / 2);

    public static final Easing EASE_IN_QUINT = of(x -> Math.pow(x, 5));
    public static final Easing EASE_OUT_QUINT = of(x -> 1 - Math.pow(1 - x, 5));
    public static final Easing EASE_IN_OUT_QUINT = of(x -> x < 0.5 ? 16 * Math.pow(x, 5) : 1 - Math.pow(-2 * x + 2, 5) / 2);

    public static final Easing EASE_IN_EXPO = of(x -> x == 0 ? 0 : Math.pow(2, 10 * x - 10));
    public static final Easing EASE_OUT_EXPO = of(x -> x == 1 ? 1 : 1 - Math.pow(2, -10 * x));
    public static final Easing EASE_IN_OUT_EXPO = of(x -> {
        if (x == 0) return 0.0;
        if (x == 1) return 1.0;
        return x < 0.5 ? Math.pow(2, 20 * x - 10) / 2.0 : (2 - Math.pow(2, -20 * x + 10)) / 2.0;
    });

    public static final Easing EASE_IN_CIRC = of(x -> 1 - Math.sqrt(1 - Math.pow(x, 2)));
    public static final Easing EASE_OUT_CIRC = of(x -> Math.sqrt(1 - Math.pow(x - 1, 2)));
    public static final Easing EASE_IN_OUT_CIRC = of(x -> x < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2.0 : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2.0);

    public static final Easing EASE_IN_BACK = of(x -> {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    });
    public static final Easing EASE_OUT_BACK = of(x -> {
        final double c1 = 1.70158;
        final double c3 = c1 + 1;
        return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
    });
    public static final Easing EASE_IN_OUT_BACK = of(x -> {
        final double c1 = 1.70158;
        final double c2 = c1 * 1.525;
        return x < 0.5
                ? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2.0
                : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2.0;
    });

    public static final Easing EASE_IN_ELASTIC = of(x -> {
        if (x == 0) return 0.0;
        if (x == 1) return 1.0;
        final double c4 = (2 * Math.PI) / 3.0;
        return -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4);
    });
    public static final Easing EASE_OUT_ELASTIC = of(x -> {
        if (x == 0) return 0.0;
        if (x == 1) return 1.0;
        final double c4 = (2 * Math.PI) / 3.0;
        return Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1;
    });
    public static final Easing EASE_IN_OUT_ELASTIC = of(x -> {
        if (x == 0) return 0.0;
        if (x == 1) return 1.0;
        final double c5 = (2 * Math.PI) / 4.5;
        return x < 0.5
                ? -(Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * c5)) / 2
                : (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * c5)) / 2 + 1;
    });

    public static final Easing EASE_IN_BOUNCE = of(x -> 1 - bounceOut(1 - x));
    public static final Easing EASE_OUT_BOUNCE = of(Easing::bounceOut);
    public static final Easing EASE_IN_OUT_BOUNCE = of(x -> x < 0.5 ? (1 - bounceOut(1 - 2 * x)) / 2 : (1 + bounceOut(2 * x - 1)) / 2);

    private static Easing of(DoubleUnaryOperator operator) {
        return new Easing(operator) {};
    }

    private static double bounceOut(double x) {
        final double n1 = 7.5625;
        final double d1 = 2.75;
        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            double t = x - 1.5 / d1;
            return n1 * t * t + 0.75;
        } else if (x < 2.5 / d1) {
            double t = x - 2.25 / d1;
            return n1 * t * t + 0.9375;
        } else {
            double t = x - 2.625 / d1;
            return n1 * t * t + 0.984375;
        }
    }

    public static Easing createBezier(double x1, double y1, double x2, double y2) {
        final double cx1 = clamp01(x1), cy1 = clamp01(y1), cx2 = clamp01(x2), cy2 = clamp01(y2);
        return of(x -> cubicBezier(x, cx1, cy1, cx2, cy2));
    }

    private static double cubicBezier(double x, double x1, double y1, double x2, double y2) {
        DoubleUnaryOperator sampleCurveX = (t) -> ((3 * x1 - 3 * x2 + 1) * t * t * t) + ((-6 * x1 + 3 * x2) * t * t) + (3 * x1 * t);
        DoubleUnaryOperator sampleCurveY = (t) -> ((3 * y1 - 3 * y2 + 1) * t * t * t) + ((-6 * y1 + 3 * y2) * t * t) + (3 * y1 * t);

        double low = 0.0, high = 1.0, t = x;
        for (int i = 0; i < 24; i++) {
            t = (low + high) * 0.5;
            double xt = sampleCurveX.applyAsDouble(t);
            if (Math.abs(xt - x) < 1e-6) break;
            if (xt > x) high = t; else low = t;
        }
        double y = sampleCurveY.applyAsDouble(t);
        return clamp01(y);
    }
}