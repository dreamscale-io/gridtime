package com.dreamscale.htmflow.core.gridtime.machine.memory;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;

import java.util.UUID;

public class MemoryOnlyFeaturePool extends AbstractFeaturePool {


    public MemoryOnlyFeaturePool(UUID torchieId) {
        super(torchieId, new FeatureCache());
    }

    @Override
    public void resolveReferences() {
    }

    @Override
    protected CarryOverContext getCarryOverContextFromTile(GeometryClock.GridTime gridTime) {
        return null;
    }


}