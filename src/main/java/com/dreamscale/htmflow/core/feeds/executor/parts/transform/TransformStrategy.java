package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.story.StoryTile;

public interface TransformStrategy {

    void transform(StoryTile storyTile);
}
