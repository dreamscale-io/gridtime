package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;

public interface Flow {

    void tick(Metronome.Tick coordinates) throws InterruptedException;

}
