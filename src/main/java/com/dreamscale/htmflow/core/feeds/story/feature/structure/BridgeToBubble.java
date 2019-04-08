package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class BridgeToBubble extends FlowFeature {

    private final Bridge bridge;
    private final LocationInBox connectedLocation;
    private int relativeSequence;


    public BridgeToBubble(Bridge bridge, LocationInBox connectedLocation) {
        this.bridge = bridge;
        this.connectedLocation = connectedLocation;
    }

    public int getVisitCount() {
        return bridge.getVisitCount();
    }

    public void setRelativeSequence(int relativeSequence) {
        this.relativeSequence = relativeSequence;
    }
}
