package com.dreamscale.htmflow.core.feeds.executor.parts.transform;


import com.dreamscale.htmflow.core.feeds.common.Flow;
import com.dreamscale.htmflow.core.feeds.pool.SharedFeaturePool;

import java.time.LocalDateTime;
import java.util.*;

public class FlowTransformer implements Flow {

    private final UUID memberId;
    private final List<TransformStrategy> transformStrategies;
    private final SharedFeaturePool sharedFeaturePool;


    public FlowTransformer(UUID memberId, SharedFeaturePool sharedFeaturePool, TransformStrategy... transforms) {
        this.memberId = memberId;
        this.sharedFeaturePool = sharedFeaturePool;
        this.transformStrategies = Arrays.asList(transforms);
    }

    public void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) {

        for (TransformStrategy transform : transformStrategies) {
            transform.transform(sharedFeaturePool.getActiveStoryTile());
        }
    }


}
