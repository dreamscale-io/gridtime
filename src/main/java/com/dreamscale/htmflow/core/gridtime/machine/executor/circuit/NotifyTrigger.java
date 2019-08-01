package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;

import java.util.List;

public interface NotifyTrigger {

    void notifyWhenDone(TileInstructions instructions, List<Results> results);
}
