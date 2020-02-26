package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;

public interface Worker<T> {

    T whatsNext();

    CircuitMonitor getCircuitMonitor();

    boolean isWorkerReady();
}
