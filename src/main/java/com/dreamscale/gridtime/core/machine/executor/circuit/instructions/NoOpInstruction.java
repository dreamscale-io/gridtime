package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpInstruction extends TickInstructions {

    @Override
    protected void executeInstruction() {

       log.debug("Executing no-op instruction");

    }

    @Override
    public String getCmdDescription() {
        return "no-op instruction";
    }
}
