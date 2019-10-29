package com.dreamscale.gridtime.core.machine.memory;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;

import java.util.UUID;

public class MemoryOnlyTorchieState extends AbstractTorchieState {


    public MemoryOnlyTorchieState(UUID torchieId) {
        super(torchieId, null, new TeamBoxConfiguration.Builder().build(), new FeatureCache());
    }


    @Override
    public void resolveFeatureReferences() {
    }

    @Override
    protected CarryOverContext getCarryOverContextFromTileDB(GeometryClock.GridTime gridTime) {
        return null;
    }


}
