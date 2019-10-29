package com.dreamscale.gridtime.core.machine.memory.box;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.BoxConfigurationLoaderService;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import org.apache.commons.collections.map.LRUMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class TeamBoxConfigurationManager {

    private static final int MAX_TEAMS = 5;

    @Autowired
    private BoxConfigurationLoaderService boxConfigurationLoaderService;

    private Map<UUID, TeamBoxConfiguration> boxConfigurationsByTeam = DefaultCollections.lruMap(MAX_TEAMS);


    public TeamBoxConfiguration findOrCreateTeamBoxConfig(UUID teamId) {
        TeamBoxConfiguration teamBoxConfig = boxConfigurationsByTeam.get(teamId);
        if (teamBoxConfig == null) {
            teamBoxConfig = boxConfigurationLoaderService.loadBoxConfiguration(teamId);
            boxConfigurationsByTeam.put(teamId, teamBoxConfig);
        }
        return teamBoxConfig;
    }



}
