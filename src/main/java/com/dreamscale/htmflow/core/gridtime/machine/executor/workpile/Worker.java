package com.dreamscale.htmflow.core.gridtime.machine.executor.workpile;

import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;

public interface Worker {

    TileInstructions whatsNext();

    boolean isWorkerReady();
}
