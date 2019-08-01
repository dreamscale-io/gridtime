package com.dreamscale.htmflow.core.gridtime.machine.memory;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.InputFeed;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class AbstractTorchieState implements TorchieState {

    private final UUID torchieId;
    private final FeatureCache featureCache;

    private Map<FeedStrategyFactory.FeedType, InputFeed> inputFeeds = DefaultCollections.map();

    private GeometryClock.GridTime gridTime;

    private GridTile activeGridTile;

    public AbstractTorchieState(UUID torchieId, FeatureCache featureCache) {
        this.torchieId = torchieId;
        this.featureCache = featureCache;
    }

    public GridTile getActiveTile() {
        return activeGridTile;
    }

    @Override
    public String getActiveTime() {
        return gridTime.toDisplayString();
    }


    public void nextTile() {
        if (activeGridTile != null) {
            gotoPosition(activeGridTile.getGridTime().panRight());
        }
    }

    public void gotoPosition(GeometryClock.GridTime toGridPosition) {
        log.debug("gotoTilePosition: "+ toGridPosition.toDisplayString());

        this.gridTime = toGridPosition;

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

    @Override
    public <T extends Flowable> InputFeed<T> getInputFeed(FeedStrategyFactory.FeedType type) {
        return inputFeeds.get(type);
    }

    public <T extends Flowable> InputFeed<T> registerInputFeed(UUID memberId, FeedStrategyFactory.FeedType feedType, FeedStrategy<T> feedStrategy) {
        InputFeed<T> inputFeed = new InputFeed<T>(feedType, memberId, feedStrategy);
        inputFeeds.put(feedType, inputFeed);

        return inputFeed;
    }

    private void validateCoordsMatchAndResetTileIfNeeded(GeometryClock.GridTime toCoordPosition, GeometryClock.GridTime nextGridTime) {
        if (!nextGridTime.equals(toCoordPosition)) {
            gridTime = toCoordPosition;
            activeGridTile = null;
        }
    }

    @Override
    public abstract void resolveFeatureReferences();

    protected abstract CarryOverContext getCarryOverContextFromTileDB(GeometryClock.GridTime gridTime);

}
