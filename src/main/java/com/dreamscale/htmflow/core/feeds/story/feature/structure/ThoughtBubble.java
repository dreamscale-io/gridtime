package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.util.ArrayList;
import java.util.List;

public class ThoughtBubble extends FlowFeature {

    private final List<BridgeToBubble> bridgeToBubbles = new ArrayList<>();
    private final RadialStructure radialStructure;
    private int relativeSequence;
    private String uri;

    private int bridgeToBubbleSequence = 1;

    public ThoughtBubble(RadialStructure radialStructure) {
        this.radialStructure = radialStructure;
    }

    public boolean contains(LocationInBox locationInBubble) {
        return radialStructure.contains(locationInBubble);
    }

    public void setRelativeSequence(int sequence) {
        this.relativeSequence = sequence;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getRelativeSequence() {
        return relativeSequence;
    }

    public RadialStructure.RingLocation getCenter() {
        return radialStructure.getCenter();
    }

    public RadialStructure.RingLocation getEntrance() {
        return radialStructure.getEntrance();
    }

    public RadialStructure.RingLocation getExit() {
        return radialStructure.getExit();
    }

    public List<RadialStructure.Link> getLinksFromEntrance() {
        return radialStructure.getLinksFromEntrance();
    }

    public List<RadialStructure.Link> getLinksToExit() {
        return radialStructure.getLinksToExit();
    }

    public List<RadialStructure.Ring> getRings() {
        return radialStructure.getRings();
    }


    public void addBoxToBubbleLink(BridgeToBubble bridgeToBubble) {
        bridgeToBubble.setRelativeSequence(bridgeToBubbleSequence);
        this.bridgeToBubbles.add(bridgeToBubble);
        bridgeToBubbleSequence++;
    }

    public List<BridgeToBubble> getBridgeToBubbles() {
        return bridgeToBubbles;
    }
}
