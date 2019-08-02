package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.IMusicGrid;

public interface Locas {

    IMusicGrid runProgram(Metronome.Tick tick);

     MusicGridResults playAllTracks();

}


