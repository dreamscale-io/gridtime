package com.dreamscale.htmflow.core.gridtime.machine.executor.workpile;

import com.dreamscale.htmflow.core.gridtime.machine.Torchie;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;

import java.util.UUID;

public interface WorkerPool {

    TileInstructions whatsNext();

    void addWorker(Worker worker);

    boolean containsWorker(UUID workerId);

    Worker getWorker(UUID workerId);

    boolean hasWork();

    void evictLastWorker();

}
