package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FeelsContext implements FlowFeature {

    private Integer flameRating;
    private Duration duration;

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return this.duration;
    }

}
