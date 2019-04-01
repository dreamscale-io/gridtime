package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.sequence.MovementEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SpatialGeometryMapper {

    private Map<String, FocalPoint> placeMap = new HashMap<>();
    private Map<String, Bridge> bridgeMap = new HashMap<>();

    private List<FocalPoint> mainThoughtSequence = new ArrayList<>();
    private FocalPoint currentPlace;
    private BoxAndBridgeStructure extractedBoxAndBridgeStructure;

    public List<MovementEvent> gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<MovementEvent> movements = new ArrayList<>();

        if (currentPlace == null) {
            FocalPoint place = findOrCreatePlace(placeName, locationPath);
            currentPlace = place;
            movements.add(new MovementEvent(moment, place));

            MovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentPlace.getPlaceName().equals(placeName)) {

            MovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentPlace.getPlaceName().equals(placeName)) {

            FocalPoint nextPlace = findOrCreatePlace(placeName, locationPath);
            List<MovementEvent> bridgeMovements = crossBridge(moment, currentPlace, nextPlace, locationPath, timeInLocation);
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

        for (Bridge bridgeBetweenBoxes : bridgeMap.values()) {
            boxAndBridgeStructure.createBridge(bridgeBetweenBoxes);
        }

        extractedBoxAndBridgeStructure = boxAndBridgeStructure;

    }

    private MovementEvent gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInFocus locationInFocus = currentPlace.goToLocation(locationPath, timeInLocation);
        return new MovementEvent(moment, locationInFocus);
    }


    private List<MovementEvent> crossBridge(LocalDateTime moment, FocalPoint fromPlace, FocalPoint toPlace, String toLocationPath, Duration timeInLocation) {

        List<MovementEvent> movements = new ArrayList<>();

        LocationInFocus fromLocation = fromPlace.getCurrentLocation();
        LocationInFocus exitLocation = fromPlace.exit();

        movements.add(new MovementEvent(moment, exitLocation));

        LocationInFocus enterLocation = toPlace.enter();
        LocationInFocus toLocation = toPlace.goToLocation(toLocationPath, timeInLocation);
        Bridge bridgeCrossed = findOrCreateBridge(fromLocation, toLocation);
        bridgeCrossed.visit();

        movements.add(new MovementEvent(moment, bridgeCrossed));
        movements.add(new MovementEvent(moment, enterLocation));
        movements.add(new MovementEvent(moment, toLocation));

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

    private Bridge findOrCreateBridge(LocationInFocus fromLocation, LocationInFocus toLocation) {
        String fromLocationKey = fromLocation.getMainFocusName() + ":" + fromLocation.getLocationPath();
        String toLocationKey = toLocation.getMainFocusName() + ":" + toLocation.getLocationPath();

        String bridgeKey = fromLocationKey + "=>" + toLocationKey;

        Bridge bridge = this.bridgeMap.get(bridgeKey);
        if (bridge == null) {
            bridge = new Bridge(bridgeKey, fromLocation, toLocation);
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
