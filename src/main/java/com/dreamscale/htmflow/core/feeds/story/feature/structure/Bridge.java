package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

@Getter
public class Bridge extends FlowFeature {

    private final String bridgeKey;

    private Box fromBox;
    private Box toBox;
    private final LocationInBox fromLocation;
    private final LocationInBox toLocation;

    private int visitCount;
    private int relativeSequence;

    public Bridge(String bridgeKey, LocationInBox fromLocation, LocationInBox toLocation) {
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

    public LocationInBox getFromLocation() {
        return fromLocation;
    }

    public LocationInBox getToLocation() {
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
