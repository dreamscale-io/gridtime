package com.dreamscale.gridtime.core.machine.executor.program.parts.sink;

import com.dreamscale.gridtime.core.machine.memory.TorchieState;

import java.util.UUID;

public interface SinkStrategy {

    void save(UUID torchieId, TorchieState torchieState);

}
