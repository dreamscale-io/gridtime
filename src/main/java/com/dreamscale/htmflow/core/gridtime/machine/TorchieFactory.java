package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.*;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.BoxConfigurationLoaderService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.AggregatingWire;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyTorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.PerProcessTorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureResolverService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.TileSearchService;
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


    public Torchie wireUpTeamMemberTorchie(UUID teamId, UUID memberId, LocalDateTime startingPosition) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);
        AggregatingWire teamWire = findOrCreateWire(teamId);

        PerProcessTorchieState torchieState = new PerProcessTorchieState(teamId, memberId,
                featureCache, featureResolverService, tileSearchService);

        //stream data into the tiles
        Program program = programFactory.createBaseTileGeneratorProgram(memberId, torchieState, teamWire, startingPosition);

        return new Torchie(memberId, torchieState, program);

    }

    public Torchie wireUpTeamTorchie(UUID teamId) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);

        PerProcessTorchieState torchieBody = new PerProcessTorchieState(teamId, teamId,
                featureCache, featureResolverService, tileSearchService);

        AggregatingWire teamWire = findOrCreateWire(teamId);

        return null;
//        AggregateByTeamProgram aggregateWiresProgram = programFactory.createAggregateWiresProgram(teamId, torchieBody, teamWire);
//
//        return new Torchie(teamId, torchieBody, aggregateWiresProgram);
        //teamTorchie is going to listen to queue of team events,

    }

    //wire up calendar Torchie, does a years worth of tiles ahead, then stops.

    public Torchie wireUpCalendarTorchie(int maxTiles) {

        UUID torchieId = UUID.randomUUID();
        TorchieState torchieState = new MemoryOnlyTorchieState(torchieId);

        CalendarGeneratorProgram program = programFactory.createCalendarGenerator(maxTiles);

        return new Torchie(torchieId, torchieState, program);
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
