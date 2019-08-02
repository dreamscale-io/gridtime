package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.AggregatingWire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.DevNullWire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;

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
    public List<TileInstructions> getInstructionsAtActiveTick() {
        return DefaultCollections.emptyList();
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Wire getOutputStreamEventWire() {
        return new DevNullWire();
    }
}
