package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.feeds.story.StoryTile;

public interface SinkStrategy {

    void save(StoryTile storyTile);

}
