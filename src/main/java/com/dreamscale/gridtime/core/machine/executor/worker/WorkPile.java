package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

public interface WorkPile {

    TickInstructions whatsNext();

    boolean hasWork();

    void evictLastWorker();

    int size();
}
