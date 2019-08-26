package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.team.TeamDto;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberRepository;
import com.dreamscale.htmflow.core.domain.tile.TorchieBookmarkEntity;
import com.dreamscale.htmflow.core.domain.tile.TorchieBookmarkRepository;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.Torchie;
import com.dreamscale.htmflow.core.gridtime.machine.GridTimeExecutor;
import com.dreamscale.htmflow.core.gridtime.machine.TorchieFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.ProgramFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.workpile.TorchieWorkerPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
public class TorchieService {

    @Autowired
    private TorchieFactory torchieFactory;

    @Autowired
    private TorchieBookmarkRepository torchieBookmarkRepository;

    @Autowired
    private TimeService timeService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private OrganizationMemberRepository memberRepository;

    @Autowired
    private JournalService journalService;

    private GridTimeExecutor gridTimeExecutor;


    public CircuitMonitor startMemberTorchie(UUID memberId) {
        OrganizationMemberEntity member = memberRepository.findById(memberId);

        Torchie torchie = findOrCreateMemberTorchie(member.getOrganizationId(), memberId);
        gridTimeExecutor.startTorchieIfNotActive(torchie);

        return torchie.getCircuitMonitor();
    }

    public Torchie findOrCreateMemberTorchie(UUID organizationId, UUID memberId) {
        Torchie torchie = null;

        if (!gridTimeExecutor.contains(memberId)) {
            LocalDateTime startingPosition = determineStartingPositionForMemberFeed(memberId);
            UUID teamId = determineTeam(organizationId, memberId);

            if (startingPosition != null) {
                torchie = torchieFactory.wireUpMemberTorchie(teamId, memberId, startingPosition);
            } else {
                log.error("Unable to start Torchie for until first intention created, memberId: "+memberId);
            }

        } else {
            torchie = gridTimeExecutor.getTorchie(memberId);
        }
        return torchie;
    }

    private UUID determineTeam(UUID organizationId, UUID memberId) {

        TeamDto team = teamService.getMyPrimaryTeam(organizationId, memberId);

        return team.getId();
    }


    private LocalDateTime determineStartingPositionForMemberFeed(UUID memberId) {
        LocalDateTime startingPosition = null;

        TorchieBookmarkEntity torchieBookmark = torchieBookmarkRepository.findByTorchieId(memberId);
        if (torchieBookmark != null) {
            startingPosition = torchieBookmark.getMetronomeCursor();
        } else {
            LocalDateTime dateOfFirstIntention = journalService.getDateOfFirstIntention(memberId);
            if (dateOfFirstIntention != null) {
                startingPosition = GeometryClock.roundDownToNearestTwenty(dateOfFirstIntention);
            }
        }

        return startingPosition;
    }



}
