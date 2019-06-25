package com.dreamscale.htmflow.core.gridtime.kernel.memory;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;

import java.util.UUID;

public interface FeaturePool {

    void gotoPosition(GeometryClock.GridTime toCoordPosition);

    GridTile getActiveGridTile();

    String getActiveGridTime();

    void nextGridTile(GeometryClock.GridTime toCoordPosition);

    <T extends Flowable> Feed<T> registerFeed(UUID memberId, FetchStrategy<T> fetchStrategy);

    void resolveReferences();
}
