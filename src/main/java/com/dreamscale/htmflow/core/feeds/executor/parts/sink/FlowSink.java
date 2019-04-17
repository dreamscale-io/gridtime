package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.feeds.common.Flow;
import com.dreamscale.htmflow.core.feeds.common.SharedFeaturePool;
import com.dreamscale.htmflow.core.feeds.executor.parts.transform.FlowTransform;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlowSink implements Flow {


    private final UUID memberId;
    private final SharedFeaturePool sharedFeaturePool;
    private final List<SinkStrategy> sinkStrategies;

    public FlowSink(UUID memberId, SharedFeaturePool sharedFeaturePool, SinkStrategy... sinkStrategies) {
        this.memberId = memberId;
        this.sharedFeaturePool = sharedFeaturePool;

        this.sinkStrategies = new ArrayList<>();

        for (SinkStrategy sink : sinkStrategies) {
            addFlowSink(sink);
        }
    }

    @Override
    public void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException {

        for (SinkStrategy sink : sinkStrategies) {
            sink.save(memberId, sharedFeaturePool.getActiveStoryTile());
        }
    }

    private void addFlowSink(SinkStrategy sink) {
        this.sinkStrategies.add(sink);
    }
}
