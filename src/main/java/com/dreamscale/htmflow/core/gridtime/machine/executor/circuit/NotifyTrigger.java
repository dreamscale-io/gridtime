package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;

public interface NotifyTrigger {

    void notifyWhenDone(TileInstructions instructions, Results results);
}
