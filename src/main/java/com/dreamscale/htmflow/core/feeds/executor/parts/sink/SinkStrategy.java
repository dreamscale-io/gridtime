package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.feeds.story.StoryFrame;

public interface SinkStrategy {

    void save(StoryFrame storyFrame);

}
