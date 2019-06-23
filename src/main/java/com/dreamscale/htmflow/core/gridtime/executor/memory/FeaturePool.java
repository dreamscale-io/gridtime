package com.dreamscale.htmflow.core.gridtime.executor.memory;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.FeatureSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.TileSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;

import java.util.UUID;

public interface FeaturePool {

    void gotoGridTile(GeometryClock.Coords toCoordPosition);

    GridTile getActiveGridTile();

    String getActiveGridTime();

    void nextGridTile(GeometryClock.Coords toCoordPosition);

    <T extends Flowable> Feed<T> registerFeed(UUID memberId, FetchStrategy<T> fetchStrategy);

    void resolveReferences();
}
