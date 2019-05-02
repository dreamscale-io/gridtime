package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.grid.FeatureMetrics;
import com.dreamscale.htmflow.core.feeds.story.grid.GridMetrics;
import com.dreamscale.htmflow.core.feeds.story.grid.StructuredMetricsMap;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Column extends FlowFeature {

    private final MusicGeometryClock.Coords coords;

    private Context projectContext;
    private Context taskContext;
    private Context intentionContext;

    private int feels;

    private boolean isTroubleshooting;
    private boolean isLearning;
    private boolean isProgress;

    private boolean isPairing;

    private List<Member> activeAuthors;

    private StructuredMetricsMap structuredMetricsMap = new StructuredMetricsMap();

    private List<String> experimentContextUris = new ArrayList<>();
    private List<String> messageContextUris = new ArrayList<>();

    private int relativeSequence;


    public Column(MusicGeometryClock.Coords coords) {
        this.coords = coords;
    }

    public void addActivityForStructure(FlowFeature feature, GridMetrics metrics) {
        structuredMetricsMap.addActivityForStructure(feature,  metrics);
    }

    public void addAliasForStructure(String uriFrom, String uriTo) {
        structuredMetricsMap.addAliasForStructure(uriFrom,  uriTo);
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

    public void addExperimentContexts(List<String> experimentUris) {
        this.experimentContextUris.addAll(experimentUris);
    }

    public void addMessageContexts(List<String> messageUris) {
        this.messageContextUris.addAll(messageUris);
    }

    public void setActiveAuthors(List<Member> activeAuthors) {
        this.activeAuthors = activeAuthors;
    }

}
