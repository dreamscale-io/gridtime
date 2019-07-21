package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;

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
    public void tick(Metronome.Tick coordinates) throws InterruptedException {

        for (SinkStrategy sink : sinkStrategies) {
            sink.save(memberId, featurePool);
        }
    }

    private void addFlowSink(SinkStrategy sink) {
        this.sinkStrategies.add(sink);
    }
}
