package com.dreamscale.htmflow.core.gridtime.machine.memory;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;

import java.util.UUID;

public class MemoryOnlyTorchieState extends AbstractTorchieState {


    public MemoryOnlyTorchieState(UUID torchieId) {
        super(torchieId, new FeatureCache());
    }



    @Override
    public void resolveFeatureReferences() {
    }

    @Override
    protected CarryOverContext getCarryOverContextFromTileDB(GeometryClock.GridTime gridTime) {
        return null;
    }


}
