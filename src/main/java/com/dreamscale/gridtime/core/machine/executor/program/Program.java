package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.List;

public interface Program {

    void tick();

    Metronome.Tick getActiveTick();

    List<TileInstructions> getInstructionsAtActiveTick();

    int getInputQueueDepth();

    boolean isDone();

}
