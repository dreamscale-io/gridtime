package com.dreamscale.htmflow.core.gridtime.machine.memory;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

import java.util.UUID;

public interface FeaturePool {

    void gotoPosition(GeometryClock.GridTime toCoordPosition);

    GridTile getActiveGridTile();

    String getActiveGridTime();

    void nextGridTile(GeometryClock.GridTime toCoordPosition);

    <T extends Flowable> Feed<T> registerFeed(UUID memberId, FetchStrategy<T> fetchStrategy);

    void resolveReferences();
}
