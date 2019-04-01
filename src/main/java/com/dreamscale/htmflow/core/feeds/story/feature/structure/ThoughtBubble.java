package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

public class ThoughtBubble implements FlowFeature {


    private final RadialStructure radialStructure;
    private int relativeSequence;

    public ThoughtBubble(RadialStructure radialStructure) {
        this.radialStructure = radialStructure;
    }

    public boolean contains(LocationInPlace locationInBubble) {
        return radialStructure.contains(locationInBubble);
    }

    public void setRelativeSequence(int sequence) {
        this.relativeSequence = sequence;
    }
}
