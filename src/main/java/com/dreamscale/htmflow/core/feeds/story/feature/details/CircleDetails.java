package com.dreamscale.htmflow.core.feeds.story.feature.details;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CircleDetails extends Details {

    private UUID circleId;
    private String circleName;

    public CircleDetails(UUID circleId, String circleName) {
        this();
        this.circleId = circleId;
        this.circleName = circleName;
    }

    public CircleDetails() {
        super(DetailsType.CIRCLE);
    }
}
