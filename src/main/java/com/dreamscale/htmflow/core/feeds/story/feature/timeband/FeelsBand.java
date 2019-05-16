package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.details.FeelsDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
public class FeelsBand extends Timeband {

    public FeelsBand(LocalDateTime start, LocalDateTime end, FeelsDetails feelsDetails) {
        super(start, end, feelsDetails);
    }

    @JsonIgnore
    public int getFeels() {
        return ((FeelsDetails)getDetails()).getFlameRating();
    }

}
