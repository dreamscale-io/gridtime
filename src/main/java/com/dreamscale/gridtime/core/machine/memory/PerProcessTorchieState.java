package com.dreamscale.gridtime.core.machine.memory;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureResolverService;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.TileSearchService;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;

import java.util.Set;
import java.util.UUID;

public class PerProcessTorchieState extends AbstractTorchieState {

    private final FeatureResolverService featureResolverService;
    private final TileSearchService tileSearchService;

    public PerProcessTorchieState(UUID teamId, UUID torchieId, FeatureCache featureCache,
                                  FeatureResolverService featureResolverService, TileSearchService tileSearchService) {
        super(torchieId, teamId, featureCache);

        this.featureResolverService = featureResolverService;
        this.tileSearchService = tileSearchService;
    }


    @Override
    public void resolveFeatureReferences() {
        Set<FeatureReference> features = getActiveTile().getAllFeatures();

        for (FeatureReference feature : features) {
            featureResolverService.resolve(getTeamId(), feature);
        }
    }

    @Override
    protected CarryOverContext getCarryOverContextFromTileDB(GeometryClock.GridTime gridTime) {
        return tileSearchService.getCarryOverContextOfTile(getTorchieId(), gridTime);
    }


}
