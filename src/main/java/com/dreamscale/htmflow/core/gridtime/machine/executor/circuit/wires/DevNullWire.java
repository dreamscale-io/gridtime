package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class DevNullWire implements Wire {


    @Override
    public void pushAll(List<TileStreamEvent> tileStreamEvents) {

    }

    @Override
    public void push(TileStreamEvent event) {

    }


    @Override
    public AggregateStreamEvent pullNext(UUID workerId) {
        return null;
    }

    @Override
    public void markDone(UUID workerId) {

    }

    @Override
    public int getQueueDepth() {
        return 0;
    }
}
