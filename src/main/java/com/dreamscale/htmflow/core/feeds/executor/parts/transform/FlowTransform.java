package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;

public interface FlowTransform {

    void transform(StoryTile storyTile);
}
