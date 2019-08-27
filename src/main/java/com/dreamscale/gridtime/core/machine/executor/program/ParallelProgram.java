package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;

import java.util.List;

public interface ParallelProgram {

    List<TileInstructions> getInstructionsAtTick(Metronome.Tick tick);

    boolean isDone();

    Wire getOutputStreamEventWire();
}
