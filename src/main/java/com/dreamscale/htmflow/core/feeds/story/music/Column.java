package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.PostCircleMessage;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.grid.FeatureMetrics;
import com.dreamscale.htmflow.core.feeds.story.grid.GridMetrics;
import com.dreamscale.htmflow.core.feeds.story.grid.StructuresVisitedMap;
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

    private List<Member> activeAuthors;

    private StructuresVisitedMap structuresVisitedMap = new StructuresVisitedMap();

    private List<ExecuteThing> executionContexts = new ArrayList<>();
    private List<PostCircleMessage> messageContexts = new ArrayList<>();

    private int relativeSequence;


    public Column(MusicGeometryClock.Coords coords) {
        this.coords = coords;
    }

    public void addActivityForStructure(FlowFeature feature, GridMetrics metrics) {
        structuresVisitedMap.addActivityForStructure(feature,  metrics);
    }

    public void addAliasForStructure(String uriFrom, String uriTo) {
        structuresVisitedMap.addAliasForStructure(uriFrom,  uriTo);
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

    public void addExecutionContexts(LinkedList<ExecuteThing> activeExecutionContexts) {
        executionContexts.addAll(activeExecutionContexts);
    }

    public void addMessageContexts(LinkedList<PostCircleMessage> activeCircleMessageContexts) {
        messageContexts.addAll(activeCircleMessageContexts);
    }

    public void setActiveAuthors(List<Member> activeAuthors) {
        this.activeAuthors = activeAuthors;
    }

}
