package com.dreamscale.htmflow.core.gridtime.executor.memory;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Feed;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.FeedType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.FeatureSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.TileSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;

import java.util.Map;
import java.util.UUID;

public class InMemoryFeaturePool implements FeaturePool {

    private final UUID torchieId;
    private final FeatureCache featureCache;

    private GeometryClock.Coords activeGridCoords;

    private GridTile activeGridTile;

    public InMemoryFeaturePool(UUID torchieId) {
        this.torchieId = torchieId;
        this.featureCache = new FeatureCache();
    }

    @Override
    public GridTile getActiveGridTile() {
        return activeGridTile;
    }

    @Override
    public String getActiveGridTime() {
        return activeGridCoords.getFormattedGridTime();
    }

    @Override
    public void gotoGridTile(GeometryClock.Coords coords) {
        this.activeGridCoords = coords;
        this.activeGridTile = new GridTile(torchieId, activeGridCoords, featureCache);
    }

    public void nextGridTile(GeometryClock.Coords toCoordPosition) {
        GeometryClock.Coords nextCoords = activeGridCoords.panRight();

        validateCoordsMatchAndResetTileIfNeeded(toCoordPosition, nextCoords);

        if (activeGridTile == null) {
            activeGridTile = new GridTile(torchieId, activeGridCoords, featureCache);
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


}
