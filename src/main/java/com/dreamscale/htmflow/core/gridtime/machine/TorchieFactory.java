package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.*;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.service.BoxConfigurationLoaderService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.wires.AggregatingWire;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyFeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.PerProcessFeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureResolverService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.service.TileSearchService;
import com.dreamscale.htmflow.core.gridtime.machine.memory.box.TeamBoxConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class TorchieFactory {

    @Autowired
    private ProgramFactory programFactory;

    @Autowired
    private FeatureResolverService featureResolverService;

    @Autowired
    private TileSearchService tileSearchService;


    @Autowired
    private BoxConfigurationLoaderService boxConfigurationLoaderService;

    private static final int MAX_TEAMS = 5;

    private Map<UUID, FeatureCache> teamCacheMap = DefaultCollections.lruMap(MAX_TEAMS);

    private Map<UUID, AggregatingWire> teamWiresMap = DefaultCollections.map();


    public Torchie wireUpMemberTorchie(UUID teamId, UUID memberId, LocalDateTime startingPosition) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);

        PerProcessFeaturePool featurePool = new PerProcessFeaturePool(teamId, memberId,
                featureCache, featureResolverService, tileSearchService);

        //stream data into the tiles
        Program program = programFactory.createBaseTileGeneratorProgram(memberId, featurePool, startingPosition);

        return new Torchie(memberId, featurePool, program);

    }

    public Torchie wireUpTeamTorchie(UUID teamId) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);

        PerProcessFeaturePool featurePool = new PerProcessFeaturePool(teamId, teamId,
                featureCache, featureResolverService, tileSearchService);

        AggregatingWire teamWire = findOrCreateWire(teamId);

        return null;
//        AggregateByTeamProgram aggregateWiresProgram = programFactory.createAggregateWiresProgram(teamId, featurePool, teamWire);
//
//        return new Torchie(teamId, featurePool, aggregateWiresProgram);
        //teamTorchie is going to listen to queue of team events,

    }

    //wire up calendar Torchie, does a years worth of tiles ahead, then stops.

    public Torchie wireUpCalendarTorchie(int maxTiles) {

        UUID torchieId = UUID.randomUUID();
        FeaturePool featurePool = new MemoryOnlyFeaturePool(torchieId);

        CalendarGeneratorProgram program = programFactory.createCalendarGenerator(maxTiles);

        return new Torchie(torchieId, featurePool, program);
    }

    private FeatureCache findOrCreateFeatureCache(UUID teamId) {
        FeatureCache featureCache = teamCacheMap.get(teamId);
        if (featureCache == null) {
            TeamBoxConfiguration teamBoxConfig = boxConfigurationLoaderService.loadBoxConfiguration(teamId);

            featureCache = new FeatureCache(teamBoxConfig);
            teamCacheMap.put(teamId, featureCache);
        }
        return featureCache;
    }

    private AggregatingWire findOrCreateWire(UUID teamId) {
        AggregatingWire teamWire = teamWiresMap.get(teamId);
        if (teamWire == null) {
            teamWire = new AggregatingWire();
            teamWiresMap.put(teamId, teamWire);
        }
        return teamWire;
    }




}
