package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.sequence.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SpatialGeometryMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;

    private Map<String, FocalPoint> placeMap = new HashMap<>();
    private Map<String, Bridge> bridgeMap = new HashMap<>();

    private List<FocalPoint> mainThoughtSequence = new ArrayList<>();
    private FocalPoint currentPlace;
    private BoxAndBridgeStructure extractedBoxAndBridgeStructure;

    public SpatialGeometryMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public List<Movement> gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        if (currentPlace == null) {
            FocalPoint place = findOrCreatePlace(placeName, locationPath);
            currentPlace = place;
            movements.add(new Movement(moment, place));

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentPlace.getPlaceName().equals(placeName)) {

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentPlace.getPlaceName().equals(placeName)) {

            FocalPoint nextPlace = findOrCreatePlace(placeName, locationPath);
            List<Movement> bridgeMovements = crossBridge(moment, currentPlace, nextPlace, locationPath, timeInLocation);
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

    public CarryOverContext getCarryOverContext() {
        CarryOverContext carryOverContext = new CarryOverContext();
        carryOverContext.setCurrentPlace(getCurrentPlace());

        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        FocalPoint place = carryOverContext.getCurrentPlace();

        if (place != null) {
            currentPlace = new FocalPoint(place.getPlaceName(), place.getCurrentLocation().getLocationPath());
        }
    }

    private Movement gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInFocus locationInFocus = currentPlace.goToLocation(locationPath, timeInLocation);
        return new Movement(moment, locationInFocus);
    }


    private List<Movement> crossBridge(LocalDateTime moment, FocalPoint fromPlace, FocalPoint toPlace, String toLocationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        LocationInFocus fromLocation = fromPlace.getCurrentLocation();
        LocationInFocus exitLocation = fromPlace.exit();

        movements.add(new Movement(moment, exitLocation));

        LocationInFocus enterLocation = toPlace.enter();
        LocationInFocus toLocation = toPlace.goToLocation(toLocationPath, timeInLocation);
        Bridge bridgeCrossed = findOrCreateBridge(fromLocation, toLocation);
        bridgeCrossed.visit();

        movements.add(new Movement(moment, bridgeCrossed));
        movements.add(new Movement(moment, enterLocation));
        movements.add(new Movement(moment, toLocation));

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


    public void modifyCurrentLocation(int modificationCount) {
        if (currentPlace != null) {
            currentPlace.modifyCurrentLocation(modificationCount);
        }
    }

    @Getter
    @Setter
    public static class CarryOverContext {
        FocalPoint currentPlace;
    }
}
