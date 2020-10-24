package com.dreamscale.gridtime.core.machine.memory;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemoryOnlyTorchieState extends AbstractTorchieState {


    public MemoryOnlyTorchieState(UUID organizationId, UUID torchieId) {
        super(organizationId, torchieId, new ArrayList<>(), new BoxResolver(null), new FeatureCache());
    }

    public MemoryOnlyTorchieState(UUID organizationId, UUID torchieId, List<UUID> teamIds) {
        super(organizationId, torchieId, teamIds, new BoxResolver(null), new FeatureCache());
    }

    @Override
    public void resolveFeatureReferences() {
    }

    @Override
    protected CarryOverContext getCarryOverContextFromTileDB(GeometryClock.GridTime gridTime) {
        return null;
    }


}
