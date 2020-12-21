package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

public interface Worker {

    TickInstructions whatsNext();

    CircuitMonitor getCircuitMonitor();

    boolean isWorkerReady();

    void abortAndClearProgram();
}
