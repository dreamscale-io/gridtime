package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;

public interface FlowObserver {

    void see(StoryFrame storyFrame, Window window);
}
