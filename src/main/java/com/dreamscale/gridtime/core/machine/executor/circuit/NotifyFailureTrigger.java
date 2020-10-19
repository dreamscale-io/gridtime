package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

public interface NotifyFailureTrigger {

    void notifyOnAbortOrFailure(TickInstructions instructions, Exception ex);
}
