package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.sequence.MovementEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Bridge;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInPlace;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.FocalPoint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpatialGeometryMapper {

    private Map<String, FocalPoint> placeMap = new HashMap<>();
    private Map<String, Bridge> bridgeMap = new HashMap<>();

    private FocalPoint currentPlace;

    public List<MovementEvent> gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<MovementEvent> movements = new ArrayList<>();

        if (currentPlace == null) {
            FocalPoint place = findOrCreatePlace(placeName, locationPath);
            currentPlace = place;
            movements.add(new MovementEvent(moment, place));

            MovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentPlace.getName().equals(placeName)) {

            MovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentPlace.getName().equals(placeName)) {

            FocalPoint newPlace = findOrCreatePlace(placeName, locationPath);
            List<MovementEvent> bridgeMovements = crossBridge(moment, currentPlace, newPlace, locationPath, timeInLocation);
            movements.addAll(bridgeMovements);
        }

        return movements;
    }

    private MovementEvent gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInPlace locationInPlace = currentPlace.goToLocation(locationPath, timeInLocation);
        return new MovementEvent(moment, locationInPlace);
    }


    private List<MovementEvent> crossBridge(LocalDateTime moment, FocalPoint fromPlace, FocalPoint toPlace, String toLocationPath, Duration timeInLocation) {

        List<MovementEvent> movements = new ArrayList<>();

        LocationInPlace fromLocation = fromPlace.getCurrentLocation();
        LocationInPlace exitLocation = fromPlace.exit();

        movements.add(new MovementEvent(moment, exitLocation));

        LocationInPlace enterLocation = toPlace.enter();
        LocationInPlace toLocation = toPlace.goToLocation(toLocationPath, timeInLocation);
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
            this.placeMap.put(place.getName(), place);
        }
        return place;
    }

    private Bridge findOrCreateBridge(LocationInPlace fromLocation, LocationInPlace toLocation) {
        String fromLocationKey = fromLocation.getPlaceName() + ":" + fromLocation.getLocationPath();
        String toLocationKey = toLocation.getPlaceName() + ":" + toLocation.getLocationPath();

        String bridgeKey = fromLocationKey + "=>" + toLocationKey;

        Bridge bridge = this.bridgeMap.get(bridgeKey);
        if (bridge == null) {
            bridge = new Bridge(bridgeKey, fromLocation, toLocation);
            this.bridgeMap.put(bridgeKey, bridge);

        }
        return bridge;
    }

    public List<FocalPoint> getPlaceStructure() {

       return new ArrayList<>(placeMap.values());

    }

    public FocalPoint getCurrentFocalPoint() {
        return currentPlace;
    }

    public LocationInPlace getCurrentLocationInPlace() {
        LocationInPlace location = null;
        if (currentPlace != null) {
            location = currentPlace.getCurrentLocation();
        }
        return location;
    }

    public void initPlaceFromPriorContext(FocalPoint place, LocationInPlace locationInPlace) {
        if (place != null) {
            currentPlace = new FocalPoint(place.getName(), locationInPlace.getLocationPath());
        }

    }

    public void modifyCurrentLocation(int modificationCount) {
        if (currentPlace != null) {
            currentPlace.modifyCurrentLocation(modificationCount);
        }
    }
}
