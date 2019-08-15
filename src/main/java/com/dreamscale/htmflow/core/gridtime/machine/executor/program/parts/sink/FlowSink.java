package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlowSink implements Flow {


    private final UUID memberId;
    private final TorchieState torchieState;
    private final List<SinkStrategy> sinkStrategies;

    public FlowSink(UUID memberId, TorchieState torchieState, SinkStrategy... sinkStrategies) {
        this.memberId = memberId;
        this.torchieState = torchieState;

        this.sinkStrategies = new ArrayList<>();

        for (SinkStrategy sink : sinkStrategies) {
            addFlowSink(sink);
        }
    }

    @Override
    public void tick(Metronome.Tick coordinates) throws InterruptedException {

        for (SinkStrategy sink : sinkStrategies) {
            sink.save(memberId, torchieState);
        }
    }

    private void addFlowSink(SinkStrategy sink) {
        this.sinkStrategies.add(sink);
    }
}
