package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform;


import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.Flow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;

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

    public void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) {

        for (TransformStrategy transform : transformStrategies) {
            transform.transform(featurePool.getActiveGridTile());
        }
    }


}
