package com.bouncingelf10.amj.timelesslib;

import java.util.ArrayList;
import java.util.List;

import static com.bouncingelf10.amj.timelesslib.TimeFormatter.*;

public enum TimeFormat {
    /**
     * Compact format such as {@code "1d 2h 3m 4s"} omitting milliseconds.
     */
    COMPACT {
        @Override public String apply(long nanos) {
            return formatCompact(nanos, DurationUnit.SECONDS);
        }
    },
    /**
     * Compact format that includes milliseconds, e.g. {@code "3s 250ms"}.
     */
    COMPACT_MILLIS {
        @Override public String apply(long nanos) {
            return formatCompact(nanos, DurationUnit.MILLISECONDS);
        }
    },
    /**
     * Verbose English format, e.g. {@code "2 days, 3 hours and 4 minutes"}.
     */
    VERBOSE {
        @Override public String apply(long nanos) {
            return formatVerbose(nanos, "and");
        }
    },
    /**
     * Verbose format without a conjunction, e.g. {@code "2 days, 3 hours, 4 minutes"}.
     */
    VERBOSE_SIMPLE {
        @Override public String apply(long nanos) {
            return formatVerbose(nanos, "");
        }
    },
    /**
     * Digital clock-style formatting such as {@code "01:22:05"}.
     */
    DIGITAL {
        @Override public String apply(long nanos) {
            return formatDigital(nanos, false);
        }
    },
    /**
     * Digital formatting including milliseconds such as {@code "00:10.532"}.
     */
    DIGITAL_MILLIS {
        @Override public String apply(long nanos) {
            return formatDigital(nanos, true);
        }
    },
    /**
     * Returns only the largest non-zero component, e.g. {@code "3h"} or {@code "20m"}.
     */
    MINIMAL {
        @Override public String apply(long nanos) {
            TimeFormatter.TimeComponents components = new TimeFormatter.TimeComponents(nanos);
            if (components.days > 0) return components.days + "d";
            if (components.hours > 0) return components.hours + "h";
            if (components.minutes > 0) return components.minutes + "m";
            if (components.seconds > 0) return components.seconds + "s";
            return components.millis + "ms";
        }
    },
    /**
     * Returns the first two non-zero components, e.g. {@code "3h 20m"}.
     */
    MINIMAL_TWO {
        @Override public String apply(long nanos) {
            TimeFormatter.TimeComponents components = new TimeFormatter.TimeComponents(nanos);
            List<String> firstTwoParts = new ArrayList<>();

            if (components.days > 0) firstTwoParts.add(components.days + "d");
            if (components.hours > 0) firstTwoParts.add(components.hours + "h");
            if (components.minutes > 0) firstTwoParts.add(components.minutes + "m");
            if (components.seconds > 0) firstTwoParts.add(components.seconds + "s");
            if (components.millis > 0) firstTwoParts.add(components.millis + "ms");

            if (firstTwoParts.isEmpty()) return "0ms";
            if (firstTwoParts.size() == 1) return firstTwoParts.get(0);
            return firstTwoParts.get(0) + " " + firstTwoParts.get(1);
        }
    },
    /**
     * ISO-8601 duration format, such as {@code "PT30S"} or {@code "P2DT3H"}.
     */
    ISO_8601 {
        @Override public String apply(long nanos) {
            return formatISO8601(nanos);
        }
    },
    /**
     * Debug output including all units down to nanoseconds.
     */
    DEBUG {
        @Override public String apply(long nanos) {
            TimeFormatter.TimeComponents components = new TimeFormatter.TimeComponents(nanos);
            long microseconds = (nanos % 1_000_000_000L) / 1_000L;
            long remainingNanos = nanos % 1_000L;

            return String.format(
                    "Days: %d, Hours: %d, Minutes: %d, Seconds: %d, Milliseconds: %d, Microseconds: %d, Nanoseconds: %d",
                    components.days, components.hours, components.minutes, components.seconds,
                    components.millis, microseconds, remainingNanos
            );
        }
    };
    /**
     * Converts a nanosecond duration into a formatted string.
     *
     * @param nanos duration in nanoseconds
     * @return formatted representation of the duration
     */
    public abstract String apply(long nanos);
}

