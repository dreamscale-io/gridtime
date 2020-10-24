package com.dreamscale.gridtime.core.machine.memory;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class AbstractTorchieState implements TorchieState {

    private final UUID organizationId;
    private final UUID torchieId;
    private List<UUID> teamIds;
    private final FeatureCache featureCache;

    private Map<FeedStrategyFactory.FeedType, InputFeed> inputFeeds = DefaultCollections.map();

    private GeometryClock.GridTime gridTime;

    private BoxResolver boxResolver;
    private GridTile activeGridTile;


    public AbstractTorchieState(UUID organizationId, UUID torchieId, List<UUID> teamIds, BoxResolver boxResolver, FeatureCache featureCache) {
        this.organizationId = organizationId;
        this.torchieId = torchieId;
        this.teamIds = teamIds;
        this.boxResolver = boxResolver;
        this.featureCache = featureCache;
    }

    @Override
    public UUID getTorchieId() {
        return torchieId;
    }

    @Override
    public UUID getOrganizationId() {
        return organizationId;
    }

    @Override
    public List<UUID> getTeamIds() { return teamIds; }

    public GridTile getActiveTile() {
        return activeGridTile;
    }

    public FeatureCache getFeatureCache() {
        return featureCache;
    }

    @Override
    public String getActiveTime() {
        return gridTime.toDisplayString();
    }


    @Override
    public void changeBoxConfiguration(BoxResolver boxResolver) {
        this.boxResolver = boxResolver;
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

        this.activeGridTile = new GridTile(torchieId, this.gridTime, featureCache, boxResolver);

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
