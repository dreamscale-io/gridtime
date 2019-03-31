package com.dreamscale.ideaflow.core.feeds.story;

import com.dreamscale.ideaflow.core.feeds.story.feature.sequence.IdeaFlowMovementEvent;
import com.dreamscale.ideaflow.core.feeds.story.feature.structure.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class IdeaFlowSpatialGeometryMapper {

    private Map<String, FocalPoint> placeMap = new HashMap<>();
    private Map<String, BridgeStructure> bridgeMap = new HashMap<>();

    private List<FocalPoint> mainThoughtSequence = new ArrayList<>();
    private FocalPoint currentPlace;
    private BoxAndBridgeStructure extractedBoxAndBridgeStructure;

    public List<IdeaFlowMovementEvent> gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<IdeaFlowMovementEvent> movements = new ArrayList<>();

        if (currentPlace == null) {
            FocalPoint place = findOrCreatePlace(placeName, locationPath);
            currentPlace = place;
            movements.add(new IdeaFlowMovementEvent(moment, place));

            IdeaFlowMovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentPlace.getPlaceName().equals(placeName)) {

            IdeaFlowMovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentPlace.getPlaceName().equals(placeName)) {

            FocalPoint nextPlace = findOrCreatePlace(placeName, locationPath);
            List<IdeaFlowMovementEvent> bridgeMovements = crossBridge(moment, currentPlace, nextPlace, locationPath, timeInLocation);
            movements.addAll(bridgeMovements);
        }

        return movements;
    }

    public void finish() {

        BoxAndBridgeStructure boxAndBridgeStructure = new BoxAndBridgeStructure();

        for (FocalPoint thought : mainThoughtSequence) {
            List<ThoughtBubble> thoughtBubbles = thought.getThoughtBubbles();
            boxAndBridgeStructure.createThoughtBox(thought.getPlaceName(), thoughtBubbles);
        }

        for (BridgeStructure bridgeBetweenBoxes : bridgeMap.values()) {
            boxAndBridgeStructure.createBridge(bridgeBetweenBoxes);
        }

        extractedBoxAndBridgeStructure = boxAndBridgeStructure;

    }

    private IdeaFlowMovementEvent gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInFocus locationInFocus = currentPlace.goToLocation(locationPath, timeInLocation);
        return new IdeaFlowMovementEvent(moment, locationInFocus);
    }


    private List<IdeaFlowMovementEvent> crossBridge(LocalDateTime moment, FocalPoint fromPlace, FocalPoint toPlace, String toLocationPath, Duration timeInLocation) {

        List<IdeaFlowMovementEvent> movements = new ArrayList<>();

        LocationInFocus fromLocation = fromPlace.getCurrentLocation();
        LocationInFocus exitLocation = fromPlace.exit();

        movements.add(new IdeaFlowMovementEvent(moment, exitLocation));

        LocationInFocus enterLocation = toPlace.enter();
        LocationInFocus toLocation = toPlace.goToLocation(toLocationPath, timeInLocation);
        BridgeStructure bridgeCrossed = findOrCreateBridge(fromLocation, toLocation);
        bridgeCrossed.visit();

        movements.add(new IdeaFlowMovementEvent(moment, bridgeCrossed));
        movements.add(new IdeaFlowMovementEvent(moment, enterLocation));
        movements.add(new IdeaFlowMovementEvent(moment, toLocation));

        currentPlace = toPlace;

        return movements;
    }


    private FocalPoint findOrCreatePlace(String placeName, String locationPath) {
        FocalPoint place = this.placeMap.get(placeName);
        if (place == null) {
            place = new FocalPoint(placeName, locationPath);
            this.placeMap.put(place.getPlaceName(), place);
            this.mainThoughtSequence.add(place);
        }
        return place;
    }

    private BridgeStructure findOrCreateBridge(LocationInFocus fromLocation, LocationInFocus toLocation) {
        String fromLocationKey = fromLocation.getMainFocusName() + ":" + fromLocation.getLocationPath();
        String toLocationKey = toLocation.getMainFocusName() + ":" + toLocation.getLocationPath();

        String bridgeKey = fromLocationKey + "=>" + toLocationKey;

        BridgeStructure bridge = this.bridgeMap.get(bridgeKey);
        if (bridge == null) {
            bridge = new BridgeStructure(bridgeKey, fromLocation, toLocation);
            this.bridgeMap.put(bridgeKey, bridge);

        }
        return bridge;
    }

    public BoxAndBridgeStructure getThoughtStructure() {
       return extractedBoxAndBridgeStructure;
    }

    public FocalPoint getCurrentPlace() {
        return currentPlace;
    }

    public LocationInFocus getCurrentLocationInPlace() {
        LocationInFocus location = null;
        if (currentPlace != null) {
            location = currentPlace.getCurrentLocation();
        }
        return location;
    }

    public void initPlaceFromPriorContext(FocalPoint place, LocationInFocus locationInFocus) {
        if (place != null) {
            currentPlace = new FocalPoint(place.getPlaceName(), locationInFocus.getLocationPath());
        }

    }

    public void modifyCurrentLocation(int modificationCount) {
        if (currentPlace != null) {
            currentPlace.modifyCurrentLocation(modificationCount);
        }
    }
}
