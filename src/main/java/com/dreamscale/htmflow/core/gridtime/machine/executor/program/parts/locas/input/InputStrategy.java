package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.input;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;

import java.util.List;
import java.util.UUID;

public interface InputStrategy<T> {

    List<T> breatheIn(UUID torchieId, Metronome.Tick tick);
}
