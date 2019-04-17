package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.feeds.story.StoryTile;

import java.util.UUID;

public interface SinkStrategy {

    void save(UUID memberId, StoryTile storyTile);

}
