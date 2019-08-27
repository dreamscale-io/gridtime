package com.dreamscale.gridtime.core.machine.executor.workpile;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.UUID;

public interface WorkerPool {

    TileInstructions whatsNext();

    void addWorker(Worker worker);

    boolean containsWorker(UUID workerId);

    Worker getWorker(UUID workerId);

    boolean hasWork();

    void evictLastWorker();

}
