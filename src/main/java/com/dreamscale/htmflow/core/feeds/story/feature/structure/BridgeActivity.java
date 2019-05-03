package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class BridgeActivity extends FlowFeature {

    private Bridge bridge;
    private int relativeSequence;

    public BridgeActivity(Bridge bridge) {
        this();
        this.bridge = bridge;
    }

    public BridgeActivity() {
        super(FlowObjectType.BRIDGE_ACTIVITY);
    }

    public void setRelativeSequence(int relativeSequence) {
        this.relativeSequence = relativeSequence;
    }
}
