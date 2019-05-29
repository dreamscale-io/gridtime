package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.Flow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.PerProcessFeaturePool;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlowSink implements Flow {


    private final UUID memberId;
    private final FeaturePool featurePool;
    private final List<SinkStrategy> sinkStrategies;

    public FlowSink(UUID memberId, FeaturePool featurePool, SinkStrategy... sinkStrategies) {
        this.memberId = memberId;
        this.featurePool = featurePool;

        this.sinkStrategies = new ArrayList<>();

        for (SinkStrategy sink : sinkStrategies) {
            addFlowSink(sink);
        }
    }

    @Override
    public void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException {

        for (SinkStrategy sink : sinkStrategies) {
            sink.save(memberId, featurePool.getActiveGridTile());
        }
    }

    private void addFlowSink(SinkStrategy sink) {
        this.sinkStrategies.add(sink);
    }
}