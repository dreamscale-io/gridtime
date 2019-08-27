package com.dreamscale.gridtime.core.machine.executor.program.parts.transform;

import com.dreamscale.gridtime.core.machine.memory.TorchieState;

public interface TransformStrategy {

    void transform(TorchieState torchieState);
}
