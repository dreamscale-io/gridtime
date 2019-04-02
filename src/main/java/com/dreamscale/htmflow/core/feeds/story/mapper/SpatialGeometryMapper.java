package com.dreamscale.htmflow.core.feeds.story.mapper;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SpatialGeometryMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;

    private Map<String, FocusPlace> placeMap = new HashMap<>();
    private Map<String, Bridge> bridgeMap = new HashMap<>();

    private List<FocusPlace> mainThoughtSequence = new ArrayList<>();
    private FocusPlace currentPlace;
    private BoxAndBridgeStructure extractedBoxAndBridgeStructure;

    public SpatialGeometryMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public List<Movement> gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        if (currentPlace == null) {
            FocusPlace place = findOrCreatePlace(placeName, locationPath);
            currentPlace = place;
            movements.add(new Movement(moment, place));

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentPlace.getPlaceName().equals(placeName)) {

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentPlace.getPlaceName().equals(placeName)) {

            FocusPlace nextPlace = findOrCreatePlace(placeName, locationPath);
            List<Movement> bridgeMovements = crossBridge(moment, currentPlace, nextPlace, locationPath, timeInLocation);
            movements.addAll(bridgeMovements);
        }

        return movements;
    }

    public void finish() {

        BoxAndBridgeStructure boxAndBridgeStructure = new BoxAndBridgeStructure();

        for (FocusPlace thought : mainThoughtSequence) {
            List<ThoughtBubble> thoughtBubbles = thought.getThoughtBubbles();
            boxAndBridgeStructure.createThoughtBox(thought.getPlaceName(), thoughtBubbles);
        }

        for (Bridge bridgeBetweenBoxes : bridgeMap.values()) {
            boxAndBridgeStructure.createBridge(bridgeBetweenBoxes);
        }

        extractedBoxAndBridgeStructure = boxAndBridgeStructure;

    }

    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();
        subContext.setCurrentFocusPlace(getCurrentFocusPlace());
        subContext.setCurrentLocationInPlace(getCurrentLocation());

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);
        FocusPlace place = subContext.getCurrentFocusPlace();

        if (place != null) {
            currentPlace = new FocusPlace(place.getPlaceName(), place.getCurrentLocationInPlace().getLocationPath());
        }
    }

    private Movement gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInPlace location = currentPlace.goToLocation(locationPath, timeInLocation);
        return new Movement(moment, location);
    }


    private List<Movement> crossBridge(LocalDateTime moment, FocusPlace fromPlace, FocusPlace toPlace, String toLocationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        LocationInPlace fromLocation = fromPlace.getCurrentLocationInPlace();
        LocationInPlace exitLocation = fromPlace.exit();

        movements.add(new Movement(moment, exitLocation));

        LocationInPlace enterLocation = toPlace.enter();
        LocationInPlace toLocation = toPlace.goToLocation(toLocationPath, timeInLocation);
        Bridge bridgeCrossed = findOrCreateBridge(fromLocation, toLocation);
        bridgeCrossed.visit();

        movements.add(new Movement(moment, bridgeCrossed));
        movements.add(new Movement(moment, enterLocation));
        movements.add(new Movement(moment, toLocation));

        currentPlace = toPlace;

        return movements;
    }


    private FocusPlace findOrCreatePlace(String placeName, String locationPath) {
        FocusPlace place = this.placeMap.get(placeName);
        if (place == null) {
            place = new FocusPlace(placeName, locationPath);
            this.placeMap.put(place.getPlaceName(), place);
            this.mainThoughtSequence.add(place);
        }
        return place;
    }

    private Bridge findOrCreateBridge(LocationInPlace fromLocation, LocationInPlace toLocation) {
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

    public FocusPlace getCurrentFocusPlace() {
        return currentPlace;
    }

    public LocationInPlace getCurrentLocation() {
        LocationInPlace location = null;
        if (currentPlace != null) {
            location = currentPlace.getCurrentLocationInPlace();
        }
        return location;
    }


    public void modifyCurrentLocation(int modificationCount) {
        if (currentPlace != null) {
            currentPlace.modifyCurrentLocationInFocus(modificationCount);
        }
    }

    public static class CarryOverSubContext {

        private static final String SUBCONTEXT_NAME = "[SpatialGeometryMapper]";

        private static final String CURRENT_FOCUS_PLACE = "current.focus.place";
        private static final String CURRENT_FOCUS_LOCATION_IN_PLACE = "current.focus.location.in.place";
        private final CarryOverContext subContext;

        public CarryOverSubContext() {
            subContext = new CarryOverContext(SUBCONTEXT_NAME);
        }

        public CarryOverSubContext(CarryOverContext carryOverContext) {
            subContext = carryOverContext.getSubContext(SUBCONTEXT_NAME);
        }

        void setCurrentFocusPlace(FocusPlace place) {
            subContext.addKeyValue(CURRENT_FOCUS_PLACE, place);
        }

        void setCurrentLocationInPlace(LocationInPlace location) {
            subContext.addKeyValue(CURRENT_FOCUS_LOCATION_IN_PLACE, location);
        }

        FocusPlace getCurrentFocusPlace() {
            return (FocusPlace) subContext.getValue(CURRENT_FOCUS_PLACE);
        }

        LocationInPlace getCurrentLocationInPlace() {
            return (LocationInPlace) subContext.getValue(CURRENT_FOCUS_PLACE);
        }

        public CarryOverContext toCarryOverContext() {
            return subContext;
        }
    }
}
