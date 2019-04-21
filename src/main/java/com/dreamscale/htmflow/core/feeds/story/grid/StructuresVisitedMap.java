package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import lombok.Getter;

import java.util.*;

@Getter
public class StructuresVisitedMap  {

    private List<String> boxesVisited = new ArrayList<>();
    private List<String> locationsVisited = new ArrayList<>();
    private List<String> traversalsVisited = new ArrayList<>();
    private List<String> bridgesVisited = new ArrayList<>();
    private List<String> bubblesVisited = new ArrayList<>();

    private Map<String, String> uriAliasMap = new LinkedHashMap<>();

    private Map<String, FeatureMetrics> activityMetricsMap = new LinkedHashMap<>();


    public void addActivityForStructure(FlowFeature feature, GridMetrics metrics) {
        String uri = feature.getUri();
        if (uri == null) {
            uri = feature.getId().toString();
        }

        if (feature instanceof Box) {
            boxesVisited.add(uri);
        }
        if (feature instanceof LocationInBox) {
            locationsVisited.add(uri);
        }
        if (feature instanceof Traversal) {
            traversalsVisited.add(uri);
        }
        if (feature instanceof Bridge) {
            bridgesVisited.add(uri);
        }
        if (feature instanceof ThoughtBubble) {
            bubblesVisited.add(uri);
        }

        activityMetricsMap.put(uri, new FeatureMetrics(feature, metrics));
    }


    public void addAliasForStructure(String uriFrom, String uriTo) {
        uriAliasMap.put(uriFrom, uriTo);
    }

    public FeatureMetrics getFeatureMetrics(String uri) {
        String aliasMappedUri = uriAliasMap.get(uri);

        if (aliasMappedUri != null) {
            return activityMetricsMap.get(aliasMappedUri);
        } else {
            return activityMetricsMap.get(uri);
        }
    }


}
