package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.details.CircleDetails;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
public class WTFFrictionBand extends Timeband {

    public WTFFrictionBand(LocalDateTime start, LocalDateTime end, CircleDetails circleDetails) {
        super(start, end, circleDetails);
    }

}
