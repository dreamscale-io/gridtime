package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import java.util.ArrayList;
import java.util.List;

public class BoxAndBridgeStructure {

    private int relativeBoxSequence = 1;
    private int relativeBridgeSequence = 1;
    private List<BoxStructure> boxes = new ArrayList<>();
    private List<BridgeStructure> bridges = new ArrayList<>();

    public void createThoughtBox(String placeName, List<ThoughtBubble> thoughtBubbles) {

        BoxStructure box = new BoxStructure(placeName, thoughtBubbles);
        box.setRelativeSequence(relativeBoxSequence);
        boxes.add(box);

        relativeBoxSequence++;
    }

    public void createBridge(BridgeStructure bridgeBetweenBoxes) {

        LocationInFocus fromLocation = bridgeBetweenBoxes.getFromLocation();
        LocationInFocus toLocation = bridgeBetweenBoxes.getToLocation();

        BoxStructure boxWithFromLocation = findBoxContaining(fromLocation);
        BoxStructure boxWithToLocation = findBoxContaining(toLocation);

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

    private void validateBoxFound(BoxStructure boxWithFromLocation, LocationInFocus location) {
        if (boxWithFromLocation == null) {
            throw new RuntimeException("Logic error, box not found for location: "+location);
        }
    }

    private BoxStructure findBoxContaining(LocationInFocus location) {
        BoxStructure boxFound = null;
        for (BoxStructure box : boxes) {
            if (box.contains(location)) {
                boxFound = box;
            }
        }
        return boxFound;
    }



}
