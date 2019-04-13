package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.StandardizedKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObject;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObjectMetrics;

import java.time.Duration;

public class Traversal extends FlowFeature implements GridObject {

    private final LocationInBox locationA;
    private final LocationInBox locationB;

    private GridObjectMetrics gridObjectMetrics = new GridObjectMetrics();

    public Traversal(LocationInBox locationA, LocationInBox locationB) {
        this.locationA = locationA;
        this.locationB = locationB;
    }

    public LocationInBox getLocationA() {
        return locationA;
    }

    public LocationInBox getLocationB() {
        return locationB;
    }

    public String toKey() {
        return StandardizedKeyMapper.createLocationTraversalKey(locationA.toKey(), locationB.toKey());
    }

    @Override
    public GridObjectMetrics getGridObjectMetrics() {
        return gridObjectMetrics;
    }

    public void spendTime(Duration timeInLocation) {
        gridObjectMetrics.addVelocitySample(timeInLocation.getSeconds());
    }
}
