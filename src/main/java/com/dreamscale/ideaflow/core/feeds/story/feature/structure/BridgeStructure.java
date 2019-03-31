package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

public class BridgeStructure implements IdeaFlowFeature {

    private final String bridgeKey;
    private final LocationInFocus fromLocation;
    private final LocationInFocus toLocation;

    private int visitCount;

    public BridgeStructure(String bridgeKey, LocationInFocus fromLocation, LocationInFocus toLocation) {
        this.bridgeKey = bridgeKey;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.visitCount = 0;
    }

    public void visit() {
        visitCount++;
    }
}
