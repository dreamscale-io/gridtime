package com.dreamscale.htmflow.core.feeds.executor;

import com.dreamscale.htmflow.core.feeds.common.ZoomableFlow;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;

import java.util.UUID;

public class Torchie {

    private final UUID memberId;
    private final ZoomableFlow zoomableFlow;

    public Torchie(UUID memberId, ZoomableFlow zoomableFlow) {
        this.memberId = memberId;
        this.zoomableFlow = zoomableFlow;
    }


    public UUID getMemberId() {
        return memberId;
    }

    /**
     * One tick of progress, generates the next bit of work to do, when there's no more work.
     */
    public void tick() {
        this.zoomableFlow.tick();
    }

    public Runnable whatsNext() {
        return this.zoomableFlow.whatsNext();
    }

    public StoryTile whereAmI() {
        return this.zoomableFlow.getActiveStoryFrame();
    }

    public StoryTile zoomIn() {
        return this.zoomableFlow.zoomIn();
    }

    public StoryTile zoomOut() {
        return this.zoomableFlow.zoomOut();
    }

    public StoryTile panLeft() {
        return this.zoomableFlow.panLeft();
    }

    public StoryTile panRight() {
        return this.zoomableFlow.panRight();
    }

    public void wrapUpAndBookmark() {
        this.zoomableFlow.wrapUpAndBookmark();
    }

}
