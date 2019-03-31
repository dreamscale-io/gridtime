package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BoxAndBridgeStructure {

    private List<BoxStructure> boxes = new ArrayList<>();

    public void createThoughtBox(String placeName, List<RadialStructure> thoughtBubbles) {

        BoxStructure box = new BoxStructure(placeName, thoughtBubbles);
        boxes.add(box);

    }

    public void createBridge(BridgeStructure bridgeBetweenBoxes) {
       // bridgeBetweenBoxes.


    }

    public void finish() {

    }



}
