package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform;


import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FlowTransformer implements Flow {

    private final UUID memberId;
    private final List<TransformStrategy> transformStrategies;
    private final FeaturePool featurePool;


    public FlowTransformer(UUID memberId, FeaturePool featurePool, TransformStrategy... transforms) {
        this.memberId = memberId;
        this.featurePool = featurePool;
        this.transformStrategies = Arrays.asList(transforms);
    }

    public void tick(Metronome.Tick coordinates) {

        for (TransformStrategy transform : transformStrategies) {
            transform.transform(featurePool);
        }
    }


}
