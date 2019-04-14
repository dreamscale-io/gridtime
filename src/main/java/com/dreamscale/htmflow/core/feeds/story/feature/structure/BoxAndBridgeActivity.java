package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import java.util.ArrayList;
import java.util.List;

public class BoxAndBridgeActivity {

    private List<BoxActivity> boxActivities = new ArrayList<>();
    private List<BridgeActivity> bridgesCrossed = new ArrayList<>();


    public void addBoxActivity(BoxActivity boxActivity) {
        this.boxActivities.add(boxActivity);
    }

    public void addBridgeCrossed(BridgeActivity bridgeActivity) {

        this.bridgesCrossed.add(bridgeActivity);
    }

    public List<BoxActivity> getBoxActivities() {
        return boxActivities;
    }

    public List<BridgeActivity> getBridgesCrossed() {
        return bridgesCrossed;
    }

    public Box findBoxContaining(LocationInBox location) {
        Box boxFound = null;
        for (BoxActivity boxActivity : boxActivities) {
            if (boxActivity.contains(location)) {
                boxFound = boxActivity.getBox();
            }
        }
        return boxFound;
    }

    public ThoughtBubble findBubbleContaining(LocationInBox location) {
        ThoughtBubble bubbleFound = null;
        for (BoxActivity boxActivity : boxActivities) {
            for (ThoughtBubble bubble : boxActivity.getThoughtBubbles()) {
                if (bubble.contains(location)) {
                    bubbleFound = bubble;
                    break;
                }
            }
            if (bubbleFound != null) {
                break;
            }
        }
        return bubbleFound;
    }


}
