package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

public class Bridge implements FlowFeature {

    private final String bridgeKey;

    private Box fromBox;
    private Box toBox;
    private final LocationInPlace fromLocation;
    private final LocationInPlace toLocation;

    private int visitCount;
    private int relativeSequence;

    public Bridge(String bridgeKey, LocationInPlace fromLocation, LocationInPlace toLocation) {
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

    public LocationInPlace getFromLocation() {
        return fromLocation;
    }

    public LocationInPlace getToLocation() {
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
