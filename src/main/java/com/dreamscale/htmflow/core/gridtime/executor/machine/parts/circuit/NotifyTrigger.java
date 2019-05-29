package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.circuit;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.TileInstructions;

public interface NotifyTrigger {

    void notifyWhenDone(TileInstructions instructions, Results results);
}
