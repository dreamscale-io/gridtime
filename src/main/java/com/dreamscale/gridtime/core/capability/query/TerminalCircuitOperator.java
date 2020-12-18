package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.query.TargetType;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberEntity;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberRepository;
import com.dreamscale.gridtime.core.domain.terminal.*;
import com.dreamscale.gridtime.core.domain.time.GridCalendarEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TerminalCircuitOperator {

    @Autowired
    TerminalCircuitRepository terminalCircuitRepository;

    @Autowired
    CircuitMemberRepository circuitMemberRepository;

    @Autowired
    TerminalCircuitQueryTargetRepository terminalCircuitQueryTargetRepository;

    @Autowired
    TerminalCircuitLocationHistoryRepository terminalCircuitLocationHistoryRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    TorchieFeedCursorRepository torchieFeedCursorRepository;

    @Autowired
    GridClock gridClock;


    public QueryTarget resolveQueryTarget(UUID organizationId, UUID invokingMemberId, TerminalCircuitEntity circuit) {

        QueryTarget queryTarget = null;

        if (circuit != null) {
            queryTarget = resolveLastQueryTargetForCircuit(organizationId, circuit.getId());
        }

        if (queryTarget == null) {
            UUID targetId = null;

            if (circuit != null) {
                targetId = circuit.getCreatorId();
            } else {
                targetId = invokingMemberId;
            }
            String targetName = memberDetailsRetriever.lookupUsername(targetId);
            queryTarget = new QueryTarget(TargetType.USER, targetName, organizationId, targetId);
        }
        return queryTarget;
    }

    private QueryTarget resolveLastQueryTargetForCircuit(UUID organizationId, UUID circuitId) {

        TerminalCircuitQueryTargetEntity lastTarget = terminalCircuitQueryTargetRepository.findLastTargetByCircuitId(organizationId, circuitId);

        if (lastTarget != null) {
            return new QueryTarget(lastTarget.getTargetType(), lastTarget.getTargetName(), organizationId, lastTarget.getTargetId());
        }

        return null;

    }

    @Transactional
    public QueryTarget saveQueryTarget(LocalDateTime now, UUID organizationId, UUID circuitId, QueryTarget queryTarget) {

        TerminalCircuitQueryTargetEntity targetEntity = new TerminalCircuitQueryTargetEntity();
        targetEntity.setId(UUID.randomUUID());
        targetEntity.setTargetType(queryTarget.getTargetType());
        targetEntity.setTargetName(queryTarget.getTargetName().toLowerCase());
        targetEntity.setTargetId(queryTarget.getTargetId());
        targetEntity.setTargetDate(now);
        targetEntity.setCircuitId(circuitId);
        targetEntity.setOrganizationId(organizationId);

        terminalCircuitQueryTargetRepository.save(targetEntity);

        return queryTarget;
    }

    public void saveLocationHistory(LocalDateTime now, UUID organizationId, UUID circuitId, GridtimeExpression tileLocation) {

        GridCalendarEntity tile = calendarService.lookupTile(tileLocation.getZoomLevel(), tileLocation.getCoords());

        validateCalendarTileFound(tileLocation, tile);

        TerminalCircuitLocationHistoryEntity location = new TerminalCircuitLocationHistoryEntity();
        location.setId(UUID.randomUUID());
        location.setOrganizationId(organizationId);
        location.setCircuitId(circuitId);
        location.setCalendarId(tile.getId());
        location.setMovementDate(now);

        terminalCircuitLocationHistoryRepository.save(location);
    }

    public GeometryClock.GridTime resolveLastLocation(UUID organizationId, UUID invokingMemberId, UUID circuitId) {

        GeometryClock.GridTime gridTime = null;

        GridCalendarEntity lastLocation = calendarService.lookupTileByCircuitLocationHistory(organizationId, circuitId);

        if (lastLocation != null) {
            gridTime = GeometryClock.createGridTime(lastLocation.getZoomLevel(), lastLocation.getStartTime());
        }

        if (gridTime == null) {
            TorchieFeedCursorEntity feedCursor = torchieFeedCursorRepository.findByOrganizationIdAndTorchieId(organizationId, invokingMemberId);
            if (feedCursor != null) {
                gridTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, feedCursor.getLastTileProcessedCursor());
            }
        }

        if (gridTime == null) {
            gridTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, gridClock.getGridStart());
        }

        return gridTime;

    }

    private void validateCalendarTileFound(GridtimeExpression tileLocation, GridCalendarEntity tile) {
        if (tile == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_GT_EXPRESSION, "Unable to find calendar for "+tileLocation.getFormattedExpression());
        }
    }


    public TerminalCircuitEntity validateCircuitMembershipAndGetCircuitIfExists(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {
        TerminalCircuitEntity circuit = null;

        if (terminalCircuitContext != null) {
            circuit = terminalCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, terminalCircuitContext);

            validateCircuitExists(terminalCircuitContext, circuit);

            if (!circuit.getCreatorId().equals(invokingMemberId)) {
                validateCircuitMembership(circuit, invokingMemberId);
            }
        }

        return circuit;
    }

    public TerminalCircuitEntity validateCircuitMembershipAndGetCircuit(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {
        TerminalCircuitEntity circuit = terminalCircuitRepository.findByOrganizationIdAndCircuitName(organizationId, terminalCircuitContext);

        validateCircuitExists(terminalCircuitContext, circuit);

        if (!circuit.getCreatorId().equals(invokingMemberId)) {
            validateCircuitMembership(circuit, invokingMemberId);
        }

        return circuit;
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



}
