package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
public class FeelsContext implements BandContext {

    private Integer flameRating;

    public FeelsContext(Integer flameRating) {

        this.flameRating = flameRating;
    }


}
