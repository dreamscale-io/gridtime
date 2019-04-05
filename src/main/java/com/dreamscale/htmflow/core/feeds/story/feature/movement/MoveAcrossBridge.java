package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.Bridge;

import java.time.LocalDateTime;

public class MoveAcrossBridge extends Movement {

    private final Bridge bridge;

    public MoveAcrossBridge(LocalDateTime moment, Bridge bridge) {
        super(moment);
        this.bridge = bridge;
    }

    public Bridge getBridge() {
        return bridge;
    }
}
