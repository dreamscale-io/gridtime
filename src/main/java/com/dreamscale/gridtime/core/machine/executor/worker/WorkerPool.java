package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.UUID;

public interface WorkerPool {

    TileInstructions whatsNext();

    boolean hasWork();

    void evictLastWorker();

    int size();
}
