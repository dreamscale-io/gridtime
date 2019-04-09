package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.details.FeelsDetails;

import java.time.LocalDateTime;

public class FeelsBand extends TimeBand {

    public FeelsBand(LocalDateTime start, LocalDateTime end, FeelsDetails feelsDetails) {
        super(start, end, feelsDetails);
    }

}
