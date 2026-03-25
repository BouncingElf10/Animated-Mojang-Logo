package com.bouncingelf10.amj.timelesslib;

public enum DurationUnit {
    NANOSECONDS(1),
    MICROSECONDS(1_000),
    MILLISECONDS(1_000_000),
    TICKS(50_000_000),
    SECONDS(1_000_000_000),
    MINUTES(60L * 1_000_000_000),
    HOURS(60L * 60 * 1_000_000_000),
    DAYS(24L * 60 * 60 * 1_000_000_000);

    private final long nanos;

    DurationUnit(long nanos) {
        this.nanos = nanos;
    }

    public long toNanos() {
        return this.nanos;
    }
    public long toNanos(long amount) {
        return amount * this.nanos;
    }
    public double toNanos(double amount) {
        return amount * this.nanos;
    }
    public double from(long nanos) {
        return (double) nanos / this.nanos;
    }
    public double to(double amount, DurationUnit target) {
        return (amount * this.nanos) / target.nanos;
    }
}