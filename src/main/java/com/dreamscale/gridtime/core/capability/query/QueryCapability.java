package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.api.query.QueryInputDto;
import com.dreamscale.gridtime.api.query.TargetInputDto;
import com.dreamscale.gridtime.api.query.TargetType;
import com.dreamscale.gridtime.api.query.TimeScope;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository;
import com.dreamscale.gridtime.core.domain.member.TeamEntity;
import com.dreamscale.gridtime.core.domain.member.TeamRepository;
import com.dreamscale.gridtime.core.domain.terminal.TerminalCircuitEntity;
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
    CalendarService calendarService;

    @Autowired
    GridClock gridClock;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TopWTFsQueryRunner topWTFsQueryRunner;

    @Autowired
    MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    TerminalCircuitOperator terminalCircuitOperator;

    public GridTableResults getTopWTFs(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext, QueryInputDto queryInputDto) {

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuitIfExists(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget queryTarget = resolveQueryTarget(organizationId, invokingMemberId, circuit, queryInputDto);

        QueryTimeScope queryTimeScope = resolveQueryTimeScope(queryInputDto.getTimeScope(), queryInputDto.getGridtimeScopeExpression());

        return runTopWTFsQuery(queryTarget, queryTimeScope);
    }

    private GridTableResults runTopWTFsQuery(QueryTarget queryTarget, QueryTimeScope queryTimeScope) {

        return topWTFsQueryRunner.runQuery(queryTarget, queryTimeScope);

    }

    public SimpleStatusDto setQueryTarget(UUID organizationId, UUID invokingMemberId, String terminalCircuitContext, TargetInputDto targetInputDto) {

        LocalDateTime now = gridClock.now();

        TerminalCircuitEntity circuit = terminalCircuitOperator.validateCircuitMembershipAndGetCircuit(organizationId, invokingMemberId, terminalCircuitContext);

        QueryTarget target = resolveAndSaveQueryTarget(now, circuit, targetInputDto);

        return new SimpleStatusDto(Status.SUCCESS, "Query target set to "+target.getTargetType() + " "+targetInputDto.getTargetName());
    }

    private QueryTarget resolveAndSaveQueryTarget(LocalDateTime now, TerminalCircuitEntity circuit, TargetInputDto targetInputDto) {
        QueryTarget queryTarget = resolveQueryTargetFromInput(circuit.getOrganizationId(), targetInputDto.getTargetType(), targetInputDto.getTargetName());

        if (queryTarget == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_FOUND, "Unable to resolve target.");
        }

        return terminalCircuitOperator.saveQueryTarget(now, circuit.getOrganizationId(), circuit.getId(), queryTarget);
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

            queryTimeScope = new QueryTimeScope(calendar.getGridCoords(), calendar.getStartTime(), calendar.getEndTime());

        } else if (gridtimeScopeExpression != null) {
            GridtimeExpression gtExp = GridtimeExpression.parse(gridtimeScopeExpression);

            queryTimeScope = toQueryTimeScope(gtExp);

        }

        return queryTimeScope;

    }

    private QueryTimeScope toQueryTimeScope(GridtimeExpression gtExp) {

        QueryTimeScope queryTimeScope = null;
        if (!gtExp.hasRangeExpression()) {
            GridCalendarEntity calendar = calendarService.lookupTile(gtExp.getZoomLevel(), gtExp.getCoords());

            if (calendar == null ) {
                throw new BadRequestException(ValidationErrorCodes.NO_DATA_AVAILABLE, "Missing calendar data for timescope = "+gtExp.getFormattedExpression());
            }

            queryTimeScope = new QueryTimeScope(gtExp.getFormattedExpression(), calendar.getStartTime(), calendar.getEndTime());
        } else {

            if (!gtExp.getZoomLevel().equals(gtExp.getRangeZoomLevel())) {
                throw new BadRequestException(ValidationErrorCodes.INVALID_GT_EXPRESSION, "Range expression currently only valid on last gt[exp] term: "+gtExp.getFormattedExpression());
            }

            GridCalendarEntity calendarMin = calendarService.lookupTile(gtExp.getZoomLevel(), gtExp.getCoords());

            GridCalendarEntity calendarMax = calendarService.lookupTile(gtExp.getZoomLevel(), gtExp.getRangeCoords());

            if (calendarMin == null || calendarMax == null) {
                throw new BadRequestException(ValidationErrorCodes.NO_DATA_AVAILABLE, "Missing calendar data for timescope = "+gtExp.getFormattedExpression());
            }

            queryTimeScope = new QueryTimeScope(gtExp.getFormattedExpression(), calendarMin.getStartTime(), calendarMax.getEndTime());
        }

        return queryTimeScope;

    }


    private QueryTarget resolveQueryTarget(UUID organizationId, UUID invokingMemberId, TerminalCircuitEntity circuit, QueryInputDto queryInputDto) {


        QueryTarget queryTarget = resolveQueryTargetFromInput(organizationId, queryInputDto.getTargetType(), queryInputDto.getTargetName());

        if (queryTarget == null && circuit != null) {

            queryTarget = terminalCircuitOperator.resolveLastQueryTarget(organizationId, circuit.getId());
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

    private QueryTarget resolveQueryTargetFromInput(UUID organizationId, TargetType targetType, String targetName) {
        QueryTarget queryTarget = null;

        if (targetType != null ) {
            validateNotNull("targetName", targetName);

            String lowercaseName = targetName.toLowerCase();

            if (targetType.equals(TargetType.USER)) {
                OrganizationMemberEntity member = organizationMemberRepository.findByOrganizationIdAndLowercaseUsername(organizationId, lowercaseName);
                validateMemberFound(targetName, member);

                queryTarget = new QueryTarget(targetType, lowercaseName, organizationId, member.getId());
            } else if (targetType.equals(TargetType.TEAM)) {
                TeamEntity team = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, lowercaseName);
                validateTeamFound(targetName, team);

                queryTarget = new QueryTarget(targetType, lowercaseName, organizationId, team.getId());
            }
        }
        return queryTarget;
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

    public SimpleStatusDto getCurrentTime() {
        GeometryClock.GridTime currentTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, gridClock.now());

        return new SimpleStatusDto(Status.VALID, "Current Gridtime is "+currentTime.getFormattedGridTime() + " ("+ currentTime.getFormattedCoords() + ")");
    }
}
