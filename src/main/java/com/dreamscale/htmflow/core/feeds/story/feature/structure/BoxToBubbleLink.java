package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class BoxToBubbleLink extends FlowFeature {

    private final ThoughtBubble thoughtBubble;

    private final Set<Bridge> bridges = new HashSet<>();
    private final Set<LocationInBox> connectedLocations = new HashSet<>();


    public BoxToBubbleLink(ThoughtBubble bubble) {
        this.thoughtBubble = bubble;
    }

    public boolean connectedTo(ThoughtBubble bubble) {
        return thoughtBubble == bubble;
    }

    public void addBridgeToSet(Bridge bridge) {
        this.bridges.add(bridge);
    }

    public void addConnectedLocationToSet(LocationInBox location) {
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
