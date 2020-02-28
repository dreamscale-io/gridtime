package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;

import java.util.List;

public interface Program {

    String getName();

    void tick();

    Metronome.TickScope getActiveTick();

    List<TickInstructions> getInstructionsAtActiveTick();

    int getInputQueueDepth();

    boolean isDone();

}
