package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;

import java.util.List;

public interface Program {

    void tick(Wire inputStreamEventWire);

    Metronome.Tick getActiveTick();

    List<TileInstructions> getInstructionsAtActiveTick();

    boolean isDone();

}
