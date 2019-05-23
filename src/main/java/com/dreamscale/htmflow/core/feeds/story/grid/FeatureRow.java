package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.clock.ClockBeat;
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
public class FeatureRow {

    private final FlowFeature feature;

    private GridMetrics allTimeBucket = new GridMetrics();
    private Map<ClockBeat, GridMetrics> gridMetricsPerTimeBucket = new HashMap<>();

    public FeatureRow(FlowFeature feature) {
        this.feature = feature;
    }


    public GridMetrics findOrCreateMetrics(ClockBeat clockBeat) {
        GridMetrics gridMetrics = gridMetricsPerTimeBucket.get(clockBeat);
        if (gridMetrics == null) {
            gridMetrics = new GridMetrics(allTimeBucket);
            gridMetricsPerTimeBucket.put(clockBeat, gridMetrics);
        }
        return gridMetrics;
    }

    public Set<ClockBeat> getTimingKeys() {
        return gridMetricsPerTimeBucket.keySet();
    }


}
