package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.query.GridLocationDto;
import com.dreamscale.gridtime.api.query.LocationInputDto;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberEntity;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberRepository;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberStatusRepository;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitLocationHistoryEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitLocationHistoryRepository;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitRepository;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.GridtimeSequence;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ExplorerCapability {

    @Autowired
    TerminalCircuitRepository terminalCircuitRepository;

    @Autowired
    CircuitMemberRepository circuitMemberRepository;

    @Autowired
    CircuitMemberStatusRepository circuitMemberStatusRepository;

    @Autowired
    TerminalCircuitLocationHistoryRepository terminalCircuitLocationHistoryRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    GridClock gridClock;

    @Autowired
    MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    TorchieFeedCursorRepository torchieFeedCursorRepository;

    public GridLocationDto gotoLocation(UUID organizationId, UUID memberId, String terminalCircuitContext, LocationInputDto locationInputDto) {
        return null;
    }

    public GridLocationDto look(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {

        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = terminalCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, terminalCircuitContext);

        validateCircuitExists(terminalCircuitContext, circuit);

        if (!circuit.getCreatorId().equals(invokingMemberId)) {
            validateCircuitMembership(circuit, invokingMemberId);
        }

        TerminalCircuitLocationHistoryEntity location = terminalCircuitLocationHistoryRepository.findLastLocationByOrganizationIdAndCircuitName(organizationId, terminalCircuitContext);

        if (location == null) {
            location = createInitialLocationFromFeedLocation(now, organizationId, circuit.getCreatorId(), circuit.getId());
        }

        GridtimeSequence gridTimeSequence = calendarService.lookupGridTimeSequence(location.getZoomLevel(), location.getTileSeq());

        GridLocationDto gridLocation = new GridLocationDto();
        gridLocation.setGridTime(gridTimeSequence.getGridTime().getFormattedGridTime());
        gridLocation.setZoomLevel(location.getZoomLevel().name());
        gridLocation.setCoordinates(gridTimeSequence.getGridTime().getCoordinates());

        GridTableResults results = runQuery(circuit, gridTimeSequence );

        return null;
    }

    private GridTableResults runQuery(TerminalCircuitEntity circuit, GridtimeSequence gridTimeSequence) {



        return null;
    }

    private TerminalCircuitLocationHistoryEntity createInitialLocationFromFeedLocation(LocalDateTime now, UUID organizationId, UUID memberId, UUID circuitId) {

        TorchieFeedCursorEntity cursor = torchieFeedCursorRepository.findByOrganizationIdAndTorchieId(organizationId, memberId);

        if (cursor == null || cursor.getLastTileProcessedCursor() == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_DATA_AVAILABLE, "Unable to initialize location. No feed data available.");
        }

        MemberDetailsEntity memberDetails = memberDetailsRetriever.lookupMemberDetails(organizationId, memberId);

        Long tileSeq = calendarService.lookupTileSequenceFromSameTime(ZoomLevel.TWENTY, cursor.getLastTileProcessedCursor());

        TerminalCircuitLocationHistoryEntity historyEntity = new TerminalCircuitLocationHistoryEntity();
        historyEntity.setId(UUID.randomUUID());
        historyEntity.setCircuitId(circuitId);
        historyEntity.setMovementDate(now);
        historyEntity.setZoomLevel(ZoomLevel.TWENTY);
        historyEntity.setTileSeq(tileSeq.intValue());

        terminalCircuitLocationHistoryRepository.save(historyEntity);

        return historyEntity;

    }


    private void validateCircuitExists(String circuitName, TerminalCircuitEntity terminalCircuitEntity) {
        if (terminalCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find terminal circuit: " + circuitName);
        }
    }

    private void validateCircuitMembership(TerminalCircuitEntity circuit, UUID invokingMemberId) {
        CircuitMemberEntity membership = circuitMemberRepository.findByOrganizationIdAndCircuitIdAndMemberId(circuit.getOrganizationId(), circuit.getId(), invokingMemberId);

        if (membership == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_JOINED_TO_CIRCUIT, "Member is not joined to terminal circuit "+circuit.getCircuitName());
        }
    }
    public GridLocationDto zoomIn(UUID organizationId, UUID memberId, String terminalCircuitContext) {
        return null;
    }

    public GridLocationDto zoomOut(UUID organizationId, UUID memberId, String terminalCircuitContext) {
        return null;
    }

    public GridLocationDto panLeft(UUID organizationId, UUID memberId, String terminalCircuitContext) {
        return null;
    }

    public GridLocationDto panRight(UUID organizationId, UUID memberId, String terminalCircuitContext) {
        return null;
    }
}
