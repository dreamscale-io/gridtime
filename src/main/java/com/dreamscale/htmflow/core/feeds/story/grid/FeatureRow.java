package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
public class FeatureRow extends FlowFeature {

    private final FlowFeature feature;

    private GridMetrics allTimeBucket = new GridMetrics();
    private Map<MusicGeometryClock.Coords, GridMetrics> gridMetricsPerTimeBucket = new HashMap<>();

    public FeatureRow(FlowFeature feature) {
        this.feature = feature;
    }


    public GridMetrics findOrCreateMetrics(MusicGeometryClock.Coords coords) {
        GridMetrics gridMetrics = gridMetricsPerTimeBucket.get(coords);
        if (gridMetrics == null) {
            gridMetrics = new GridMetrics(allTimeBucket);
            gridMetricsPerTimeBucket.put(coords, gridMetrics);
        }
        return gridMetrics;
    }

    public Set<MusicGeometryClock.Coords> getTimingKeys() {
        return gridMetricsPerTimeBucket.keySet();
    }

}
