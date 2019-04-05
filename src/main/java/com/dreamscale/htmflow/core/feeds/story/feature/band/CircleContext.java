package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.domain.json.LinkedMember;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class CircleContext implements BandContext {

    private final UUID circleId;

    public CircleContext(UUID circleId) {
        this.circleId = circleId;
    }
}
