package com.dreamscale.ideaflow.core.feeds.story.feature.sequence;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RelativeSequence implements IdeaFlowFeature {
    private int relativeSequence;

    public int increment() {
        relativeSequence++;

        return relativeSequence;
    }

    public void reset(int startingSequence) {
        relativeSequence = startingSequence;
    }
}
