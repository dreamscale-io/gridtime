package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.MusicClock;
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
    private Map<MusicClock.Beat, GridMetrics> gridMetricsPerTimeBucket = new HashMap<>();

    public FeatureRow(FlowFeature feature) {
        this.feature = feature;
    }


    public GridMetrics findOrCreateMetrics(MusicClock.Beat beat) {
        GridMetrics gridMetrics = gridMetricsPerTimeBucket.get(beat);
        if (gridMetrics == null) {
            gridMetrics = new GridMetrics(allTimeBucket);
            gridMetricsPerTimeBucket.put(beat, gridMetrics);
        }
        return gridMetrics;
    }

    public Set<MusicClock.Beat> getTimingKeys() {
        return gridMetricsPerTimeBucket.keySet();
    }


}
