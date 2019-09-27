package com.dreamscale.gridtime.core.machine.executor.program.parts.transform;


import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.Flow;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FlowTransformer implements Flow {

    private final UUID memberId;
    private final List<TransformStrategy> transformStrategies;
    private final TorchieState torchieState;


    public FlowTransformer(UUID memberId, TorchieState torchieState, TransformStrategy... transforms) {
        this.memberId = memberId;
        this.torchieState = torchieState;
        this.transformStrategies = Arrays.asList(transforms);
    }

    public void tick(Metronome.TickScope coordinates) {

        for (TransformStrategy transform : transformStrategies) {
            transform.transform(torchieState);
        }
    }


}
