package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveAcrossBridge;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveToLocation;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveToBox;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SpatialGeometryMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;

    private Map<String, FocalPoint> focalPointMap = new HashMap<>();
    private Map<String, Bridge> bridgeMap = new HashMap<>();

    private List<FocalPoint> mainThoughtSequence = new ArrayList<>();
    private FocalPoint currentFocus;
    private BoxAndBridgeStructure extractedBoxAndBridgeStructure;

    public SpatialGeometryMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public List<Movement> gotoLocation(LocalDateTime moment, String boxName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        if (currentFocus == null) {
            FocalPoint placeInBox = findOrCreateFocalPoint(boxName, locationPath);
            currentFocus = placeInBox;
            movements.add(new MoveToBox(moment, placeInBox.getBox()));

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentFocus.getBoxName().equals(boxName)) {

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentFocus.getBoxName().equals(boxName)) {

            FocalPoint otherBox = findOrCreateFocalPoint(boxName, locationPath);
            List<Movement> bridgeMovements = crossBridge(moment, currentFocus, otherBox, locationPath, timeInLocation);
            movements.addAll(bridgeMovements);
        }

        return movements;
    }

    public void finish() {

        BoxAndBridgeStructure boxAndBridgeStructure = new BoxAndBridgeStructure();

        for (FocalPoint thought : mainThoughtSequence) {
            thought.loadThoughtsIntoBox();
            boxAndBridgeStructure.addBoxOfThoughts(thought.getBox());
        }

        for (Bridge bridgeBetweenBoxes : bridgeMap.values()) {
            boxAndBridgeStructure.createBridge(bridgeBetweenBoxes);
        }

        extractedBoxAndBridgeStructure = boxAndBridgeStructure;

    }

    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();
        subContext.setCurrentBox(getCurrentFocus().getBox());
        subContext.setCurrentLocationInBox(getCurrentLocation());

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);
        Box box = subContext.getCurrentBox();
        LocationInBox location = subContext.getCurrentLocationInBox();

        if (box != null) {
            currentFocus = new FocalPoint(box.getBoxName(), location.getLocationPath());
        }
    }

    private Movement gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInBox location = currentFocus.goToLocation(locationPath, timeInLocation);
        return new MoveToLocation(moment, location, currentFocus.getLastTraversal());
    }


    private List<Movement> crossBridge(LocalDateTime moment, FocalPoint fromBox, FocalPoint toBox, String toLocationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        LocationInBox fromLocation = fromBox.getCurrentLocation();
        LocationInBox exitLocation = fromBox.exit();

        movements.add(new MoveToLocation(moment, exitLocation, fromBox.getLastTraversal()));

        LocationInBox enterLocation = toBox.enter();
        LocationInBox toLocation = toBox.goToLocation(toLocationPath, timeInLocation);
        Bridge bridgeCrossed = findOrCreateBridge(fromLocation, toLocation);
        bridgeCrossed.visit();

        movements.add(new MoveAcrossBridge(moment, bridgeCrossed));
        movements.add(new MoveToLocation(moment, enterLocation, toBox.getLastTraversal()));
        movements.add(new MoveToLocation(moment, toLocation, toBox.getLastTraversal()));

        currentFocus = toBox;

        return movements;
    }


    private FocalPoint findOrCreateFocalPoint(String boxName, String locationPath) {
        FocalPoint focalPoint = this.focalPointMap.get(boxName);
        if (focalPoint == null) {
            focalPoint = new FocalPoint(boxName, locationPath);
            this.focalPointMap.put(focalPoint.getBoxName(), focalPoint);
            this.mainThoughtSequence.add(focalPoint);
        }
        return focalPoint;
    }

    private Bridge findOrCreateBridge(LocationInBox fromLocation, LocationInBox toLocation) {
        String fromLocationKey = fromLocation.toKey();
        String toLocationKey = toLocation.toKey();

        String bridgeKey = StandardizedKeyMapper.createBridgeKey(fromLocationKey, toLocationKey);

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

    public FocalPoint getCurrentFocus() {
        return currentFocus;
    }

    public LocationInBox getCurrentLocation() {
        LocationInBox location = null;
        if (currentFocus != null) {
            location = currentFocus.getCurrentLocation();
        }
        return location;
    }


    public void modifyCurrentLocation(int modificationCount) {
        if (currentFocus != null) {
            currentFocus.modifyCurrentLocationInFocus(modificationCount);
        }
    }

    public static class CarryOverSubContext {

        private static final String SUBCONTEXT_NAME = "[SpatialGeometryMapper]";

        private static final String CURRENT_FOCUS_BOX = "current.focus.box";
        private static final String CURRENT_FOCUS_LOCATION_IN_BOX = "current.focus.location.in.box";
        private final CarryOverContext subContext;

        public CarryOverSubContext() {
            subContext = new CarryOverContext(SUBCONTEXT_NAME);
        }

        public CarryOverSubContext(CarryOverContext carryOverContext) {
            subContext = carryOverContext.getSubContext(SUBCONTEXT_NAME);
        }

        void setCurrentBox(Box box) {
            subContext.saveFeature(CURRENT_FOCUS_BOX, box);
        }

        void setCurrentLocationInBox(LocationInBox location) {
            subContext.saveFeature(CURRENT_FOCUS_LOCATION_IN_BOX, location);
        }

        Box getCurrentBox() {
            return (Box) subContext.getFeature(CURRENT_FOCUS_BOX);
        }

        LocationInBox getCurrentLocationInBox() {
            return (LocationInBox) subContext.getFeature(CURRENT_FOCUS_LOCATION_IN_BOX);
        }

        public CarryOverContext toCarryOverContext() {
            return subContext;
        }
    }
}
