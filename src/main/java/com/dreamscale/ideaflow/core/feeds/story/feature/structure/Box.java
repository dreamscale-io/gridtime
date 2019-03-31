package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

import java.util.ArrayList;
import java.util.List;

public class Box implements IdeaFlowFeature {

    private final String placeName;
    private final List<ThoughtBubble> thoughtBubbles;

    private final List<BoxToBubbleLink> boxToBubbleLinks = new ArrayList<>();
    private int relativeSequence;

    public Box(String placeName, List<ThoughtBubble> thoughtBubbles) {
        this.placeName = placeName;
        this.thoughtBubbles = thoughtBubbles;

        int sequence = 1;
        for (ThoughtBubble bubble : thoughtBubbles) {
            bubble.setRelativeSequence(sequence);
            sequence++;
        }
    }

    public void linkToBridge(Bridge bridgeBetweenBoxes, LocationInFocus locationInBubble) {
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

    private ThoughtBubble findBubbleContainingLocation(LocationInFocus locationInBubble) {
        ThoughtBubble bubbleFound = null;

        for (ThoughtBubble bubble : thoughtBubbles) {
            if (bubble.contains(locationInBubble)) {
                bubbleFound = bubble;
            }
        }

        return bubbleFound;
    }

    public boolean contains(LocationInFocus location) {
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

    public int getRelativeSequence() {
        return this.relativeSequence;
    }
}
