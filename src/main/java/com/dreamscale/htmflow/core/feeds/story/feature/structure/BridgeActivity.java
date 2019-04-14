package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.List;

@Getter
public class BridgeActivity extends FlowFeature {

    private Bridge bridge;
    private int relativeSequence;

    public BridgeActivity(Bridge bridge) {
        this.bridge = bridge;
    }

    public void setRelativeSequence(int relativeSequence) {
        this.relativeSequence = relativeSequence;
    }
}
