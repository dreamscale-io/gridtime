package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

public class Bridge implements IdeaFlowFeature {

    private final String bridgeKey;

    private Box fromBox;
    private Box toBox;
    private final LocationInFocus fromLocation;
    private final LocationInFocus toLocation;

    private int visitCount;
    private int relativeSequence;

    public Bridge(String bridgeKey, LocationInFocus fromLocation, LocationInFocus toLocation) {
        this.bridgeKey = bridgeKey;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.visitCount = 0;
    }

    public void setFromBox(Box boxWithFromLocation) {
        this.fromBox = boxWithFromLocation;
    }

    public void setToBox(Box boxWithToLocation) {
        this.toBox = boxWithToLocation;
    }

    public LocationInFocus getFromLocation() {
        return fromLocation;
    }

    public LocationInFocus getToLocation() {
        return toLocation;
    }

    public void visit() {
        visitCount++;
    }


    public int getVisitCount() {
        return visitCount;
    }

    public void setRelativeSequence(int relativeBridgeSequence) {
        this.relativeSequence = relativeBridgeSequence;
    }

    public int getRelativeSequence() {
        return this.relativeSequence;
    }
}
