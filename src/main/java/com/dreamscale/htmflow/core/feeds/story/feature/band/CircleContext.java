package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.domain.json.LinkedMember;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class CircleContext implements BandContext {

    private final UUID circleId;
    private final String circleName;

    public CircleContext(UUID circleId, String circleName) {
        this.circleId = circleId;
        this.circleName = circleName;
    }
}
