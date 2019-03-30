package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.IdeaFlowFeature;

public class ThoughtBridge implements IdeaFlowFeature {

    private final String bridgeKey;
    private final LocationInThought fromLocation;
    private final LocationInThought toLocation;

    private int visitCount;

    public ThoughtBridge(String bridgeKey, LocationInThought fromLocation, LocationInThought toLocation) {
        this.bridgeKey = bridgeKey;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.visitCount = 0;
    }

    public void visit() {
        visitCount++;
    }
}
