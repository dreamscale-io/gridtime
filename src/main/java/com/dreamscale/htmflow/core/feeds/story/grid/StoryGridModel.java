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

    private StoryGridSummary storyGridSummary;

    private StructuredMetricsMap structuredMetricsMap = new StructuredMetricsMap();

    private List<Column> columns;

    public StoryGridModel() {
        super(FlowObjectType.STORY_GRID);
    }

    public void addActivityForStructure(FlowFeature feature, GridMetrics metrics) {
        structuredMetricsMap.addActivityForStructure(feature,  metrics);
    }

    public List<String> getBoxesVisited() {
        return structuredMetricsMap.getBoxesVisited();
    }

    public List<String> getLocationsVisited() {
        return structuredMetricsMap.getLocationsVisited();
    }

    public List<String> getTraversalsVisited() {
        return structuredMetricsMap.getTraversalsVisited();
    }

    public List<String> getBridgesVisited() {
        return structuredMetricsMap.getBridgesVisited();
    }

    public List<String> getBubblesVisited() {
        return structuredMetricsMap.getBubblesVisited();
    }

    public FeatureMetrics getFeatureMetrics(String uri) {
        return structuredMetricsMap.getFeatureMetrics(uri);
    }

    public FeatureMetrics getFeatureMetricsForColumn(String uri, int columnIndex) {
        return columns.get(columnIndex).getFeatureMetrics(uri);
    }

    @JsonIgnore
    public Set<String> getAllFeaturesVisited() {
        return structuredMetricsMap.getAllFeaturesVisited();
    }
}
