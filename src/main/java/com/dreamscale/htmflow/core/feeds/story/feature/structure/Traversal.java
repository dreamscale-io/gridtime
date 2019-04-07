package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.StandardizedKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

public class Traversal extends FlowFeature {

    private final LocationInBox locationA;
    private final LocationInBox locationB;
    private int visitCounter;

    public Traversal(LocationInBox locationA, LocationInBox locationB) {
        this.locationA = locationA;
        this.locationB = locationB;
        this.visitCounter = 0;
    }

    public LocationInBox getLocationA() {
        return locationA;
    }

    public LocationInBox getLocationB() {
        return locationB;
    }

    public void visit() {
        this.visitCounter++;
    }

    public String toKey() {
        return StandardizedKeyMapper.createLocationTraversalKey(locationA.toKey(), locationB.toKey());
    }

    public int getTraversalCount() {
        return visitCounter;
    }
}
