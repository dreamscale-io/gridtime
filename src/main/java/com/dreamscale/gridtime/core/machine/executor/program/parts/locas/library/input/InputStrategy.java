package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input;

import com.dreamscale.gridtime.core.machine.clock.Metronome;

import java.util.List;
import java.util.UUID;

public interface InputStrategy<T> {

    List<T> breatheIn(UUID torchieId, Metronome.Tick tick);
}
