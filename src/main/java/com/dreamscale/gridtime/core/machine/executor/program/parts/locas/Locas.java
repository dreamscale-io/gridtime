package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;

public interface Locas {

    IMusicGrid runProgram(Metronome.TickScope tickScope);

     MusicGridResults playAllTracks();

}


