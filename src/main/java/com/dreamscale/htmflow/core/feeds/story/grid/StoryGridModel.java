package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class StoryGridModel extends FlowFeature {

    private FeatureMetricsMap featureMetricTotals = new FeatureMetricsMap();

    private List<Column> columns;

    public StoryGridModel() {
        super(FlowObjectType.STORY_GRID);
    }

    public void addMetricTotalsForFeature(FlowFeature feature, GridMetrics metrics) {
        featureMetricTotals.addMetricsForFeature(feature,  metrics);
    }


    @JsonIgnore
    public Set<String> getAllFeaturesVisited() {
        return featureMetricTotals.getAllFeaturesVisited();
    }
}
