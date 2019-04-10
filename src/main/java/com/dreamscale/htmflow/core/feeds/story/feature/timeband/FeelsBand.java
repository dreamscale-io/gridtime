package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.details.FeelsDetails;

import java.time.LocalDateTime;

public class FeelsBand extends TimeBand {

    private final FeelsDetails feelsDetails;

    public FeelsBand(LocalDateTime start, LocalDateTime end, FeelsDetails feelsDetails) {
        super(start, end, feelsDetails);
        this.feelsDetails = feelsDetails;
    }

    public int getFeels() {
        return feelsDetails.getFlameRating();
    }

}
