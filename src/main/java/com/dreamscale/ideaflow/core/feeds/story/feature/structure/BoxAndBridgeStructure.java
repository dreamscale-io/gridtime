package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import java.util.ArrayList;
import java.util.List;

public class BoxAndBridgeStructure {

    private int relativeBoxSequence = 1;
    private int relativeBridgeSequence = 1;
    private List<Box> boxes = new ArrayList<>();
    private List<Bridge> bridges = new ArrayList<>();

    public void createThoughtBox(String placeName, List<ThoughtBubble> thoughtBubbles) {

        Box box = new Box(placeName, thoughtBubbles);
        box.setRelativeSequence(relativeBoxSequence);
        boxes.add(box);

        relativeBoxSequence++;
    }

    public void createBridge(Bridge bridgeBetweenBoxes) {

        LocationInFocus fromLocation = bridgeBetweenBoxes.getFromLocation();
        LocationInFocus toLocation = bridgeBetweenBoxes.getToLocation();

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

    private void validateBoxFound(Box boxWithFromLocation, LocationInFocus location) {
        if (boxWithFromLocation == null) {
            throw new RuntimeException("Logic error, box not found for location: "+location);
        }
    }

    private Box findBoxContaining(LocationInFocus location) {
        Box boxFound = null;
        for (Box box : boxes) {
            if (box.contains(location)) {
                boxFound = box;
            }
        }
        return boxFound;
    }



}
