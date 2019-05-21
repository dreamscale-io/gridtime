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
public class TileGridModel extends FlowFeature {

    private FeatureMetricsMap featureMetricTotals = new FeatureMetricsMap();

    private List<Column> columns;

    public TileGridModel() {
        super(FlowObjectType.STORY_GRID);
    }

    public void addMetricTotalsForFeature(FlowFeature feature, GridMetrics metrics) {
        featureMetricTotals.addMetricsForFeature(feature,  metrics);
    }

    @JsonIgnore
    public FeatureMetrics getMetricTotals(String uri) {
        return featureMetricTotals.getFeatureMetrics(uri);
    }

    @JsonIgnore
    public Column getLastColumn() {
        if (columns != null && columns.size() > 0) {
            return columns.get(columns.size() - 1);
        }
        return null;
    }

    @JsonIgnore
    public Set<String> getAllFeaturesVisited() {
        return featureMetricTotals.getAllFeaturesVisited();
    }
}
