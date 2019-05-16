package com.dreamscale.htmflow.core.feeds.story.grid;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.music.MusicClock;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Column extends FlowFeature {

    private MusicClock.Beat beat;

    private Context projectContext;
    private Context taskContext;
    private Context intentionContext;

    private int feels;

    private boolean isTroubleshooting;
    private boolean isLearning;
    private boolean isProgress;

    private boolean isPairing;

    private List<Member> activeAuthors;

    private FeatureMetricsMap featureMetricsMap = new FeatureMetricsMap();

    private int relativeSequence;


    public Column(MusicClock.Beat beat) {
        this();
        this.beat = beat;
    }

    public Column() {
        super(FlowObjectType.STORY_GRID_COLUMN);
    }

    @JsonIgnore
    public List<String> getBoxesVisited() {
        return featureMetricsMap.getBoxesVisited();
    }
    @JsonIgnore
    public List<String> getLocationsVisited() {
        return featureMetricsMap.getLocationsVisited();
    }
    @JsonIgnore
    public List<String> getTraversalsVisited() {
        return featureMetricsMap.getTraversalsVisited();
    }
    @JsonIgnore
    public List<String> getBridgesVisited() {
        return featureMetricsMap.getBridgesVisited();
    }
    @JsonIgnore
    public List<String> getBubblesVisited() {
        return featureMetricsMap.getBubblesVisited();
    }
    @JsonIgnore
    public FeatureMetrics getFeatureMetrics(String uri) {
        return featureMetricsMap.getFeatureMetrics(uri);
    }

    @JsonIgnore
    public List<String> getExperimentContextUris() {
        return featureMetricsMap.getExperimentUris();
    }
    @JsonIgnore
    public List<String> getMessageContextUris() {
        return featureMetricsMap.getMessageUris();
    }

    public void addActivityForStructure(FlowFeature feature, GridMetrics metrics) {
        featureMetricsMap.addMetricsForFeature(feature,  metrics);
    }

    public void addAliasForStructure(String uriFrom, String uriTo) {
        featureMetricsMap.addAliasForFeature(uriFrom,  uriTo);
    }

    public void addExperimentContexts(List<String> experimentUris) {
        featureMetricsMap.addExperiment(experimentUris);
    }

    public void addMessageContexts(List<String> messageUris) {
        featureMetricsMap.addMessage(messageUris);
    }

    public void setActiveAuthors(List<Member> activeAuthors) {
        this.activeAuthors = activeAuthors;
    }



}
