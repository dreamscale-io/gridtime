package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import java.util.List;
import java.util.UUID;

public interface Wire {

    void pushAll(List<TileStreamEvent> tileStreamEvents);

    void push(TileStreamEvent event);

    AggregateStreamEvent pullNext(UUID workerId);

    void markDone(UUID workerId);

    int getQueueDepth();
}
