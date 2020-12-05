package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.api.query.QueryInputDto;
import com.dreamscale.gridtime.api.query.TargetInputDto;
import com.dreamscale.gridtime.api.query.TargetType;
import com.dreamscale.gridtime.api.query.TimeScope;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberEntity;
import com.dreamscale.gridtime.core.domain.circuit.CircuitMemberRepository;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository;
import com.dreamscale.gridtime.core.domain.member.TeamEntity;
import com.dreamscale.gridtime.core.domain.member.TeamRepository;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitQueryTargetEntity;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitQueryTargetRepository;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitRepository;
import com.dreamscale.gridtime.core.domain.time.GridCalendarEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class QueryCapability {

    @Autowired
    TerminalCircuitRepository terminalCircuitRepository;

    @Autowired
    CircuitMemberRepository circuitMemberRepository;

    @Autowired
    TerminalCircuitQueryTargetRepository terminalCircuitQueryTargetRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    GridClock gridClock;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TopWTFsQueryRunner topWTFsQueryRunner;


    public GridTableResults getTopWTFs(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext, QueryInputDto queryInputDto) {

        TerminalCircuitEntity circuit = validateCircuitMembershipAndGetCircuitIfExists(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget queryTarget = resolveQueryTarget(organizationId, invokingMemberId, circuit, queryInputDto);

        QueryTimeScope queryTimeScope = resolveQueryTimeScope(queryInputDto.getTimeScope(), queryInputDto.getGridtimeScopeExpression());

        return runTopWTFsQuery(queryTarget, queryTimeScope);
    }

    private GridTableResults runTopWTFsQuery(QueryTarget queryTarget, QueryTimeScope queryTimeScope) {

        return topWTFsQueryRunner.runQuery(queryTarget, queryTimeScope);

    }

    public SimpleStatusDto setQueryTarget(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext, TargetInputDto targetInputDto) {

        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget target = resolveAndSaveQueryTarget(now, circuit, targetInputDto);

        return new SimpleStatusDto(Status.SUCCESS, "Query target set to "+target.getTargetType() + " "+targetInputDto.getTargetName());
    }


    private QueryTimeScope resolveQueryTimeScope(TimeScope timeScope, String gridtimeScopeExpression) {
        QueryTimeScope queryTimeScope = null;

        if (timeScope != null ) {

            GridCalendarEntity calendar = null;

            LocalDateTime now = gridClock.now();

            switch (timeScope) {
                case BLOCK:
                    calendar = calendarService.lookupTile(ZoomLevel.BLOCK, now);
                    break;
                case WEEK:
                    calendar = calendarService.lookupTile(ZoomLevel.WEEK, now);
                    break;
                case DAY:
                    calendar = calendarService.lookupTile(ZoomLevel.DAY, now);
                    break;
            }

            if (calendar == null ) {
                throw new BadRequestException(ValidationErrorCodes.NO_DATA_AVAILABLE, "Missing calendar data for timescope = "+timeScope);
            }

            queryTimeScope = new QueryTimeScope(calendar.getGridTime(), calendar.getStartTime(), calendar.getEndTime());

        } else {
            throw new BadRequestException(ValidationErrorCodes.NO_DATA_AVAILABLE, "Gridtime expressions not yet supported");
        }

        return queryTimeScope;

    }

    private QueryTarget resolveAndSaveQueryTarget(LocalDateTime now, TerminalCircuitEntity circuit, TargetInputDto targetInputDto) {

        TargetType targetType = targetInputDto.getTargetType();
        UUID targetId = resolveTargetId(circuit.getOrganizationId(), targetInputDto.getTargetType(), targetInputDto.getTargetName());

        validateNotNull("targetType", targetType);
        validateNotNull("targetId", targetId);

        TerminalCircuitQueryTargetEntity targetEntity = new TerminalCircuitQueryTargetEntity();
        targetEntity.setId(UUID.randomUUID());
        targetEntity.setTargetType(targetType);
        targetEntity.setTargetName(targetInputDto.getTargetName().toLowerCase());
        targetEntity.setTargetId(targetId);
        targetEntity.setTargetDate(now);
        targetEntity.setCircuitId(circuit.getId());
        targetEntity.setOrganizationId(circuit.getOrganizationId());

        terminalCircuitQueryTargetRepository.save(targetEntity);

        return new QueryTarget(targetType, targetInputDto.getTargetName(), circuit.getOrganizationId(), targetId);
    }


    private QueryTarget resolveQueryTarget(UUID organizationId, UUID invokingMemberId, TerminalCircuitEntity circuit, QueryInputDto queryInputDto) {

        TargetType targetType = queryInputDto.getTargetType();
        String targetName = queryInputDto.getTargetName();

        UUID targetId = resolveTargetId(organizationId, queryInputDto.getTargetType(), queryInputDto.getTargetName());

        if (targetId == null && circuit != null) {
            TerminalCircuitQueryTargetEntity lastTarget = terminalCircuitQueryTargetRepository.findLastTargetByCircuitId(circuit.getId());

            if (lastTarget != null) {
                targetType = lastTarget.getTargetType();
                targetId = lastTarget.getTargetId();
                targetName = lastTarget.getTargetName();
            }
        }

        if (targetId == null) {
            targetType = TargetType.USER;

            if (circuit != null) {
                targetId = circuit.getCreatorId();
            } else {
                targetId = invokingMemberId;
            }
        }
        return new QueryTarget(targetType, targetName, organizationId, targetId);
    }

    private UUID resolveTargetId(UUID organizationId, TargetType targetType, String targetName) {
        UUID targetId = null;

        if (targetType != null ) {
            validateNotNull("targetName", targetName);

            String lowercaseName = targetName.toLowerCase();

            if (targetType.equals(TargetType.USER)) {
                OrganizationMemberEntity member = organizationMemberRepository.findByOrganizationIdAndLowercaseUsername(organizationId, lowercaseName);
                validateMemberFound(targetName, member);

                targetId = member.getId();
            } else if (targetType.equals(TargetType.TEAM)) {
                TeamEntity team = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, lowercaseName);
                validateTeamFound(targetName, team);

                targetId = team.getId();
            }
        }
        return targetId;
    }

    private TerminalCircuitEntity validateCircuitMembershipAndGetCircuitIfExists(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {
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

    private TerminalCircuitEntity validateCircuitMembershipAndGetCircuit(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext) {
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

    private void validateMemberFound(String memberName, OrganizationMemberEntity memberEntity) {
        if (memberEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_FOUND, "Targeted member not found: " + memberName);
        }
    }

    private void validateNotNull(String propertyName, Object property) {
        if (property == null) {
            throw new BadRequestException(ValidationErrorCodes.PROPERTY_CANT_BE_NULL, "Property " + propertyName + " cant be null");
        }
    }

    private void validateTeamFound(String teamName, TeamEntity teamEntity) {
        if (teamEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TEAM, "Team not found: " + teamName);
        }
    }

    private void validateCircuitMembership(TerminalCircuitEntity circuit, UUID invokingMemberId) {
        CircuitMemberEntity membership = circuitMemberRepository.findByOrganizationIdAndCircuitIdAndMemberId(circuit.getOrganizationId(), circuit.getId(), invokingMemberId);

        if (membership == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_JOINED_TO_CIRCUIT, "Member is not joined to terminal circuit "+circuit.getCircuitName());
        }
    }

    public SimpleStatusDto getCurrentTime() {
        GeometryClock.GridTime currentTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, gridClock.now());

        return new SimpleStatusDto(Status.VALID, "Current Gridtime is "+currentTime.getFormattedGridTime() + " ("+ currentTime.getFormattedCoords() + ")");
    }
}
