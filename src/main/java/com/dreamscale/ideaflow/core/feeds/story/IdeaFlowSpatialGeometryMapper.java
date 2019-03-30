package com.dreamscale.ideaflow.core.feeds.story;

import com.dreamscale.ideaflow.core.feeds.story.feature.sequence.IdeaFlowMovementEvent;
import com.dreamscale.ideaflow.core.feeds.story.feature.structure.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class IdeaFlowSpatialGeometryMapper {

    private Map<String, FocalPoint> thoughtMap = new HashMap<>();
    private Map<String, ThoughtBridge> bridgeMap = new HashMap<>();

    private FocalPoint currentThought;
    private BoxAndBridgeStructure extractedBoxAndBridgeStructure;

    public List<IdeaFlowMovementEvent> gotoLocation(LocalDateTime moment, String thoughtName, String locationPath, Duration timeInLocation) {

        List<IdeaFlowMovementEvent> movements = new ArrayList<>();

        if (currentThought == null) {
            FocalPoint thought = findOrCreateThought(thoughtName, locationPath);
            currentThought = thought;
            movements.add(new IdeaFlowMovementEvent(moment, thought));

            IdeaFlowMovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentThought.getName().equals(thoughtName)) {

            IdeaFlowMovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentThought.getName().equals(thoughtName)) {

            FocalPoint newPlace = findOrCreateThought(thoughtName, locationPath);
            List<IdeaFlowMovementEvent> bridgeMovements = crossBridge(moment, currentThought, newPlace, locationPath, timeInLocation);
            movements.addAll(bridgeMovements);
        }

        return movements;
    }

    public void finish() {

        this.extractedBoxAndBridgeStructure = new BoxAndBridgeStructure();

        Collection<FocalPoint> thoughts = thoughtMap.values();
        for (FocalPoint thought : thoughts) {
            thought.buildRadialStructure();
        }

        extractedBoxAndBridgeStructure.addMainThoughtsAsBoxes(thoughts);
        extractedBoxAndBridgeStructure.addBridgesBetweenThoughts(bridgeMap.values());

        extractedBoxAndBridgeStructure.finish();

    }

    private IdeaFlowMovementEvent gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInThought locationInPlace = currentThought.goToLocation(locationPath, timeInLocation);
        return new IdeaFlowMovementEvent(moment, locationInPlace);
    }


    private List<IdeaFlowMovementEvent> crossBridge(LocalDateTime moment, FocalPoint fromThought, FocalPoint toThought, String toLocationPath, Duration timeInLocation) {

        List<IdeaFlowMovementEvent> movements = new ArrayList<>();

        LocationInThought fromLocation = fromThought.getCurrentLocation();
        LocationInThought exitLocation = fromThought.exit();

        movements.add(new IdeaFlowMovementEvent(moment, exitLocation));

        LocationInThought enterLocation = toThought.enter();
        LocationInThought toLocation = toThought.goToLocation(toLocationPath, timeInLocation);
        ThoughtBridge bridgeCrossed = findOrCreateBridge(fromLocation, toLocation);
        bridgeCrossed.visit();

        movements.add(new IdeaFlowMovementEvent(moment, bridgeCrossed));
        movements.add(new IdeaFlowMovementEvent(moment, enterLocation));
        movements.add(new IdeaFlowMovementEvent(moment, toLocation));

        currentThought = toThought;

        return movements;
    }


    private FocalPoint findOrCreateThought(String name, String locationPath) {
        FocalPoint thought = this.thoughtMap.get(name);
        if (thought == null) {
            thought = new FocalPoint(name, locationPath);
            this.thoughtMap.put(thought.getName(), thought);
        }
        return thought;
    }

    private ThoughtBridge findOrCreateBridge(LocationInThought fromLocation, LocationInThought toLocation) {
        String fromLocationKey = fromLocation.getMainFocusName() + ":" + fromLocation.getLocationPath();
        String toLocationKey = toLocation.getMainFocusName() + ":" + toLocation.getLocationPath();

        String bridgeKey = fromLocationKey + "=>" + toLocationKey;

        ThoughtBridge bridge = this.bridgeMap.get(bridgeKey);
        if (bridge == null) {
            bridge = new ThoughtBridge(bridgeKey, fromLocation, toLocation);
            this.bridgeMap.put(bridgeKey, bridge);

        }
        return bridge;
    }

    public BoxAndBridgeStructure getThoughtStructure() {
       return extractedBoxAndBridgeStructure;
    }

    public FocalPoint getCurrentFocalPoint() {
        return currentThought;
    }

    public LocationInThought getCurrentLocationInThought() {
        LocationInThought location = null;
        if (currentThought != null) {
            location = currentThought.getCurrentLocation();
        }
        return location;
    }

    public void initFocalPointFromPriorContext(FocalPoint focalPoint, LocationInThought locationInPlace) {
        if (focalPoint != null) {
            currentThought = new FocalPoint(focalPoint.getName(), locationInPlace.getLocationPath());
        }

    }

    public void modifyCurrentLocation(int modificationCount) {
        if (currentThought != null) {
            currentThought.modifyCurrentLocation(modificationCount);
        }
    }
}
