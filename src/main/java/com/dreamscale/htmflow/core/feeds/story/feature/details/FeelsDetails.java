package com.dreamscale.htmflow.core.feeds.story.feature.details;

import lombok.Getter;

@Getter
public class FeelsDetails extends Details {

    private Integer flameRating;

    public FeelsDetails(Integer flameRating) {

        this.flameRating = flameRating;
    }


}
