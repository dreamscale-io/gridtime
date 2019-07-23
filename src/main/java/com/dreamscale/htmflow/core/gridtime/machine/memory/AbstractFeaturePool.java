package com.dreamscale.htmflow.core.gridtime.machine.memory;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class AbstractFeaturePool implements FeaturePool {

    private final UUID torchieId;
    private final FeatureCache featureCache;

    private Map<String, Feed> sourceFeeds = DefaultCollections.map();

    private GeometryClock.GridTime gridTime;

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
        return gridTime.toDisplayString();
    }

    public void nextGridTile() {
        if (activeGridTile != null) {
            gotoTilePosition(activeGridTile.getGridTime().panRight());
        }
    }

    public void gotoTilePosition(GeometryClock.GridTime gridTime) {
        log.debug("gotoTilePosition: "+gridTime.toDisplayString());

        this.gridTime = gridTime;

        CarryOverContext carryOverContext = getCarryOverContext(this.gridTime.panLeft());

        this.activeGridTile = new GridTile(torchieId, this.gridTime, featureCache);

        activeGridTile.initFromCarryOverContext(carryOverContext);
    }

    private CarryOverContext getCarryOverContext(GeometryClock.GridTime panLeftGridTime) {
        if (activeGridTile != null && activeGridTile.getGridTime().equals(panLeftGridTime)) {
            return activeGridTile.getCarryOverContext();
        }
        else {
            return getCarryOverContextFromTileDB(gridTime);
        }
    }


    public <T extends Flowable> Feed<T> registerFeed(UUID memberId, FeedStrategy<T> feedStrategy) {
        String name = feedStrategy.getClass().getSimpleName();
        Feed<T> feed = new Feed<>(name, memberId, feedStrategy);
        sourceFeeds.put(name, feed);

        return feed;
    }

    private void validateCoordsMatchAndResetTileIfNeeded(GeometryClock.GridTime toCoordPosition, GeometryClock.GridTime nextGridTime) {
        if (!nextGridTime.equals(toCoordPosition)) {
            gridTime = toCoordPosition;
            activeGridTile = null;
        }
    }

    @Override
    public abstract void resolveReferences();

    protected abstract CarryOverContext getCarryOverContextFromTileDB(GeometryClock.GridTime gridTime);

}
