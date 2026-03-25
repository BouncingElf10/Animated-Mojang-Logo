package com.bouncingelf10.amj.timelesslib;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class TimeFormatter {
    private TimeFormatter() {}

    /**
     * Formats nanoseconds using a {@link TimeFormat}.
     *
     * @param nanos duration in nanoseconds
     * @param format output format
     * @return formatted string
     */
    public static String format(long nanos, TimeFormat format) {
        return format.apply(nanos);
    }

    public static String format(long amount, DurationUnit unit, TimeFormat format) {
        return format.apply(unit.toNanos(amount));
    }

    public static String format(long amount, TimeUnit unit, TimeFormat format) {
        return format.apply(unit.toNanos(amount));
    }

    public static String format(Duration duration, TimeFormat format) {
        return format.apply(duration.toNanos());
    }

    /**
     * Compact format such as {@code "1d 2h 3m 4s"} or {@code "3s 250ms"}.
     */
    public static String formatCompact(long nanos, DurationUnit minUnit) {
        TimeComponents components = new TimeComponents(nanos);
        StringBuilder output = new StringBuilder();

        if (components.days > 0) output.append(components.days).append("d ");
        if (components.hours > 0) output.append(components.hours).append("h ");
        if (components.minutes > 0) output.append(components.minutes).append("m ");
        if (components.seconds > 0) output.append(components.seconds).append("s ");
        if (components.millis > 0) output.append(components.millis).append("ms ");

        if (output.isEmpty()) {
            output.append("0").append(getUnitSuffix(minUnit));
        }

        return output.toString().trim();
    }

    /**
     * Verbose format such as {@code "2 days, 3 hours and 4 minutes"} or {@code "2 days, 3 hours, 4 minutes"}.
     */
    public static String formatVerbose(long nanos, String conjunction) {
        TimeComponents components = new TimeComponents(nanos);
        List<String> timeParts = new ArrayList<>();

        if (components.days > 0) timeParts.add(components.days + (components.days == 1 ? " day" : " days"));
        if (components.hours > 0) timeParts.add(components.hours + (components.hours == 1 ? " hour" : " hours"));
        if (components.minutes > 0) timeParts.add(components.minutes + (components.minutes == 1 ? " minute" : " minutes"));
        if (components.seconds > 0) timeParts.add(components.seconds + (components.seconds == 1 ? " second" : " seconds"));

        if (timeParts.isEmpty()) return "0 seconds";
        if (timeParts.size() == 1) return timeParts.get(0);

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < timeParts.size(); i++) {
            if (i > 0) {
                if (i == timeParts.size() - 1 && !conjunction.isEmpty()) {
                    output.append(" ").append(conjunction).append(" ");
                } else {
                    output.append(", ");
                }
            }
            output.append(timeParts.get(i));
        }

        return output.toString();
    }

    /**
     * Digital format such as {@code "01:22:05"} or {@code "00:10.532"}.
     */
    public static String formatDigital(long nanos, boolean includeMillis) {
        TimeComponents components = new TimeComponents(nanos);

        if (components.days > 0) {
            return includeMillis
                    ? String.format("%dd %02d:%02d:%02d.%03d", components.days, components.hours, components.minutes, components.seconds, components.millis)
                    : String.format("%dd %02d:%02d:%02d", components.days, components.hours, components.minutes, components.seconds);
        }
        if (components.hours > 0) {
            return includeMillis
                    ? String.format("%02d:%02d:%02d.%03d", components.hours, components.minutes, components.seconds, components.millis)
                    : String.format("%02d:%02d:%02d", components.hours, components.minutes, components.seconds);
        }
        return includeMillis
                ? String.format("%02d:%02d.%03d", components.minutes, components.seconds, components.millis)
                : String.format("%02d:%02d", components.minutes, components.seconds);
    }

    /**
     * ISO-8601 duration format such as {@code "PT30S"} or {@code "P2DT3H"}.
     */
    public static String formatISO8601(long nanos) {
        TimeComponents components = new TimeComponents(nanos);

        StringBuilder isoOutput = new StringBuilder("P");
        if (components.days > 0) isoOutput.append(components.days).append("D");

        boolean hasTime = components.hours > 0 || components.minutes > 0 || components.seconds > 0 || components.millis > 0;
        if (hasTime) {
            isoOutput.append("T");
            if (components.hours > 0) isoOutput.append(components.hours).append("H");
            if (components.minutes > 0) isoOutput.append(components.minutes).append("M");
            if (components.millis > 0) {
                isoOutput.append(components.seconds).append(".");
                isoOutput.append(String.format("%03d", components.millis)).append("S");
            } else if (components.seconds > 0) {
                isoOutput.append(components.seconds).append("S");
            }
        }

        if (!hasTime && components.days == 0) {
            isoOutput.append("T0S");
        }

        return isoOutput.toString();
    }

    /**
     * Gets a short suffix for a duration unit.
     */
    private static String getUnitSuffix(DurationUnit unit) {
        return switch (unit) {
            case DAYS -> "d";
            case HOURS -> "h";
            case MINUTES -> "m";
            case SECONDS -> "s";
            case MILLISECONDS -> "ms";
            case MICROSECONDS -> "μs";
            case NANOSECONDS -> "ns";
            case TICKS -> "t";
        };
    }

    static class TimeComponents {
        final long days;
        final int hours;
        final int minutes;
        final int seconds;
        final int millis;

        TimeComponents(long nanos) {
            long totalSeconds = nanos / 1_000_000_000L;
            this.days = totalSeconds / 86400L;

            long remainingSeconds = totalSeconds % 86400L;
            this.hours = (int) (remainingSeconds / 3600L);
            remainingSeconds %= 3600L;

            this.minutes = (int) (remainingSeconds / 60L);
            this.seconds = (int) (remainingSeconds % 60L);
            this.millis = (int) ((nanos % 1_000_000_000L) / 1_000_000L);
        }
    }
}
