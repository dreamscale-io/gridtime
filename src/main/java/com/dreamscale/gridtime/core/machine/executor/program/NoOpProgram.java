package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.List;

public class NoOpProgram implements Program {


    @Override
    public void tick() {

    }

    @Override
    public Metronome.TickScope getActiveTick() {
        return null;
    }

    @Override
    public List<TileInstructions> getInstructionsAtActiveTick() {
        return DefaultCollections.emptyList();
    }

    @Override
    public int getInputQueueDepth() {
        return 0;
    }

    @Override
    public boolean isDone() {
        return false;
    }

}
