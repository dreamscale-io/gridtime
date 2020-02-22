package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;

import java.util.List;

public interface ParallelProgram {

    List<TickInstructions> getInstructionsAtTick(Metronome.TickScope tickScope);

    boolean isDone();

    Wire getOutputStreamEventWire();
}
