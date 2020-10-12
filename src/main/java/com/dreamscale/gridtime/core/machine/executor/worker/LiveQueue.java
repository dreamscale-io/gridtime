package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

import java.util.UUID;

public interface LiveQueue {

    void submit(UUID workerId, Worker worker);

}
