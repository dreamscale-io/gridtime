package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.Movement;

import java.time.LocalDateTime;
import java.util.List;

public class TimeBandLayer implements FlowFeature {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final List<TimeBand> timeBands;

    public TimeBandLayer(LocalDateTime from, LocalDateTime to, List<TimeBand> timeBands) {
        this.from = from;
        this.to = to;
        this.timeBands = timeBands;
    }

    public List<TimeBand> getTimeBands() {
        return timeBands;
    }
}
