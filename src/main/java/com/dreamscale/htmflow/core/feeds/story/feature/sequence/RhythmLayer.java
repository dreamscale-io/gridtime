package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.LocalDateTime;
import java.util.List;

public class RhythmLayer implements FlowFeature {

    private final LocalDateTime from;
    private final LocalDateTime to;
    List<Movement> movements;

    public RhythmLayer(LocalDateTime from, LocalDateTime to, List<Movement> movements) {
        this.from = from;
        this.to = to;
        this.movements = movements;
    }

    public List<Movement> getMovements() {
        return movements;
    }
}
