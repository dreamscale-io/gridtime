package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform;

import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;

public interface TransformStrategy {

    void transform(TorchieState torchieState);
}
