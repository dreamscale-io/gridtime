package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RelativeSequence {
    private int relativeSequence;

    public int increment() {
        relativeSequence++;

        return relativeSequence;
    }

    public void reset(int startingSequence) {
        relativeSequence = startingSequence;
    }
}
