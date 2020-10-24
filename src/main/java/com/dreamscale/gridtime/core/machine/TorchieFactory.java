package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.domain.member.TeamEntity;
import com.dreamscale.gridtime.core.domain.member.TeamMemberRepository;
import com.dreamscale.gridtime.core.domain.member.TeamRepository;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.AggregateWorkToDoQueueWire;
import com.dreamscale.gridtime.core.machine.executor.program.*;
import com.dreamscale.gridtime.core.machine.memory.PerProcessTorchieState;
import com.dreamscale.gridtime.core.machine.memory.box.BoxConfigLoader;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureResolverService;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.TileSearchService;
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private FeatureCache featureCache ;

    @Autowired
    private BoxConfigLoader boxConfigLoader;

    @Autowired
    private TeamRepository teamRepository;

    private BoxResolver boxResolver;

    @PostConstruct
    private void init() {
        boxResolver = new BoxResolver(boxConfigLoader);
    }

    /**
     * Run Torchie without an effective end date
     */
    public Torchie wireUpMemberTorchie(UUID organizationId, UUID memberId, UUID teamId, LocalDateTime startingPosition) {

        return wireUpMemberTorchie(organizationId, memberId, DefaultCollections.toList(teamId), startingPosition, startingPosition.plusYears(1));
    }


    /**
     * Run Torchie without an effective end date
     */
    public Torchie wireUpMemberTorchie(UUID organizationId, UUID memberId, List<UUID> teamIds, LocalDateTime startingPosition) {

        return wireUpMemberTorchie(organizationId, memberId, teamIds, startingPosition, startingPosition.plusYears(1));
    }

    /**
     * Run Torchie with a date range
     */
    public Torchie wireUpMemberTorchie(UUID organizationId, UUID memberId, List<UUID> teamIds, LocalDateTime startingPosition, LocalDateTime runUntilPosition) {

        PerProcessTorchieState torchieState = new PerProcessTorchieState(organizationId, memberId, teamIds, featureCache,
                boxResolver, featureResolverService, tileSearchService);

        Program program = programFactory.createBaseTileGeneratorProgram(memberId, torchieState, startingPosition, runUntilPosition);

        Torchie torchie = new Torchie(memberId, torchieState, program);

        torchie.configureOutputStreamEventWire(workToDoWire);

        return torchie;

    }



}
