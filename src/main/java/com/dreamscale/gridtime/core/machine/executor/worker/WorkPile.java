package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

public interface WorkPile {

    void reset();

    void pause();

    void resume();

    TickInstructions whatsNext();

    boolean hasWork();

    int size();
}
