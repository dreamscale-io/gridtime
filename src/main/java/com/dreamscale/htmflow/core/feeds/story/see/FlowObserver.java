package com.dreamscale.htmflow.core.feeds.story.see;

import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.Window;

public interface FlowObserver {

    void see(StoryFrame storyFrame, Window window);
}
