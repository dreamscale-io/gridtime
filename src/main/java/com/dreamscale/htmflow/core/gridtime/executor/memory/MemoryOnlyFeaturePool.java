package com.dreamscale.htmflow.core.gridtime.executor.memory;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.CarryOverContext;

import java.util.UUID;

public class MemoryOnlyFeaturePool extends AbstractFeaturePool {


    public MemoryOnlyFeaturePool(UUID torchieId) {
        super(torchieId, new FeatureCache());
    }

    @Override
    FeatureReference resolve(FeatureReference originalReference) {
        return originalReference;
    }

    @Override
    CarryOverContext getCarryOverContextFromTile(GeometryClock.Coords coords) {
        return null;
    }


}
