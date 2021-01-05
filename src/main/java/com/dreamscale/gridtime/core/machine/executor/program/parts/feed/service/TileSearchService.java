package com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service;

import com.dreamscale.gridtime.core.domain.tile.GridTileCarryOverContextEntity;
import com.dreamscale.gridtime.core.domain.tile.GridTileCarryOverContextRepository;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureResolverService;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureId;
import com.dreamscale.gridtime.core.machine.memory.tile.CarryOverContext;
import com.dreamscale.gridtime.core.machine.memory.tile.ExportedCarryOverContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TileSearchService {

    @Autowired
    CalendarService calendarService;

    @Autowired
    FeatureResolverService featureResolverService;

    @Autowired
    GridTileCarryOverContextRepository gridTileCarryOverContextRepository;

    public CarryOverContext getCarryOverContextOfTile(UUID organizationId, UUID torchieId, GeometryClock.GridTime gridTime) {

        UUID calendarId = calendarService.lookupPotentialCalendarId(gridTime);
        if (calendarId == null) {
            return null;
        }
        
        GridTileCarryOverContextEntity contextEntity = gridTileCarryOverContextRepository.findByTorchieIdAndCalendarId(torchieId, calendarId);

        if (contextEntity != null) {
            ExportedCarryOverContext exported = JSONTransformer.fromJson(contextEntity.getJson(), ExportedCarryOverContext.class);

            Map<UUID, FeatureReference> featureMap = resolveFeatures(organizationId, exported);

            CarryOverContext carryOverContext = new CarryOverContext();

            carryOverContext.importContext(exported, featureMap);

            return carryOverContext;
        }
        return null;
    }

    private Map<UUID, FeatureReference> resolveFeatures(UUID organizationId, ExportedCarryOverContext exported) {
        Map<UUID, FeatureReference> featureMap = new HashMap<>();

        for (UUID featureId : exported.getAllFeatureIds()) {
            if (!featureMap.containsKey(featureId)) {
                FeatureReference feature = featureResolverService.lookupById(organizationId, featureId);
                featureMap.put(featureId, feature);
            }
        }

        return featureMap;
    }

    List<IdeaFlowMetrics> queryTiles(UUID torchieId, GeometryClock.GridTime queryByGridTime) {
        //zoom in one level for the same feed
        return null;
    }

    List<FeatureId> queryFeatureSet(UUID torchieId, GeometryClock.GridTime queryByGridTime) {
        //zoom in one level for the same feed, get all the feature reference objects, bookmark by what...?

        //can do 10 featureIds at a time
        return null;
    }

    List<BoxMetrics> queryBoxMetrics(UUID torchieId, FeatureId featureId, GeometryClock.GridTime queryByGridTime) {
        //public List<IdeaFlowTile>

        //these are atomic operations.  FeatureMetrics for the new tile, are combined all at once, and then closed.
        //If the feature is present, it includes all features.


        //then can do a list of metrics at a time, or maybe I just join?  How do I know which features I've aggregated?
        return null;
    }

}
