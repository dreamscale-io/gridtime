package com.dreamscale.htmflow.core.gridtime.executor.memory;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.FeedType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.FeatureSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.TileSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;

import java.util.Map;
import java.util.UUID;

public class PerProcessFeaturePool implements FeaturePool {

    private final UUID teamId;
    private final UUID torchieId;
    private final FeatureCache featureCache;

    private Map<FeedType, Feed> sourceFeeds = DefaultCollections.map();

    private final FeatureSearchService featureSearchService;
    private final TileSearchService tileSearchService;

    private GeometryClock.Coords activeGridCoords;

    private GridTile activeGridTile;

    public PerProcessFeaturePool(UUID teamId, UUID torchieId, FeatureCache featureCache,
                                 FeatureSearchService featureSearchService, TileSearchService tileSearchService) {
        this.teamId = teamId;
        this.torchieId = torchieId;
        this.featureCache = featureCache;
        this.featureSearchService = featureSearchService;
        this.tileSearchService = tileSearchService;
    }

    public void registerFeed(FeedType feedType, Feed feed) {
        sourceFeeds.put(feedType, feed);
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
        this.activeGridTile = null;
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

    private void validateCoordsMatchAndResetTileIfNeeded(GeometryClock.Coords toCoordPosition, GeometryClock.Coords nextCoords) {
        if (!nextCoords.equals(toCoordPosition)) {
            activeGridCoords = toCoordPosition;
            activeGridTile = null;
        }
    }

    public FeatureReference resolve(FeatureReference originalReference) {
        return featureSearchService.resolve(teamId, originalReference);
    }

    private CarryOverContext getCarryOverContextFromTile(GeometryClock.Coords coords) {
        return tileSearchService.getCarryOverContextOfTile(torchieId, coords);
    }




}
