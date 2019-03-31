package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

public class BoxStructure implements IdeaFlowFeature {

    private final String bridgeKey;
    private final LocationInFocus fromLocation;
    private final LocationInFocus toLocation;

    private int visitCount;

    public BoxStructure(String bridgeKey, LocationInFocus fromLocation, LocationInFocus toLocation) {
        this.bridgeKey = bridgeKey;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.visitCount = 0;
    }

    public void visit() {
        visitCount++;
    }
}
