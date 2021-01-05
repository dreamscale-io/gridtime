package com.dreamscale.gridtime.core.machine.memory;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureResolverService;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.TileSearchService;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PerProcessTorchieState extends AbstractTorchieState {

    private final FeatureResolverService featureResolverService;
    private final TileSearchService tileSearchService;
    private final BoxResolver boxResolver;

    public PerProcessTorchieState(UUID organizationId, UUID torchieId, List<UUID> teamIds, FeatureCache featureCache, BoxResolver boxResolver,
                                  FeatureResolverService featureResolverService, TileSearchService tileSearchService) {
        super(organizationId, torchieId, teamIds, boxResolver, featureCache);

        this.featureResolverService = featureResolverService;
        this.tileSearchService = tileSearchService;
        this.boxResolver = boxResolver;
    }


    @Override
    public void resolveFeatureReferences() {
        Set<FeatureReference> features = getActiveTile().getAllFeatures();

        for (FeatureReference feature : features) {
            featureResolverService.resolve(getOrganizationId(), feature);
        }
    }

    @Override
    public TorchieState fork() {
        TorchieState forkedState = new PerProcessTorchieState(getOrganizationId(), getTorchieId(), getTeamIds(), getFeatureCache(),
                boxResolver, featureResolverService, tileSearchService);

        return forkedState;
    }

    @Override
    protected CarryOverContext getCarryOverContextFromTileDB(GeometryClock.GridTime gridTime) {
        return tileSearchService.getCarryOverContextOfTile(getOrganizationId(), getTorchieId(), gridTime);
    }


}
