package com.dreamscale.gridtime.core.machine.memory.cache;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration;
import com.dreamscale.gridtime.core.machine.memory.feature.details.*;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.*;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.type.IdeaFlowStateType;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import com.dreamscale.gridtime.core.machine.memory.type.WorkContextType;
import org.apache.commons.collections.map.LRUMap;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class FeatureCacheManager {

    private static final int NUMBER_TEAM_CACHES = 5;

    private Map<UUID, FeatureCache> featureCachesByTeam = DefaultCollections.lruMap(NUMBER_TEAM_CACHES);

    public FeatureCache findOrCreateFeatureCache(UUID teamId) {

        FeatureCache teamCache = featureCachesByTeam.get(teamId);
        if (teamCache == null) {
            teamCache = new FeatureCache();
            featureCachesByTeam.put(teamId, teamCache);
        }

        return teamCache;
    }

}
