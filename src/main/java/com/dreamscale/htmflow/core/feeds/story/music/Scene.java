package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Box;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Bridge;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Traversal;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGrid;

import java.util.*;

public class Scene {

    private final StoryGrid storyGrid;

    private Context projectContext;
    private Context taskContext;
    private Context intentionContext;

    private int activeFeels;
    private boolean isLearningFriction;
    private boolean isWTFFriction;
    private boolean isPairing;

    private LinkedList<Box> activeBoxes = new LinkedList<>();
    private LinkedList<LocationInBox> activeLocationsInBox = new LinkedList<>();
    private LinkedList<Traversal> activeTraversals = new LinkedList<>();
    private LinkedList<Bridge> activeBridges = new LinkedList<>();
    private LinkedList<ExecuteThing> activeExecutionEvents = new LinkedList<>();

    private Set<String> urisInFrame = new HashSet<>();

    public Scene(StoryGrid storyGrid) {
        this.storyGrid = storyGrid;
    }

    public void snapshot(MusicGeometryClock.Coords coords) {
        compressIntoUrisAndMetrics(coords);

        Snapshot snapshot = new Snapshot(coords);
        snapshot.setProjectContext(projectContext);
        snapshot.setTaskContext(taskContext);
        snapshot.setIntentionContext(intentionContext);

        snapshot.setActiveBoxes(activeBoxes);
        snapshot.setActiveLocationsInBox(activeLocationsInBox);
        snapshot.setActiveTraversals(activeTraversals);
        snapshot.setActiveBridges(activeBridges);
        snapshot.setActiveExecutionEvents(activeExecutionEvents);

        snapshot.setUrisInFrame(urisInFrame);

        storyGrid.addSnapshot(snapshot);
    }

    public void panForwardTime() {
        urisInFrame.clear();

        removeAllButOne(activeBoxes);
        removeAllButOne(activeLocationsInBox);
        removeAllButOne(activeTraversals);
        removeAllButOne(activeBridges);
        removeAllButOne(activeExecutionEvents);
    }

    public void compressIntoUrisAndMetrics(MusicGeometryClock.Coords coords) {
        for(Box box : activeBoxes) {
            saveMetrics(box);
            urisInFrame.add(box.getUri());
        }

        for(LocationInBox location : activeLocationsInBox) {
            saveMetrics(location);
            urisInFrame.add(location.getUri());
        }

        for(Traversal traversal : activeTraversals) {
            saveMetrics(traversal);
            urisInFrame.add(traversal.getUri());
        }

        for(Bridge bridge : activeBridges) {
            saveMetrics(bridge);
            urisInFrame.add(bridge.getUri());
        }

        saveMetrics(projectContext);
        saveMetrics(taskContext);
        saveMetrics(intentionContext);
    }

    private void saveMetrics(FlowFeature flowFeature) {

        storyGrid.getMetricsFor(flowFeature).addFeelsSample(activeFeels);
        storyGrid.getMetricsFor(flowFeature).addPairingSample(isPairing);
        storyGrid.getMetricsFor(flowFeature).addLearningSample(isLearningFriction);
        storyGrid.getMetricsFor(flowFeature).addWtfSample(isWTFFriction);
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

    public void pushExecuteEvent(ExecuteThing executeEvent) {
        this.activeExecutionEvents.add(executeEvent);
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


    public void pushUrisInScene(String ... uris) {
        this.urisInFrame.addAll(Arrays.asList(uris));
    }


}
