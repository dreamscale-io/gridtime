package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import java.util.List;
import java.util.UUID;

public interface Wire {

    void publishAll(List<TileStreamEvent> tileStreamEvents);

    void publish(TileStreamEvent event);

    boolean hasNext();

    AggregateStreamEvent pullNext(UUID workerId);

    void markDone(UUID workerId);

    int getQueueDepth();
}
