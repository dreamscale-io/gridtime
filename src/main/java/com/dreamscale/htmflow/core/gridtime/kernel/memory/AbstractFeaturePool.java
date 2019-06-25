package com.dreamscale.htmflow.core.gridtime.kernel.memory;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.CarryOverContext;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractFeaturePool implements FeaturePool {

    private final UUID torchieId;
    private final FeatureCache featureCache;

    private Map<String, Feed> sourceFeeds = DefaultCollections.map();

    private GeometryClock.GridTime activeGridGridTime;

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
        return activeGridGridTime.getFormattedGridTime();
    }

    public void gotoPosition(GeometryClock.GridTime gridTime) {
        this.activeGridGridTime = gridTime;
        this.activeGridTile = new GridTile(torchieId, activeGridGridTime, featureCache);

        CarryOverContext carryOverContext = getCarryOverContextFromTile(activeGridGridTime.panLeft());
        activeGridTile.initFromCarryOverContext(carryOverContext);
    }

    @Override
    public void nextGridTile(GeometryClock.GridTime toCoordPosition) {
        GeometryClock.GridTime nextGridTime = activeGridGridTime.panRight();

        validateCoordsMatchAndResetTileIfNeeded(toCoordPosition, nextGridTime);

        if (activeGridTile == null) {
            CarryOverContext carryOverContext = getCarryOverContextFromTile(activeGridGridTime.panLeft());

            activeGridTile = new GridTile(torchieId, activeGridGridTime, featureCache);
            activeGridTile.initFromCarryOverContext(carryOverContext);
        } else {

            GridTile nextGridTile = new GridTile(torchieId, nextGridTime, featureCache);

            nextGridTile.initFromCarryOverContext(activeGridTile.getCarryOverContext());

            activeGridTile = nextGridTile;
            activeGridGridTime = nextGridTime;
        }
    }

    public <T extends Flowable> Feed<T> registerFeed(UUID memberId, FetchStrategy<T> fetchStrategy) {
        String name = fetchStrategy.getClass().getSimpleName();
        Feed<T> feed = new Feed<>(name, memberId, fetchStrategy);
        sourceFeeds.put(name, feed);

        return feed;
    }

    private void validateCoordsMatchAndResetTileIfNeeded(GeometryClock.GridTime toCoordPosition, GeometryClock.GridTime nextGridTime) {
        if (!nextGridTime.equals(toCoordPosition)) {
            activeGridGridTime = toCoordPosition;
            activeGridTile = null;
        }
    }

    @Override
    public abstract void resolveReferences();

    protected abstract CarryOverContext getCarryOverContextFromTile(GeometryClock.GridTime gridTime);

}
