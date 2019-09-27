package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.WorkToDoQueueWire;
import com.dreamscale.gridtime.core.machine.executor.program.*;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.BoxConfigurationLoaderService;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.MemoryOnlyTorchieState;
import com.dreamscale.gridtime.core.machine.memory.PerProcessTorchieState;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureResolverService;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.TileSearchService;
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class TorchieFactory {

    private static final int MAX_TEAMS = 5;

    @Autowired
    private ProgramFactory programFactory;

    @Autowired
    private FeatureResolverService featureResolverService;

    @Autowired
    private TileSearchService tileSearchService;

    @Autowired
    private WorkToDoQueueWire workToDoQueueWire;

    @Autowired
    private BoxConfigurationLoaderService boxConfigurationLoaderService;

    private Map<UUID, FeatureCache> teamCacheMap = DefaultCollections.lruMap(MAX_TEAMS);


    public Torchie wireUpMemberTorchie(UUID teamId, UUID memberId, LocalDateTime startingPosition) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);

        PerProcessTorchieState torchieState = new PerProcessTorchieState(teamId, memberId,
                featureCache, featureResolverService, tileSearchService);

        //stream data into the tiles
        Program program = programFactory.createBaseTileGeneratorProgram(teamId, memberId, torchieState, startingPosition);

        Torchie torchie = new Torchie(memberId, torchieState, program);

        torchie.configureOutputStreamEventWire(workToDoQueueWire);

        return torchie;

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



}
