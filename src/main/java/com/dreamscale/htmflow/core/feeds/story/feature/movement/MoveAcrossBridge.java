package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Bridge;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class MoveAcrossBridge extends Movement {

    private Bridge bridge;

    public MoveAcrossBridge(LocalDateTime moment, Bridge bridge) {
        super(moment, FlowObjectType.MOVEMENT_ACROSS_BRIDGE);
        this.bridge = bridge;
    }

    public MoveAcrossBridge() {
        super(FlowObjectType.MOVEMENT_ACROSS_BRIDGE);
    }

}
