package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberEntity;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberRepository;
import com.dreamscale.gridtime.core.domain.terminal.*;
import com.dreamscale.gridtime.core.domain.time.GridCalendarEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
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


    public QueryTarget resolveLastQueryTarget(UUID organizationId, UUID circuitId) {

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
