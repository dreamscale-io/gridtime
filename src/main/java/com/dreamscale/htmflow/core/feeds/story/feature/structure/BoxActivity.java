package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BoxActivity extends FlowFeature {

    private Box box;
    private List<ThoughtBubble> thoughtBubbles = new ArrayList<>();
    private int relativeSequence;

    public BoxActivity(Box box) {
        this.box = box;
    }

    public void addBubble(ThoughtBubble bubble) {
        this.thoughtBubbles.add(bubble);
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

    public void setRelativeSequence(int relativeSequence) {
        this.relativeSequence = relativeSequence;
    }


}
