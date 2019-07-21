package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;

import java.util.List;

public interface Program {

    void tick();

    Metronome.Tick getActiveTick();

    List<TileInstructions> getInstructionsAtTick(Metronome.Tick tick);

    List<TileInstructions> getInstructionsAtActiveTick();

    boolean isDone();
}
