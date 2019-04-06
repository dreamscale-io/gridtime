package com.dreamscale.htmflow.core.feeds.executor.parts.transform;


import com.dreamscale.htmflow.core.feeds.common.Flow;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.common.SharedFeaturePool;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.Batch;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.feeds.executor.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;

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

    }


}
