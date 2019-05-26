package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.feeds.story.TileBuilder;

import java.util.UUID;

public interface SinkStrategy {

    void save(UUID torchieId, TileBuilder tileBuilder);

}
