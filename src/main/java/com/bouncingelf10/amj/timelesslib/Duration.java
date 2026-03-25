package com.bouncingelf10.amj.timelesslib;

import java.math.RoundingMode;

public final class Duration implements Comparable<Duration> {
    public static final Duration ZERO = new Duration(0);
    public static final Duration DEFAULT_TICK = Duration.ofMillis(50);
    public static final Duration MINECRAFT_DAY = Duration.ofTicks(24000);
    public static final Duration MINECRAFT_HOUR = Duration.ofTicks(1000);
    public static final Duration MINECRAFT_MINUTE = Duration.ofTicks((long) (1000.0 / 60.0));

    public static final Duration TICK = Duration.ofTicks(1);
    public static final Duration SECOND = Duration.ofSeconds(1);
    public static final Duration MINUTE = Duration.ofMinutes(1);
    public static final Duration HOUR = Duration.ofHours(1);
    public static final Duration DAY = Duration.ofDays(1);
    private final long nanos;

    private Duration(long nanos) {
        this.nanos = nanos;
    }

    public static Duration of(java.time.Duration duration) {
        return ofNanos(duration.toNanos());
    }

    public static Duration of(long amount, DurationUnit unit) {
        return ofNanos(unit.toNanos(amount));
    }

    public static Duration of(double amount, DurationUnit unit) {
        return ofNanos(round(unit.toNanos(amount), RoundingMode.HALF_UP));
    }

    public static Duration of(long amount, DurationUnit unit, RoundingMode rounding) {
        return ofNanos(round(unit.toNanos((double) amount), rounding));
    }

    public static Duration of(double amount, DurationUnit unit, RoundingMode rounding) {
        return ofNanos(round(unit.toNanos(amount), rounding));
    }

    public static Duration ofNanos(long nanos) {
        return nanos == 0 ? ZERO : new Duration(nanos);
    }

    public static Duration ofMicros(long micros) { return of(micros, DurationUnit.MICROSECONDS); }
    public static Duration ofMillis(long millis) { return of(millis, DurationUnit.MILLISECONDS); }
    public static Duration ofTicks(long ticks) { return of(ticks, DurationUnit.TICKS); }
    public static Duration ofSeconds(long s) { return of(s, DurationUnit.SECONDS); }
    public static Duration ofSeconds(double s) { return of(s, DurationUnit.SECONDS); }
    public static Duration ofMinutes(long m) { return of(m, DurationUnit.MINUTES); }
    public static Duration ofHours(long h) { return of(h, DurationUnit.HOURS); }
    public static Duration ofDays(long d) { return of(d, DurationUnit.DAYS); }
    public static Duration zero() { return ZERO; }

    public long toNanos() { return nanos; }

    public double to(DurationUnit unit) { return unit.from(nanos); }
    public double toMicros() { return to(DurationUnit.MICROSECONDS); }
    public double toMillis() { return to(DurationUnit.MILLISECONDS); }
    public double toTicks() { return to(DurationUnit.TICKS); }
    public double toSeconds() { return to(DurationUnit.SECONDS); }
    public double toMinutes() { return to(DurationUnit.MINUTES); }
    public double toHours() { return to(DurationUnit.HOURS); }
    public double toDays() { return to(DurationUnit.DAYS); }

    public Duration plus(Duration duration) throws ArithmeticException { return ofNanos(Math.addExact(nanos, duration.nanos)); }
    public Duration plus(long amount, DurationUnit unit) throws ArithmeticException { return plus(of(amount, unit)); }
    public Duration minus(Duration duration) throws ArithmeticException { return ofNanos(Math.subtractExact(nanos, duration.nanos)); }
    public Duration minus(long amount, DurationUnit unit) throws ArithmeticException { return minus(of(amount, unit)); }
    public Duration multiply(long m) throws ArithmeticException { return ofNanos(Math.multiplyExact(nanos, m)); }
    public Duration multiply(double m) throws ArithmeticException { return ofNanos(round(nanos * m, RoundingMode.HALF_UP)); }
    public Duration divide(long d) throws ArithmeticException { return ofNanos(nanos / d); }
    public Duration divide(double d) throws ArithmeticException { return ofNanos(round(nanos / d, RoundingMode.HALF_UP)); }

    public Duration abs() { return nanos < 0 ? ofNanos(-nanos) : this; }
    public Duration negate() { return ofNanos(-nanos); }

    public boolean isZero() { return nanos == 0; }
    public boolean isNegative() { return nanos < 0; }
    public boolean isPositive() { return nanos > 0; }

    public boolean isLongerThan(Duration duration) { return nanos > duration.nanos; }
    public boolean isShorterThan(Duration duration) { return nanos < duration.nanos; }

    public Duration min(Duration duration) { return nanos <= duration.nanos ? this : duration; }
    public Duration max(Duration duration) { return nanos >= duration.nanos ? this : duration; }

    @Override
    public int compareTo(Duration duration) {
        return Long.compare(nanos, duration.nanos);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Duration d && d.nanos == nanos;
    }

    @Override
    public int hashCode() { return Long.hashCode(nanos); }

    @Override
    public String toString() {
        return "Duration: " + nanos + " ns";
    }

    public String toString(TimeFormat format) {
        return TimeFormatter.format(nanos, format);
    }

    private static long round(double value, RoundingMode mode) {
        return switch (mode) {
            case FLOOR -> (long) Math.floor(value);
            case CEILING -> (long) Math.ceil(value);
            case HALF_UP -> Math.round(value);
            case DOWN -> (long) (value >= 0 ? Math.floor(value) : Math.ceil(value));
            case UP -> (long) (value >= 0 ? Math.ceil(value) : Math.floor(value));
            default -> Math.round(value);
        };
    }
}