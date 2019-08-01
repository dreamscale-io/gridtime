package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink;

import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;

import java.util.UUID;

public interface SinkStrategy {

    void save(UUID torchieId, TorchieState torchieState);

}
