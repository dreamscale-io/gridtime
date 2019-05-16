package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.PostCircleMessage;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.grid.Column;
import com.dreamscale.htmflow.core.feeds.story.grid.GridMetrics;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGrid;

import java.util.*;

public class Scene {

    private final StoryGrid storyGrid;

    private Context projectContext;
    private Context taskContext;
    private Context intentionContext;

    private List<Member> activeAuthors;

    private int activeFeels;
    private boolean isLearningFriction;
    private boolean isWTFFriction;
    private boolean isPairing;

    private LinkedList<Box> activeBoxes = new LinkedList<>();
    private LinkedList<LocationInBox> activeLocationsInBox = new LinkedList<>();
    private LinkedList<Traversal> activeTraversals = new LinkedList<>();
    private LinkedList<Bridge> activeBridges = new LinkedList<>();
    private LinkedList<ThoughtBubble> activeBubbles = new LinkedList<>();
    private LinkedList<ThoughtBubble.RingLocation> activeRingLocations = new LinkedList<>();
    private LinkedList<ThoughtBubble.Link> activeRingLinks = new LinkedList<>();

    private LinkedList<ExecuteThing> activeExperimentContexts = new LinkedList<>();
    private LinkedList<PostCircleMessage> activeMessageContexts = new LinkedList<>();



    public Scene(StoryGrid storyGrid) {
        this.storyGrid = storyGrid;
    }

    public void snapshot(MusicClock.Beat beat) {
        correlateMetricsWithinFrame(beat);

        Column column = new Column(beat);
        column.setProjectContext(projectContext);
        column.setTaskContext(taskContext);
        column.setIntentionContext(intentionContext);
        column.setActiveAuthors(activeAuthors);

        column.setFeels(activeFeels);

        column.setTroubleshooting(isWTFFriction);
        column.setLearning(isNotWTFAndIsLearning());
        column.setProgress(isNotWTFAndIsNotLearning());
        column.setPairing(isPairing);

        //static structures

        addStructureActivityMetrics(column, activeBoxes, beat);
        addStructureActivityMetrics(column, activeLocationsInBox, beat);
        addStructureActivityMetrics(column, activeTraversals, beat);
        addStructureActivityMetrics(column, activeBridges, beat);

        addAggregateActivityMetrics(column, activeBubbles, beat);

        addRingLocationAliases(column, activeRingLocations);
        addRingLinkAliases(column, activeRingLinks);

        //temporal structures

        column.addExperimentContexts(extractUris(activeExperimentContexts));
        column.addMessageContexts(extractUris(activeMessageContexts));

        storyGrid.addColumn(column);
    }

    private boolean isNotWTFAndIsLearning() {
        if (isWTFFriction) {
            return false;
        }
        return isLearningFriction;
    }

    private boolean isNotWTFAndIsNotLearning() {
        return !isWTFFriction && !isLearningFriction;
    }

    private List<String> extractUris(List<? extends FlowFeature> features) {
        List<String> uris = new ArrayList<>();
        for (FlowFeature feature : features) {
            uris.add(feature.getUri());
        }
        return uris;
    }

    private void addRingLocationAliases(Column column, LinkedList<ThoughtBubble.RingLocation> activeRingLocations) {
        for (ThoughtBubble.RingLocation ringLocation : activeRingLocations) {
            column.addAliasForStructure(ringLocation.getUri(), ringLocation.getLocation().getUri());
        }
    }

    private void addRingLinkAliases(Column column, LinkedList<ThoughtBubble.Link> activeRingLinks) {
        for (ThoughtBubble.Link ringLink : activeRingLinks) {
            column.addAliasForStructure(ringLink.getUri(), ringLink.getTraversal().getUri());
        }
    }

    private void addAggregateActivityMetrics(Column column, List<? extends FlowFeature> features, MusicClock.Beat beat) {
        for (FlowFeature feature : features) {
            GridMetrics metrics = storyGrid.getAggregateMetricsFor(feature, beat);
            column.addActivityForStructure(feature, metrics);
        }
    }

