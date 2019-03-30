package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.sequence.IdeaFlowMovementEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class IdeaFlowSpatialGeometryMapper {

    private Map<String, FocalPoint> placeMap = new HashMap<>();
    private Map<String, ThoughtBridge> bridgeMap = new HashMap<>();

    private FocalPoint currentPlace;
    private RadialBoxAndBridgeStructure boxStructure;

    public List<IdeaFlowMovementEvent> gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<IdeaFlowMovementEvent> movements = new ArrayList<>();

        if (currentPlace == null) {
            FocalPoint place = findOrCreatePlace(placeName, locationPath);
            currentPlace = place;
            movements.add(new IdeaFlowMovementEvent(moment, place));

            IdeaFlowMovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentPlace.getName().equals(placeName)) {

            IdeaFlowMovementEvent movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentPlace.getName().equals(placeName)) {

            FocalPoint newPlace = findOrCreatePlace(placeName, locationPath);
            List<IdeaFlowMovementEvent> bridgeMovements = crossBridge(moment, currentPlace, newPlace, locationPath, timeInLocation);
            movements.addAll(bridgeMovements);
        }

        return movements;
    }

    public void finish() {

        this.boxStructure = new RadialBoxAndBridgeStructure();

        Collection<FocalPoint> places = placeMap.values();
        for (FocalPoint place : places) {
            place.buildRadialStructure();
        }

        boxStructure.addPlacesAsBoxes(places);
        boxStructure.addBridgesBetweenBoxes(bridgeMap.values());

        boxStructure.finish();

    }

    private IdeaFlowMovementEvent gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInThought locationInPlace = currentPlace.goToLocation(locationPath, timeInLocation);
        return new IdeaFlowMovementEvent(moment, locationInPlace);
    }


    private List<IdeaFlowMovementEvent> crossBridge(LocalDateTime moment, FocalPoint fromPlace, FocalPoint toPlace, String toLocationPath, Duration timeInLocation) {

        List<IdeaFlowMovementEvent> movements = new ArrayList<>();

        LocationInThought fromLocation = fromPlace.getCurrentLocation();
        LocationInThought exitLocation = fromPlace.exit();

        movements.add(new IdeaFlowMovementEvent(moment, exitLocation));

        LocationInThought enterLocation = toPlace.enter();
        LocationInThought toLocation = toPlace.goToLocation(toLocationPath, timeInLocation);
        ThoughtBridge bridgeCrossed = findOrCreateBridge(fromLocation, toLocation);
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
            this.placeMap.put(place.getName(), place);
        }
        return place;
    }

    private ThoughtBridge findOrCreateBridge(LocationInThought fromLocation, LocationInThought toLocation) {
        String fromLocationKey = fromLocation.getPlaceName() + ":" + fromLocation.getLocationPath();
        String toLocationKey = toLocation.getPlaceName() + ":" + toLocation.getLocationPath();

        String bridgeKey = fromLocationKey + "=>" + toLocationKey;

        ThoughtBridge bridge = this.bridgeMap.get(bridgeKey);
        if (bridge == null) {
            bridge = new ThoughtBridge(bridgeKey, fromLocation, toLocation);
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

    public LocationInThought getCurrentLocationInPlace() {
        LocationInThought location = null;
        if (currentPlace != null) {
            location = currentPlace.getCurrentLocation();
        }
        return location;
    }

    public void initPlaceFromPriorContext(FocalPoint place, LocationInThought locationInPlace) {
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
