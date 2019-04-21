package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.music.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class StoryGridModel extends FlowFeature {

    private StructuresVisitedMap structuresVisitedMap = new StructuresVisitedMap();

    private List<Column> columns;


    public void addActivityForStructure(FlowFeature feature, GridMetrics metrics) {
        structuresVisitedMap.addActivityForStructure(feature,  metrics);
    }

    public List<String> getBoxesVisited() {
        return structuresVisitedMap.getBoxesVisited();
    }

    public List<String> getLocationsVisited() {
        return structuresVisitedMap.getLocationsVisited();
    }

    public List<String> getTraversalsVisited() {
        return structuresVisitedMap.getTraversalsVisited();
    }

    public List<String> getBridgesVisited() {
        return structuresVisitedMap.getBridgesVisited();
    }

    public List<String> getBubblesVisited() {
        return structuresVisitedMap.getBubblesVisited();
    }

    public FeatureMetrics getFeatureMetrics(String uri) {
        return structuresVisitedMap.getFeatureMetrics(uri);
    }

}
