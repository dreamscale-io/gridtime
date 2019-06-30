package com.dreamscale.htmflow.core.gridtime.machine.memory;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureResolverService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.service.TileSearchService;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.CarryOverContext;

import java.util.Set;
import java.util.UUID;

public class PerProcessFeaturePool extends AbstractFeaturePool {

    private final UUID teamId;
    private final UUID torchieId;

    private final FeatureResolverService featureResolverService;
    private final TileSearchService tileSearchService;

    public PerProcessFeaturePool(UUID teamId, UUID torchieId, FeatureCache featureCache,
                                 FeatureResolverService featureResolverService, TileSearchService tileSearchService) {
        super(torchieId, featureCache);

        this.teamId = teamId;
        this.torchieId = torchieId;
        this.featureResolverService = featureResolverService;
        this.tileSearchService = tileSearchService;
    }

    @Override
    public void resolveReferences() {
        Set<FeatureReference> features = getActiveGridTile().getMusicGrid().getAllFeatures();

        for (FeatureReference feature : features) {
            featureResolverService.resolve(teamId, feature);
        }
    }

    @Override
    protected CarryOverContext getCarryOverContextFromTile(GeometryClock.GridTime gridTime) {
        return tileSearchService.getCarryOverContextOfTile(torchieId, gridTime);
    }


}
