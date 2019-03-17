package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

public class Bridge implements FlowFeature {

    private final String bridgeKey;
    private final LocationInPlace fromLocation;
    private final LocationInPlace toLocation;

    private int visitCount;

    public Bridge(String bridgeKey, LocationInPlace fromLocation, LocationInPlace toLocation) {
        this.bridgeKey = bridgeKey;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.visitCount = 0;
    }

    public void visit() {
        visitCount++;
    }
}
