package com.dreamscale.htmflow.core.gridtime.machine.memory;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

import java.util.UUID;

public interface FeaturePool {

    void gotoTilePosition(GeometryClock.GridTime toCoordPosition);

    GridTile getActiveGridTile();

    String getActiveGridTime();

    <T extends Flowable> Feed<T> registerFeed(UUID memberId, FeedStrategy<T> feedStrategy);

    void resolveReferences();

    void nextGridTile();
}
