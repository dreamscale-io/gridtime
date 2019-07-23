package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;

public interface Locas {

    void breatheIn(Metronome.Tick tick);

    void breatheOut(Metronome.Tick tick);
}


