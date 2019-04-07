package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Box extends FlowFeature {

    private final String boxName;
    private final List<ThoughtBubble> thoughtBubbles;

    private final List<BoxToBubbleLink> boxToBubbleLinks = new ArrayList<>();
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

        BoxToBubbleLink boxToBubble = findOrCreateBoxToBubbleLink(bubbleFound);
        boxToBubble.addBridgeToSet(bridgeBetweenBoxes);
        boxToBubble.addConnectedLocationToSet(locationInBubble);
    }

    private BoxToBubbleLink findOrCreateBoxToBubbleLink(ThoughtBubble bubble) {
        BoxToBubbleLink boxToBubbleLinkFound = null;

        for (BoxToBubbleLink bubbleLink : boxToBubbleLinks) {
            if (bubbleLink.connectedTo(bubble)) {
                boxToBubbleLinkFound = bubbleLink;
                break;
            }
        }

        if (boxToBubbleLinkFound == null) {
            boxToBubbleLinkFound = new BoxToBubbleLink(bubble);
        }

        return boxToBubbleLinkFound;
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
