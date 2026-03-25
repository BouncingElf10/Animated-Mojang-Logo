package com.bouncingelf10.amj.timelesslib.countdown;

import com.bouncingelf10.amj.timelesslib.Duration;
import com.bouncingelf10.amj.timelesslib.TimeFormat;

import java.util.function.Supplier;

/**
 * @see CountdownManager
 */
public class ClientCountdownManager<T> extends CountdownManager<T> {
    public ClientCountdownManager(Supplier<T> contextProvider) {
        super(contextProvider);
    }

    public ClientCountdownManager(Supplier<T> contextProvider, int poolSize) {
        super(contextProvider, poolSize);
    }

    public ClientCountdown startClient(Duration total) {
        Countdown base = super.start(total, Duration.ofMillis(10));
        return new ClientCountdown(base);
    }

    public ClientCountdown startClientRealtime(Duration total) {
        Countdown base = super.start(total, Duration.ofMillis(10));
        return new ClientCountdown(base);
    }

    public ClientCountdown startClient(Duration total, Duration tickEvery) {
        Countdown base = super.start(total, tickEvery);
        return new ClientCountdown(base);
    }

    @SuppressWarnings("unchecked")
    public class ClientCountdown {
        private final Countdown base;

        public ClientCountdown(Countdown base) {
            this.base = base;
        }

        public ClientCountdown onTick(java.util.function.BiConsumer<T, Duration> handler) {
            base.onTick((java.util.function.BiConsumer<Object, Duration>) handler);
            return this;
        }

        public ClientCountdown onFinish(java.util.function.Consumer<T> handler) {
            base.onFinish((java.util.function.Consumer<Object>) handler);
            return this;
        }

        public ClientCountdown onThreshold(Duration threshold, java.util.function.Consumer<T> handler) {
            base.onThreshold(threshold, (java.util.function.Consumer<Object>) handler);
            return this;
        }

        public ClientCountdown every(Duration interval, java.util.function.Consumer<T> handler) {
            base.every(interval, (java.util.function.Consumer<Object>) handler);
            return this;
        }

        public boolean pause() { return base.pause(); }
        public boolean resume() { return base.resume(); }
        public boolean cancel() { return base.cancel(); }
        public boolean pauseOrUnpause() { return base.pauseOrUnpause(); }
        public boolean isPaused() { return base.isPaused(); }
        public boolean isCancelled() { return base.isCancelled(); }
        public boolean isFinished() { return base.isFinished(); }
        public Duration remaining() { return base.remaining(); }
        public String id() { return base.getId(); }
    }
}