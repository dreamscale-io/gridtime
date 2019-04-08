package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import java.util.ArrayList;
import java.util.List;

public class BoxAndBridgeStructure {

    private int relativeBoxSequence = 1;
    private int relativeBridgeSequence = 1;
    private List<Box> boxes = new ArrayList<>();
    private List<Bridge> bridges = new ArrayList<>();

    public void addBoxOfThoughts(Box box) {

        box.setRelativeSequence(relativeBoxSequence);
        boxes.add(box);

        relativeBoxSequence++;
    }

    public void createBridge(Bridge bridgeBetweenBoxes) {

        LocationInBox fromLocation = bridgeBetweenBoxes.getFromLocation();
        LocationInBox toLocation = bridgeBetweenBoxes.getToLocation();

        Box boxWithFromLocation = findBoxContaining(fromLocation);
        Box boxWithToLocation = findBoxContaining(toLocation);

        validateBoxFound(boxWithFromLocation, fromLocation);
        validateBoxFound(boxWithToLocation, toLocation);

        bridgeBetweenBoxes.setFromBox(boxWithFromLocation);
        bridgeBetweenBoxes.setToBox(boxWithToLocation);

        boxWithFromLocation.linkToBridge(bridgeBetweenBoxes, fromLocation);
        boxWithToLocation.linkToBridge(bridgeBetweenBoxes, toLocation);

        bridgeBetweenBoxes.setRelativeSequence(relativeBridgeSequence);
        bridges.add(bridgeBetweenBoxes);

        relativeBridgeSequence++;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public List<Bridge> getBridges() {
        return bridges;
    }

    private void validateBoxFound(Box boxWithFromLocation, LocationInBox location) {
        if (boxWithFromLocation == null) {
            throw new RuntimeException("Logic error, box not found for location: "+location);
        }
    }

    private Box findBoxContaining(LocationInBox location) {
        Box boxFound = null;
        for (Box box : boxes) {
            if (box.contains(location)) {
                boxFound = box;
            }
        }
        return boxFound;
    }

}
