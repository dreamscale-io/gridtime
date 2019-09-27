package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;

import java.util.UUID;

public interface OutputStrategy {

    void breatheOut(UUID torchieId, Metronome.TickScope tickScope, IMusicGrid outputGrid);
}
