package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveAcrossBridge;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveToNewLocationInBox;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveToNewBox;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SpatialGeometryMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;

    private Map<String, FocalPoint> boxMap = new HashMap<>();
    private Map<String, Bridge> bridgeMap = new HashMap<>();

    private List<FocalPoint> mainThoughtSequence = new ArrayList<>();
    private FocalPoint currentFocusBox;
    private BoxAndBridgeStructure extractedBoxAndBridgeStructure;

    public SpatialGeometryMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }

    public List<Movement> gotoLocation(LocalDateTime moment, String placeName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        if (currentFocusBox == null) {
            FocalPoint place = findOrCreateBox(placeName, locationPath);
            currentFocusBox = place;
            movements.add(new MoveToNewBox(moment, place));

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentFocusBox.getBoxName().equals(placeName)) {

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentFocusBox.getBoxName().equals(placeName)) {

            FocalPoint nextPlace = findOrCreateBox(placeName, locationPath);
            List<Movement> bridgeMovements = crossBridge(moment, currentFocusBox, nextPlace, locationPath, timeInLocation);
            movements.addAll(bridgeMovements);
        }

        return movements;
    }

    public void finish() {

        BoxAndBridgeStructure boxAndBridgeStructure = new BoxAndBridgeStructure();

        for (FocalPoint thought : mainThoughtSequence) {
            List<ThoughtBubble> thoughtBubbles = thought.getThoughtBubbles();
            boxAndBridgeStructure.createThoughtBox(thought.getBoxName(), thoughtBubbles);
        }

        for (Bridge bridgeBetweenBoxes : bridgeMap.values()) {
            boxAndBridgeStructure.createBridge(bridgeBetweenBoxes);
        }

        extractedBoxAndBridgeStructure = boxAndBridgeStructure;

    }

    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();
        subContext.setCurrentFocusBox(getCurrentFocusBox());
        subContext.setCurrentLocationInBox(getCurrentLocation());

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);
        FocalPoint box = subContext.getCurrentFocusBox();

        if (box != null) {
            currentFocusBox = new FocalPoint(box.getBoxName(), box.getCurrentLocation().getLocationPath());
        }
    }

    private Movement gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {
        LocationInBox location = currentFocusBox.goToLocation(locationPath, timeInLocation);
        return new MoveToNewLocationInBox(moment, location);
    }


    private List<Movement> crossBridge(LocalDateTime moment, FocalPoint fromBox, FocalPoint toBox, String toLocationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        LocationInBox fromLocation = fromBox.getCurrentLocation();
        LocationInBox exitLocation = fromBox.exit();

        movements.add(new MoveToNewLocationInBox(moment, exitLocation));

        LocationInBox enterLocation = toBox.enter();
        LocationInBox toLocation = toBox.goToLocation(toLocationPath, timeInLocation);
        Bridge bridgeCrossed = findOrCreateBridge(fromLocation, toLocation);
        bridgeCrossed.visit();

        movements.add(new MoveAcrossBridge(moment, bridgeCrossed));
        movements.add(new MoveToNewLocationInBox(moment, enterLocation));
        movements.add(new MoveToNewLocationInBox(moment, toLocation));

        currentFocusBox = toBox;

        return movements;
    }


    private FocalPoint findOrCreateBox(String boxName, String locationPath) {
        FocalPoint box = this.boxMap.get(boxName);
        if (box == null) {
            box = new FocalPoint(boxName, locationPath);
            this.boxMap.put(box.getBoxName(), box);
            this.mainThoughtSequence.add(box);
        }
        return box;
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

    public FocalPoint getCurrentFocusBox() {
        return currentFocusBox;
    }

    public LocationInBox getCurrentLocation() {
        LocationInBox location = null;
        if (currentFocusBox != null) {
            location = currentFocusBox.getCurrentLocation();
        }
        return location;
    }


    public void modifyCurrentLocation(int modificationCount) {
        if (currentFocusBox != null) {
            currentFocusBox.modifyCurrentLocationInFocus(modificationCount);
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

        void setCurrentFocusBox(FocalPoint box) {
            subContext.addKeyValue(CURRENT_FOCUS_BOX, box);
        }

        void setCurrentLocationInBox(LocationInBox location) {
            subContext.addKeyValue(CURRENT_FOCUS_LOCATION_IN_BOX, location);
        }

        FocalPoint getCurrentFocusBox() {
            return (FocalPoint) subContext.getValue(CURRENT_FOCUS_BOX);
        }

        LocationInBox getCurrentLocationInBox() {
            return (LocationInBox) subContext.getValue(CURRENT_FOCUS_LOCATION_IN_BOX);
        }

        public CarryOverContext toCarryOverContext() {
            return subContext;
        }
    }
}
