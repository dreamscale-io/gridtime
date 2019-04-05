package com.dreamscale.htmflow.core.feeds.story.feature.details;

import lombok.Getter;

import java.util.UUID;

@Getter
public class CircleDetails implements Details {

    private final UUID circleId;
    private final String circleName;

    public CircleDetails(UUID circleId, String circleName) {
        this.circleId = circleId;
        this.circleName = circleName;
    }
}
