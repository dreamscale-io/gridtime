package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.List;

@Getter
public class Box extends FlowFeature {

    private final String boxName;
    private final List<ThoughtBubble> thoughtBubbles;


    private int relativeSequence;

    public Box(String boxName, List<ThoughtBubble> thoughtBubbles) {
        this.boxName = boxName;
        this.thoughtBubbles = thoughtBubbles;

        int sequence = 1;
        for (ThoughtBubble bubble : thoughtBubbles) {
            bubble.setRelativeSequence(sequence);
            sequence++;
        }
    }

    public void linkToBridge(Bridge bridgeBetweenBoxes, LocationInBox locationInBubble) {
        ThoughtBubble bubbleFound = findBubbleContainingLocation(locationInBubble);

        BridgeToBubbleLink boxToBubble = new BridgeToBubbleLink(bridgeBetweenBoxes, locationInBubble);
        bubbleFound.addBoxToBubbleLink(boxToBubble);

    }

    private ThoughtBubble findBubbleContainingLocation(LocationInBox locationInBubble) {
        ThoughtBubble bubbleFound = null;

        for (ThoughtBubble bubble : thoughtBubbles) {
            if (bubble.contains(locationInBubble)) {
                bubbleFound = bubble;
            }
        }

        return bubbleFound;
    }

    public boolean contains(LocationInBox location) {
        ThoughtBubble bubbleFound = findBubbleContainingLocation(location);

        if (bubbleFound != null) {
            return true;
        } else {
            return false;
        }
    }

    public void setRelativeSequence(int relativeBoxSequence) {
        this.relativeSequence = relativeBoxSequence;
    }


}
