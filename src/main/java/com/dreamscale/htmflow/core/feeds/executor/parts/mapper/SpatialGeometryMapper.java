package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveToBox;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.grid.TileGrid;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SpatialGeometryMapper {

    private final FeatureFactory featureFactory;
    private final TileGrid tileGrid;

    private Map<String, FocalPoint> focalPointMap = new HashMap<>();
    private List<FocalPoint> orderedThoughtSequence = new ArrayList<>();

    private FocalPoint currentFocus;
    private BoxAndBridgeActivity extractedSpatialStructuredActivity;
    private Bridge recentBridgeCrossed;

    public SpatialGeometryMapper(FeatureFactory featureFactory, TileGrid tileGrid) {
        this.featureFactory = featureFactory;
        this.tileGrid = tileGrid;
    }

    public List<Movement> gotoLocation(LocalDateTime moment,
                                       String boxName, String locationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();
        resetRecentBridgeCrossed();

        if (currentFocus == null) {
            FocalPoint placeInBox = findOrCreateFocalPoint(boxName);
            currentFocus = placeInBox;
            movements.add(new MoveToBox(moment, placeInBox.getBox()));

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (currentFocus.getBoxName().equals(boxName)) {

            Movement movement = gotoLocationAndCreateMovement(moment, locationPath, timeInLocation);
            movements.add(movement);

        } else if (!currentFocus.getBoxName().equals(boxName)) {

            FocalPoint otherFocus = findOrCreateFocalPoint(boxName);
            List<Movement> bridgeMovements = crossBridge(moment, currentFocus, otherFocus, locationPath, timeInLocation);
            movements.addAll(bridgeMovements);
        }

        return movements;
    }

    private void resetRecentBridgeCrossed() {
        recentBridgeCrossed = null;
    }

    public FocalPoint findOrCreateFocalPoint(String boxName) {
        FocalPoint focalPoint = this.focalPointMap.get(boxName);
        if (focalPoint == null) {
            Box box = featureFactory.findOrCreateBox(boxName);

            focalPoint = new FocalPoint(featureFactory, tileGrid, box);
            this.focalPointMap.put(focalPoint.getBoxName(), focalPoint);
            this.orderedThoughtSequence.add(focalPoint);
        }
        return focalPoint;
    }

    public void finish() {

        BoxAndBridgeActivity boxAndBridgeActivity = new BoxAndBridgeActivity();

        for (FocalPoint thought : orderedThoughtSequence) {

            BoxActivity boxActivity = thought.createBoxOfThoughtBubbles();
            boxAndBridgeActivity.addBoxActivity(boxActivity);

            for (ThoughtBubble bubble : boxActivity.getThoughtBubbles()) {
                tileGrid.createAggregateRow(bubble, bubble.getAllLocations());
            }
        }

        for (Bridge bridgeBetweenBoxes : featureFactory.getAllBridges()) {

            BridgeActivity bridgeActivity = featureFactory.createBridgeActivity(bridgeBetweenBoxes);
            boxAndBridgeActivity.addBridgeCrossed(bridgeActivity);
        }

        extractedSpatialStructuredActivity = boxAndBridgeActivity;

    }

    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();
        subContext.setCurrentBox(getCurrentBox());
        subContext.setCurrentLocationInBox(getCurrentLocation());

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);
        Box box = subContext.getCurrentBox();
        LocationInBox lastLocationInBox = subContext.getCurrentLocationInBox();

        if (box != null) {
            currentFocus = findOrCreateFocalPoint(box.getBoxName());
            currentFocus.initLocation(lastLocationInBox);
        }
    }

    private Movement gotoLocationAndCreateMovement(LocalDateTime moment, String locationPath, Duration timeInLocation) {

        LocationInBox location = currentFocus.goToLocation(locationPath, timeInLocation);

        return featureFactory.createMoveToLocation(moment, location, currentFocus.getLastTraversal());
    }


    private List<Movement> crossBridge(LocalDateTime moment, FocalPoint fromBox, FocalPoint toBox, String toLocationPath, Duration timeInLocation) {

        List<Movement> movements = new ArrayList<>();

        LocationInBox fromLocation = fromBox.getCurrentLocation();
        LocationInBox exitLocation = fromBox.exit();

        movements.add(featureFactory.createMoveToLocation(moment, exitLocation, fromBox.getLastTraversal()));

        LocationInBox enterLocation = toBox.enter();
        LocationInBox toLocation = toBox.goToLocation(toLocationPath, timeInLocation);
        Bridge bridgeCrossed = featureFactory.findOrCreateBridge(fromLocation, toLocation);

        recentBridgeCrossed = bridgeCrossed;

        movements.add(featureFactory.createMoveAcrossBridge(moment, bridgeCrossed));
        movements.add(featureFactory.createMoveToBox(moment, toBox.getBox()));
        movements.add(featureFactory.createMoveToLocation(moment, enterLocation, toBox.getLastTraversal()));
        movements.add(featureFactory.createMoveToLocation(moment, toLocation, toBox.getLastTraversal()));

        currentFocus = toBox;

        return movements;
    }


    public BoxAndBridgeActivity getSpatialStructuredActivity() {
        if (extractedSpatialStructuredActivity == null) {
            finish();
        }
       return extractedSpatialStructuredActivity;
    }

    public FocalPoint getCurrentFocus() {
        return currentFocus;
    }

    public Box getCurrentBox() {
        Box box = null;
        if (currentFocus != null) {
            box = currentFocus.getBox();
        }
        return box;
    }

    public LocationInBox getCurrentLocation() {
        LocationInBox location = null;
        if (currentFocus != null) {
            location = currentFocus.getCurrentLocation();
        }
        return location;
    }

    public Traversal getLastTraversal() {
        Traversal traversal = null;
        if (currentFocus != null) {
            traversal = currentFocus.getLastTraversal();
        }
        return traversal;
    }

    public Bridge getRecentBridgeCrossed() {
        return recentBridgeCrossed;
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
