package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;

import java.util.List;

public class NoOpProgram implements Program {


    @Override
    public void tick() {

    }

    @Override
    public Metronome.Tick getActiveTick() {
        return null;
    }

    @Override
    public List<TileInstructions> getInstructionsAtTick(Metronome.Tick tick) {
        return DefaultCollections.emptyList();
    }

    @Override
    public List<TileInstructions> getInstructionsAtActiveTick() {
        return DefaultCollections.emptyList();
    }

    @Override
    public boolean isDone() {
        return false;
    }
}
