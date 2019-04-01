package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.util.HashSet;
import java.util.Set;

public class BoxToBubbleLink implements FlowFeature {

    private final ThoughtBubble thoughtBubble;

    private final Set<Bridge> bridges = new HashSet<>();
    private final Set<LocationInPlace> connectedLocations = new HashSet<>();


    public BoxToBubbleLink(ThoughtBubble bubble) {
        this.thoughtBubble = bubble;
    }

    public boolean connectedTo(ThoughtBubble bubble) {
        return thoughtBubble == bubble;
    }

    public void addBridgeToSet(Bridge bridge) {
        this.bridges.add(bridge);
    }

    public void addConnectedLocationToSet(LocationInPlace location) {
        connectedLocations.add(location);
    }

    public int getVisitCount() {
        int totalVisits = 0;
        for (Bridge bridge : bridges) {
            totalVisits += bridge.getVisitCount();
        }
        return totalVisits;
    }

}
