package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.ObjectKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

public class Traversal extends FlowFeature {

    private final LocationInBox locationA;
    private final LocationInBox locationB;

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
        return ObjectKeyMapper.createLocationTraversalKey(locationA.toKey(), locationB.toKey());
    }
}
