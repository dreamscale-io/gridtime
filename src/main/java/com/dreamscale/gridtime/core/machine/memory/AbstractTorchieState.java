package com.dreamscale.gridtime.core.machine.memory;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class AbstractTorchieState implements TorchieState {

    private final UUID torchieId;
    private final FeatureCache featureCache;
    private final UUID teamId;

    private Map<FeedStrategyFactory.FeedType, InputFeed> inputFeeds = DefaultCollections.map();

    private GeometryClock.GridTime gridTime;

    private GridTile activeGridTile;

    public AbstractTorchieState(UUID torchieId, UUID teamId, FeatureCache featureCache) {
        this.torchieId = torchieId;
        this.teamId = teamId;
        this.featureCache = featureCache;
    }

    @Override
    public UUID getTorchieId() {
        return torchieId;
    }

    @Override
    public UUID getTeamId() {
        return teamId;
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
        log.debug("gotoTilePosition: " + toGridPosition.toDisplayString());

        this.gridTime = toGridPosition;

        CarryOverContext carryOverContext = getCarryOverContext(this.gridTime.panLeft());

        this.activeGridTile = new GridTile(torchieId, this.gridTime, featureCache);

        activeGridTile.initFromCarryOverContext(carryOverContext);
    }

    private CarryOverContext getCarryOverContext(GeometryClock.GridTime panLeftGridTime) {
        if (activeGridTile != null && activeGridTile.getGridTime().equals(panLeftGridTime)) {
            return activeGridTile.getCarryOverContext();
        } else {
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
