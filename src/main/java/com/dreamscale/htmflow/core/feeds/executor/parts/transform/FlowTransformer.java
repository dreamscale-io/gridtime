package com.dreamscale.htmflow.core.feeds.executor.parts.transform;


import com.dreamscale.htmflow.core.feeds.common.Flow;
import com.dreamscale.htmflow.core.feeds.common.SharedFeaturePool;

import java.time.LocalDateTime;
import java.util.*;

public class FlowTransformer implements Flow {

    private final UUID memberId;
    private final List<FlowTransform> flowTransforms;
    private final SharedFeaturePool sharedFeaturePool;


    public FlowTransformer(UUID memberId, SharedFeaturePool sharedFeaturePool, FlowTransform... transforms) {
        this.memberId = memberId;
        this.sharedFeaturePool = sharedFeaturePool;
        this.flowTransforms = Arrays.asList(transforms);
    }

    public void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) {

        for (FlowTransform transform : flowTransforms) {
            transform.transform(sharedFeaturePool.getActiveStoryTile());
        }
    }


}
