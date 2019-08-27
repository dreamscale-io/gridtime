package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.List;

public interface NotifyTrigger {

    void notifyWhenDone(TileInstructions instructions, List<Results> results);
}
