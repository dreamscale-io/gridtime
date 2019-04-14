package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeatureRow {


    private final FlowFeature feature;
    private Map<MusicGeometryClock.Coords, GridMetrics> gridMetricsPerTimeBucket = new HashMap<>();

    public FeatureRow(FlowFeature feature) {
        this.feature = feature;
    }

    public GridMetrics findOrCreateMetrics(MusicGeometryClock.Coords coords) {
        GridMetrics gridMetrics = gridMetricsPerTimeBucket.get(coords);
        if (gridMetrics == null) {
            gridMetrics = new GridMetrics();
            gridMetricsPerTimeBucket.put(coords, gridMetrics);
        }
        return gridMetrics;
    }

    public Set<MusicGeometryClock.Coords> getTimingKeys() {
        return gridMetricsPerTimeBucket.keySet();
    }
}
