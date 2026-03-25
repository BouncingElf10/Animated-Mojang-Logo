package com.bouncingelf10.amj.timelesslib.countdown;

import com.bouncingelf10.amj.AnimatedMojangLogoClient;
import com.bouncingelf10.amj.timelesslib.Duration;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Countdown {
    private final CountdownManager<?> manager;

    private final String id = UUID.randomUUID().toString();
    private final Duration totalDuration;
    private final long totalNanos;
    private final long tickNanos;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);

    private volatile long endTimeNanos;
    private volatile long remainingOnPause = -1L;
    private volatile ScheduledFuture<?> scheduledTask;

    private final CopyOnWriteArrayList<BiConsumer<Object, Duration>> tickHandlers = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Consumer<Object>> finishHandlers = new CopyOnWriteArrayList<>();
    private final NavigableMap<Long, List<Consumer<Object>>> thresholds = new ConcurrentSkipListMap<>();

    private final Map<Long, List<Consumer<Object>>> intervalHandlers = new ConcurrentHashMap<>();
    private final Map<Long, Long> nextElapsedToFire = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private <T> CountdownManager<T> typedManager() {
        return (CountdownManager<T>) manager;
    }

    Countdown(CountdownManager<?> manager, Duration totalDuration, Duration tickInterval) {
        this.manager = manager;
        this.totalDuration = totalDuration;
        this.totalNanos = totalDuration.toNanos();
        this.tickNanos = Math.max(1L, tickInterval.toNanos());
    }

    void start() {
        this.endTimeNanos = System.nanoTime() + totalNanos;
        intervalHandlers.keySet().forEach(interval -> nextElapsedToFire.putIfAbsent(interval, interval));
        scheduleNextTick();
    }

    private void scheduleNextTick() {
        if (cancelled.get() || finished.get()) return;

        long remainingNanos = Math.max(0L, endTimeNanos - System.nanoTime());
        long delayNanos = Math.min(tickNanos, remainingNanos);

        scheduledTask = typedManager().executor.schedule(() -> {
            Object context = typedManager().contextProvider.get();
            if (context == null) {
                AnimatedMojangLogoClient.LOGGER.warn("Context provider returned null during countdown tick, cancelling countdown {}", id);
                cancel();
                return;
            }

            try {
                typedManager().mainThreadDispatcher.accept(context, this::runTickOnMainThread);
            } catch (Throwable t) {
                AnimatedMojangLogoClient.LOGGER.error("Error dispatching countdown tick for {}", id, t);
                try { runTickOnMainThread(); } catch (Throwable inner) {
                    AnimatedMojangLogoClient.LOGGER.error("Error running tick directly for {}", id, inner);
                }
            }
        }, delayNanos, TimeUnit.NANOSECONDS);
    }

    private void runTickOnMainThread() {
        if (cancelled.get() || finished.get() || paused.get()) return;

        long now = System.nanoTime();
        long remainingNanos = Math.max(0L, endTimeNanos - now);
        Duration remainingDuration = Duration.ofNanos(remainingNanos);

        Duration elapsedDuration = Duration.ofNanos(Math.max(0L, totalNanos - remainingNanos));
        long elapsedNanos = elapsedDuration.toNanos();

        Object context = typedManager().contextProvider.get();
        if (context == null) {
            AnimatedMojangLogoClient.LOGGER.warn("Context provider returned null during countdown tick, cancelling countdown {}", id);
            cancel();
            return;
        }

        tickHandlers.forEach(handler -> {
            try { handler.accept(context, remainingDuration); }
            catch (Throwable t) { AnimatedMojangLogoClient.LOGGER.error("Error in tick handler for {}", id, t); }
        });

        intervalHandlers.forEach((interval, handlers) -> {
            long nextFire = nextElapsedToFire.getOrDefault(interval, interval);
            while (elapsedNanos >= nextFire) {
                for (Consumer<Object> handler : handlers) {
                    try { handler.accept(context); }
                    catch (Throwable t) { AnimatedMojangLogoClient.LOGGER.error("Error in interval handler for {} at interval {}", id, interval, t); }
                }
                nextFire += interval;
                nextElapsedToFire.put(interval, nextFire);
            }
        });

        if (!thresholds.isEmpty()) {
            var toFire = thresholds.tailMap(remainingNanos, true);
            if (!toFire.isEmpty()) {
                new ArrayList<>(toFire.keySet()).forEach(key -> {
                    List<Consumer<Object>> handlers = thresholds.remove(key);
                    if (handlers != null) {
                        handlers.forEach(handler -> {
                            try { handler.accept(context); }
                            catch (Throwable t) { AnimatedMojangLogoClient.LOGGER.error("Error in threshold handler for {} at {}", id, key, t); }
                        });
                    }
                });
            }
        }

        if (remainingNanos == 0L && finished.compareAndSet(false, true)) {
            try {
                finishHandlers.forEach(handler -> {
                    try { handler.accept(context); }
                    catch (Throwable t) { AnimatedMojangLogoClient.LOGGER.error("Error in finish handler for {}", id, t); }
                });
            } finally {
                typedManager().activeCountdowns.remove(id);
            }
            return;
        }

        scheduleNextTick();
    }

    /**
     * Cancels the countdown.
     * @return true if the countdown was cancelled, false if it was already finished or canceled
     */
    public boolean cancel() {
        if (cancelled.getAndSet(true)) return false;
        if (scheduledTask != null) scheduledTask.cancel(false);
        typedManager().activeCountdowns.remove(id);
        return true;
    }

    void cancelSilently() {
        cancelled.set(true);
        if (scheduledTask != null) scheduledTask.cancel(false);
    }

    /**
     * Pauses the countdown.
     * Note: If the countdown is already paused, this method does nothing.
     * @return true if the countdown was paused, false if it was already finished or canceled
     */
    public boolean pause() {
        if (cancelled.get() || finished.get()) return false;
        if (!paused.compareAndSet(false, true)) return false;
        if (scheduledTask != null && !scheduledTask.isDone()) scheduledTask.cancel(false);
        remainingOnPause = Math.max(0L, endTimeNanos - System.nanoTime());
        return true;
    }

    /**
     * Resumes a paused countdown.
     * Note: If the countdown is not paused, this method does nothing.
     * @return true if the countdown was resumed, false if it was already finished or canceled
     */
    public boolean resume() {
        if (cancelled.get() || finished.get()) return false;
        if (!paused.compareAndSet(true, false)) return false;
        if (remainingOnPause < 0) remainingOnPause = 0;
        endTimeNanos = System.nanoTime() + remainingOnPause;
        remainingOnPause = -1;
        scheduleNextTick();
        return true;
    }

    /**
     * Toggles to pause or unpause the countdown.
     * @return true if the countdown was changed state, false if it was already finished or canceled
     */
    public boolean pauseOrUnpause() {
        return !isPaused() ? pause() : resume();
    }

    public boolean isCancelled() { return cancelled.get(); }
    public boolean isPaused() { return paused.get(); }
    public boolean isFinished() { return finished.get(); }

    public Duration remaining() {
        if (paused.get() && remainingOnPause >= 0) return Duration.ofNanos(remainingOnPause);
        return Duration.ofNanos(Math.max(0L, endTimeNanos - System.nanoTime()));
    }

    public String getId() { return id; }

    public Countdown onTick(BiConsumer<Object, Duration> handler) {
        tickHandlers.add(handler);
        return this;
    }

    /**
     * Adds a task to execute when the countdown finishes.
     * @param handler Handler to execute when the countdown finishes.
     */
    public Countdown onFinish(Consumer<Object> handler) {
        finishHandlers.add(handler);
        return this;
    }

    /**
     * Adds a task to execute when the countdown reaches the specified threshold.
     * @param threshold Duration threshold to reach.
     * @param handler Handler to execute when the threshold is reached.
     */
    public Countdown onThreshold(Duration threshold, Consumer<Object> handler) {
        thresholds.computeIfAbsent(threshold.toNanos(), k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
        return this;
    }

    /**
     * Adds a task to execute every time the specified interval elapses. (Like {@link #onTick(BiConsumer)},
     * but with a specified interval instead of the countdown tick interval)
     * @param interval Interval at which to execute the handler.
     * @param handler Handler to execute every time the interval elapses.
     */
    public Countdown every(Duration interval, Consumer<Object> handler) {
        long nanos = Math.max(1L, interval.toNanos());
        intervalHandlers.computeIfAbsent(nanos, k -> Collections.synchronizedList(new ArrayList<>())).add(handler);
        nextElapsedToFire.putIfAbsent(nanos, nanos);
        return this;
    }
}
