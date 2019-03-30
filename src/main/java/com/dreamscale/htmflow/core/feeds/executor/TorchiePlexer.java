package com.dreamscale.htmflow.core.feeds.executor;

import com.dreamscale.htmflow.core.feeds.common.ZoomableIdeaFlow;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;

import java.util.UUID;

public class TorchiePlexer {

    private final UUID memberId;
    private final ZoomableIdeaFlow zoomableIdeaFlow;

    public TorchiePlexer(UUID memberId, ZoomableIdeaFlow zoomableIdeaFlow) {
        this.memberId = memberId;
        this.zoomableIdeaFlow = zoomableIdeaFlow;
    }


    public UUID getMemberId() {
        return memberId;
    }

    /**
     * One tick of progress, generates the next bit of work to do, when there's no more work.
     */
    public void tick() {
        this.zoomableIdeaFlow.tick();
    }

    public Runnable whatsNext() {
        return this.zoomableIdeaFlow.whatsNext();
    }

    public StoryFrame whereAmI() {
        return this.zoomableIdeaFlow.getActiveStoryFrame();
    }

    public StoryFrame zoomIn() {
        return this.zoomableIdeaFlow.zoomIn();
    }

    public StoryFrame zoomOut() {
        return this.zoomableIdeaFlow.zoomOut();
    }

    public StoryFrame panLeft() {
        return this.zoomableIdeaFlow.panLeft();
    }

    public StoryFrame panRight() {
        return this.zoomableIdeaFlow.panRight();
    }

    public void wrapUpAndBookmark() {
        this.zoomableIdeaFlow.wrapUpAndBookmark();
    }

}
