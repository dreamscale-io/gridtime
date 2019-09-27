package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;

public interface Flow {

    void tick(Metronome.TickScope coordinates) throws InterruptedException;

}