    private void addStructureActivityMetrics(Column column, List<? extends FlowFeature> features, MusicClock.Beat beat) {
        for (FlowFeature feature : features) {
            column.addActivityForStructure(feature, storyGrid.getMetricsFor(feature, beat));
        }
    }

    public void panForwardTime() {
        removeAllButOne(activeBoxes);
        removeAllButOne(activeLocationsInBox);
        removeAllButOne(activeTraversals);
        removeAllButOne(activeBridges);
        removeAllButOne(activeBubbles);
        removeAllButOne(activeRingLocations);
        removeAllButOne(activeRingLinks);

        removeAllButOne(activeExperimentContexts);
        removeAllButOne(activeMessageContexts);
    }

    public void correlateMetricsWithinFrame(MusicClock.Beat beat) {
        for(Box box : activeBoxes) {
            saveMetrics(box, beat);
        }

        for(LocationInBox location : activeLocationsInBox) {
            saveMetrics(location, beat);
        }

        for(Traversal traversal : activeTraversals) {
            saveMetrics(traversal, beat);
        }

        for(Bridge bridge : activeBridges) {
            saveMetrics(bridge, beat);
        }

        saveMetrics(projectContext, beat);
        saveMetrics(taskContext, beat);
        saveMetrics(intentionContext, beat);
    }

    private void saveMetrics(FlowFeature flowFeature, MusicClock.Beat beat) {

        storyGrid.getMetricsFor(flowFeature, beat).addFeelsSample(activeFeels);
        storyGrid.getMetricsFor(flowFeature, beat).addPairingSample(isPairing);
        storyGrid.getMetricsFor(flowFeature, beat).addLearningSample(isLearningFriction);
        storyGrid.getMetricsFor(flowFeature, beat).addWtfSample(isWTFFriction);
    }

    private void removeAllButOne(LinkedList<?> activeItems) {
        for (int i = 0; i < activeItems.size() - 1; i++) {
            activeItems.removeLast();
        }
    }

    public void updateFeels(int feels) {
        this.activeFeels = feels;
    }


    public void updateLearningFriction(boolean isLearningFriction) {
        this.isLearningFriction = isLearningFriction;
    }

    public void setIsPairing(boolean isPairing) {
        this.isPairing = isPairing;
    }

    public void updateWTFFriction(boolean isWTFFriction) {
        this.isWTFFriction = isWTFFriction;
    }

    public void pushActiveBox(Box box) {
        this.activeBoxes.push(box);
    }

    public void pushActiveLocation(LocationInBox locationInBox) {
        this.activeLocationsInBox.push(locationInBox);
    }

    public void pushActiveTraversal(Traversal traversal) {
        this.activeTraversals.push(traversal);
    }

    public void pushActiveBridge(Bridge bridge) {
        this.activeBridges.push(bridge);
    }

    public void pushActiveBubble(ThoughtBubble bubble) {
        this.activeBubbles.push(bubble);
    }

    public void pushActiveRingLocation(ThoughtBubble.RingLocation ringLocation) {
        this.activeRingLocations.push(ringLocation);
    }

    public void pushActiveRingLink(ThoughtBubble.Link ringLink) {
        if (ringLink == null) {
            System.out.println("hello!");
        }

        this.activeRingLinks.push(ringLink);
    }

    public void pushExecuteEvent(ExecuteThing executeEvent) {
        this.activeExperimentContexts.add(executeEvent);
    }

    public void pushCircleMessageEvent(PostCircleMessage circleMessageEvent) {
        this.activeMessageContexts.add(circleMessageEvent);
    }

    public void changeActiveAuthors(List<Member> authors) {
        this.activeAuthors = authors;
    }

    public void changeProjectContext(Context context) {
        this.projectContext = context;
    }

    public void changeTaskContext(Context context) {
        this.taskContext = context;
    }

    public void changeIntentionContext(Context context) {
        this.intentionContext = context;
    }



}
