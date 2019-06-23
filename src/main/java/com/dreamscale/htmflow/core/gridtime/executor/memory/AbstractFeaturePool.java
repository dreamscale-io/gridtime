package com.dreamscale.htmflow.core.gridtime.executor.memory;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractFeaturePool implements FeaturePool {

    private final UUID torchieId;
    private final FeatureCache featureCache;

    private Map<String, Feed> sourceFeeds = DefaultCollections.map();

    private GeometryClock.Coords activeGridCoords;

    private GridTile activeGridTile;

    public AbstractFeaturePool(UUID torchieId, FeatureCache featureCache) {
        this.torchieId = torchieId;
        this.featureCache = featureCache;
    }

    public GridTile getActiveGridTile() {
        return activeGridTile;
    }

    @Override
    public String getActiveGridTime() {
        return activeGridCoords.getFormattedGridTime();
    }

    public void gotoGridTile(GeometryClock.Coords coords) {
        this.activeGridCoords = coords;
        this.activeGridTile = new GridTile(torchieId, activeGridCoords, featureCache);

        CarryOverContext carryOverContext = getCarryOverContextFromTile(activeGridCoords.panLeft());
        activeGridTile.initFromCarryOverContext(carryOverContext);
    }

    @Override
    public void nextGridTile(GeometryClock.Coords toCoordPosition) {
        GeometryClock.Coords nextCoords = activeGridCoords.panRight();

        validateCoordsMatchAndResetTileIfNeeded(toCoordPosition, nextCoords);

        if (activeGridTile == null) {
            CarryOverContext carryOverContext = getCarryOverContextFromTile(activeGridCoords.panLeft());

            activeGridTile = new GridTile(torchieId, activeGridCoords, featureCache);
            activeGridTile.initFromCarryOverContext(carryOverContext);
        } else {

            GridTile nextGridTile = new GridTile(torchieId, nextCoords, featureCache);

            nextGridTile.initFromCarryOverContext(activeGridTile.getCarryOverContext());

            activeGridTile = nextGridTile;
            activeGridCoords = nextCoords;
        }
    }

    public <T extends Flowable> Feed<T> registerFeed(UUID memberId, FetchStrategy<T> fetchStrategy) {
        String name = fetchStrategy.getClass().getSimpleName();
        Feed<T> feed = new Feed<>(name, memberId, fetchStrategy);
        sourceFeeds.put(name, feed);

        return feed;
    }

    private void validateCoordsMatchAndResetTileIfNeeded(GeometryClock.Coords toCoordPosition, GeometryClock.Coords nextCoords) {
        if (!nextCoords.equals(toCoordPosition)) {
            activeGridCoords = toCoordPosition;
            activeGridTile = null;
        }
    }

    @Override
    public abstract void resolveReferences();

    protected abstract CarryOverContext getCarryOverContextFromTile(GeometryClock.Coords coords);

}
