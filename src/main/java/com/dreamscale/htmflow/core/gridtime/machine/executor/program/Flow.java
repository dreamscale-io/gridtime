package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;

public interface Flow {

    void tick(Metronome.Tick coordinates) throws InterruptedException;

}
