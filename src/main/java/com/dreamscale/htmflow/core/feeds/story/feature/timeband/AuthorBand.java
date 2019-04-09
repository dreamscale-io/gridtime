package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.details.CircleDetails;

import java.time.LocalDateTime;

public class AuthorBand extends TimeBand {

    public AuthorBand(LocalDateTime start, LocalDateTime end, AuthorDetails authorDetails) {
        super(start, end, authorDetails);
    }

}
