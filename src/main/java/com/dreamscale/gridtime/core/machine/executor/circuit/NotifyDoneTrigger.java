package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

import java.util.List;

public interface NotifyDoneTrigger {

    void notifyWhenDone(TickInstructions instructions, List<Results> results);
}
