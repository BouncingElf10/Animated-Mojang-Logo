package com.bouncingelf10.amj.timelesslib.countdown;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.timelesslib.Duration;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CountdownManager<T> {
    final ScheduledThreadPoolExecutor executor;
    final Map<String, Countdown> activeCountdowns = new ConcurrentHashMap<>();
    final Supplier<T> contextProvider;
    final BiConsumer<T, Runnable> mainThreadDispatcher;

    public CountdownManager(Supplier<T> contextProvider, BiConsumer<T, Runnable> mainThreadDispatcher) {
        this(contextProvider, mainThreadDispatcher, Math.max(1, Runtime.getRuntime().availableProcessors()));
    }

    public CountdownManager(Supplier<T> contextProvider, BiConsumer<T, Runnable> mainThreadDispatcher, int poolSize) {
        this.contextProvider = Objects.requireNonNull(contextProvider, "contextProvider");
        this.mainThreadDispatcher = Objects.requireNonNull(mainThreadDispatcher, "mainThreadDispatcher");
        this.executor = new ScheduledThreadPoolExecutor(poolSize);
        this.executor.setRemoveOnCancelPolicy(true);
    }

    public CountdownManager(Supplier<T> contextProvider) {
        this(contextProvider, detectDispatcher(contextProvider));
    }

    public CountdownManager(Supplier<T> contextProvider, int poolSize) {
        this(contextProvider, detectDispatcher(contextProvider), poolSize);
    }

    static <T> BiConsumer<T, Runnable> detectDispatcher(Supplier<T> contextSupplier) {
        T context = contextSupplier.get();
        if (context == null)
            throw new IllegalArgumentException("Context provider returned null when probing for execute(Runnable). Provide an explicit dispatcher instead.");

        try {
            Method executeMethod = context.getClass().getMethod("execute", Runnable.class);
            executeMethod.setAccessible(true);
            return (ctx, runnable) -> {
                try { executeMethod.invoke(ctx, runnable); }
                catch (RuntimeException re) { throw re; }
                catch (Exception e) {
                    AnimatedMojangLogoClient.LOGGER.error("Failed to dispatch task to main thread", e);
                    throw new RuntimeException(e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Context type " + context.getClass().getName() + " does not expose execute(Runnable). Provide explicit dispatcher.", e);
        }
    }

    public Countdown start(Duration totalDuration) {
        Objects.requireNonNull(totalDuration);
        return start(totalDuration, Duration.ofMillis(50));
    }

    public Countdown start(Duration totalDuration, Duration tickInterval) {
        Objects.requireNonNull(totalDuration);
        Objects.requireNonNull(tickInterval);

        Countdown countdown = new Countdown(this, totalDuration, tickInterval);
        activeCountdowns.put(countdown.getId(), countdown);
        countdown.start();
        return countdown;
    }

    public Optional<Countdown> get(String id) {
        return Optional.ofNullable(activeCountdowns.get(id));
    }
}
