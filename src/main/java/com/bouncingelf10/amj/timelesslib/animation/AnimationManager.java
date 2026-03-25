package com.bouncingelf10.amj.timelesslib.animation;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationManager {
    private final Map<String, AnimationTimeline> timelines = new ConcurrentHashMap<>();

    public AnimationTimeline createTimeline(String id) {
        Objects.requireNonNull(id);
        AnimationTimeline timeline = new AnimationTimeline(id);
        timelines.put(id, timeline);
        return timeline;
    }

    /**
     * Gets a timeline by its ID. <br>
     * Note: Returns an empty Optional if the timeline does not exist.
     * @param id Timeline ID
     * @return {@link AnimationTimeline} or empty
     */
    public Optional<AnimationTimeline> getTimeline(String id) {
        return Optional.ofNullable(timelines.get(id));
    }

    public boolean removeTimeline(String id) { return timelines.remove(id) != null; }

    public void update() {
        for (AnimationTimeline timeline : timelines.values()) {
            double delta = timeline.getDeltaSeconds();
            timeline.update(delta);
        }
    }
}