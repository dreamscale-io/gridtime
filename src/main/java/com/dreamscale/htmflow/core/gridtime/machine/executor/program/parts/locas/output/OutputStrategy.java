package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.output;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateGrid;

import java.util.UUID;

public interface OutputStrategy {

    void breatheOut(UUID torchieId, Metronome.Tick tick, AggregateGrid aggregateGrid);
}
