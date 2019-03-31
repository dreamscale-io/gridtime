package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

import java.util.HashSet;
import java.util.Set;

public class BoxToBubbleLink implements IdeaFlowFeature {

    private final ThoughtBubble thoughtBubble;

    private final Set<BridgeStructure> bridges = new HashSet<>();
    private final Set<LocationInFocus> connectedLocations = new HashSet<>();


    public BoxToBubbleLink(ThoughtBubble bubble) {
        this.thoughtBubble = bubble;
    }

    public boolean connectedTo(ThoughtBubble bubble) {
        return thoughtBubble == bubble;
    }

    public void addBridgeToSet(BridgeStructure bridgeStructure) {
        this.bridges.add(bridgeStructure);
    }

    public void addConnectedLocationToSet(LocationInFocus location) {
        connectedLocations.add(location);
    }

    public int getVisitCount() {
        int totalVisits = 0;
        for (BridgeStructure bridge : bridges) {
            totalVisits += bridge.getVisitCount();
        }
        return totalVisits;
    }

}
