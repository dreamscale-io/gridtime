package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires;

import java.util.List;

public interface Wire {

    void publishAll(List<TileStreamEvent> tileStreamEvents);

    void publish(TileStreamEvent event);
}
