package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

public interface Worker<T> {

    T pullNext();

    boolean isWorkerReady();
}
