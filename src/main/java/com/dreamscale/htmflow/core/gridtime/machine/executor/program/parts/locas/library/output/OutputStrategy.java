package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.output;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.IMusicGrid;

import java.util.UUID;

public interface OutputStrategy {

    void breatheOut(UUID torchieId, Metronome.Tick tick, IMusicGrid outputGrid);
}
