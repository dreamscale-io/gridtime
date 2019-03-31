package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

import java.util.List;

public class BoxStructure implements IdeaFlowFeature {

    private final String placeName;
    private final List<RadialStructure> thoughtBubbles;

    public BoxStructure(String placeName, List<RadialStructure> thoughtBubbles) {
        this.placeName = placeName;
        this.thoughtBubbles = thoughtBubbles;
    }


}
