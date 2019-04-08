package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.nio.file.attribute.AttributeView;
import java.time.LocalDateTime;
import java.util.List;

public class TimeBandLayer extends FlowFeature {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final List<TimeBand> timeBands;
    private final BandLayerType layerType;

    public TimeBandLayer(BandLayerType layerType, LocalDateTime from, LocalDateTime to, List<TimeBand> timeBands) {
        this.layerType = layerType;
        this.from = from;
        this.to = to;
        this.timeBands = timeBands;
    }

    public List<TimeBand> getTimeBands() {
        return timeBands;
    }

    public BandLayerType getLayerType() {
        return layerType;
    }
}
