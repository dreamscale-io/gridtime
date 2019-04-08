package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class RhythmLayer extends FlowFeature {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final RhythmLayerType layerType;
    List<Movement> movements;

    public RhythmLayer(RhythmLayerType layerType, LocalDateTime from, LocalDateTime to, List<Movement> movements) {
        this.layerType = layerType;
        this.from = from;
        this.to = to;
        this.movements = movements;
    }

    public List<Movement> getMovements() {
        return movements;
    }
}
