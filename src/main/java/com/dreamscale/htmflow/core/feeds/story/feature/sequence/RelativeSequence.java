package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RelativeSequence implements FlowFeature {
    private int relativeSequence;

    public int increment() {
        relativeSequence++;

        return relativeSequence;
    }

    public void reset(int startingSequence) {
        relativeSequence = startingSequence;
    }
}
