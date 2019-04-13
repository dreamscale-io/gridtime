package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObject;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObjectMetrics;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

@Getter
public class Box extends FlowFeature implements GridObject {

    private final String boxName;
    private List<ThoughtBubble> thoughtBubbles;

    private int relativeSequence;

    private GridObjectMetrics gridObjectMetrics = new GridObjectMetrics();


    public Box(String boxName) {
        this.boxName = boxName;
    }

    public void linkToBridge(Bridge bridgeBetweenBoxes, LocationInBox locationInBubble) {
        ThoughtBubble bubbleFound = findBubbleContainingLocation(locationInBubble);

        BridgeToBubble boxToBubble = new BridgeToBubble(bridgeBetweenBoxes, locationInBubble);
        bubbleFound.addBoxToBubbleLink(boxToBubble);

    }

    public ThoughtBubble findBubbleContainingLocation(LocationInBox locationInBubble) {
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


    public void setThoughtBubbles(List<ThoughtBubble> thoughtBubbles) {
        this.thoughtBubbles = thoughtBubbles;
    }

    @Override
    public GridObjectMetrics getGridObjectMetrics() {
        return gridObjectMetrics;
    }


    public void spendTime(Duration timeInLocation) {
        gridObjectMetrics.addVelocitySample(timeInLocation.getSeconds());
    }

    public void modify(int modificationCount) {
        gridObjectMetrics.addModificationSample(modificationCount);
    }
}
