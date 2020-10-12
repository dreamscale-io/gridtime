package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.executor.circuit.wires.AggregateWorkToDoQueueWire;
import com.dreamscale.gridtime.core.machine.executor.program.*;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.MemoryOnlyTorchieState;
import com.dreamscale.gridtime.core.machine.memory.PerProcessTorchieState;
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfigurationManager;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureResolverService;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.TileSearchService;
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    private AggregateWorkToDoQueueWire workToDoWire;


    @Autowired
    private FeatureCacheManager featureCacheManager;

    @Autowired
    private TeamBoxConfigurationManager teamBoxConfigurationManager;

    /**
     * Run without an effective end date
     * @param teamId
     * @param memberId
     * @param startingPosition
     * @return
     */
    public Torchie wireUpMemberTorchie(UUID teamId, UUID memberId, LocalDateTime startingPosition) {

        return wireUpMemberTorchie(teamId, memberId, startingPosition, startingPosition.plusYears(1));
    }

    public Torchie wireUpMemberTorchie(UUID teamId, UUID memberId, LocalDateTime startingPosition, LocalDateTime runUntilPosition) {

        FeatureCache featureCache = featureCacheManager.findOrCreateFeatureCache(teamId);
        TeamBoxConfiguration teamBoxConfig = teamBoxConfigurationManager.findOrCreateTeamBoxConfig(teamId);

        PerProcessTorchieState torchieState = new PerProcessTorchieState(teamId, memberId,  featureCache,
                teamBoxConfig, featureResolverService, tileSearchService);

        //stream data into the tiles
        Program program = programFactory.createBaseTileGeneratorProgram(teamId, memberId, torchieState, startingPosition, runUntilPosition);

        Torchie torchie = new Torchie(memberId, torchieState, program);

        torchie.configureOutputStreamEventWire(workToDoWire);

        return torchie;

    }


}
