package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input;

import com.dreamscale.gridtime.core.machine.clock.Metronome;

import java.util.List;
import java.util.UUID;

public interface AggregateInputStrategy<T> {

    List<T> breatheIn(UUID teamId, UUID torchieId, Metronome.TickScope tickScope);
}
