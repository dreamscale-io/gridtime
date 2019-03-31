package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

import java.util.List;

public class ThoughtBubble implements IdeaFlowFeature {


    private final RadialStructure radialStructure;
    private int relativeSequence;

    public ThoughtBubble(RadialStructure radialStructure) {
        this.radialStructure = radialStructure;
    }

    public boolean contains(LocationInFocus locationInBubble) {
        return radialStructure.contains(locationInBubble);
    }

    public void setRelativeSequence(int sequence) {
        this.relativeSequence = sequence;
    }
}
