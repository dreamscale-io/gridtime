package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.api.query.QueryInputDto;
import com.dreamscale.gridtime.api.query.TargetInputDto;
import com.dreamscale.gridtime.api.query.TargetType;
import com.dreamscale.gridtime.api.query.TimeScope;
import com.dreamscale.gridtime.api.terminal.ActivityContext;
import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.query.QueryCapability;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRoute;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRouteRegistry;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.QUERY_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class QueryResource {

    @Autowired
    OrganizationCapability organizationCapability;

    @Autowired
    QueryCapability queryCapability;

    @Autowired
    TerminalRouteRegistry terminalRouteRegistry;

    @PostConstruct
    void init() {
        terminalRouteRegistry.register(ActivityContext.QUERY, Command.SELECT,
                "Select data from DB",
                new SelectTopWTFsTerminalRoute(),
                new SelectTopWTFsForUserTerminalRoute(),
                new SelectTopWTFsForTeamTerminalRoute());

        terminalRouteRegistry.register(ActivityContext.QUERY, Command.TARGET,
                "Set target user or team for all queries",
                new SetUserTargetTerminalRoute(),
                new SetTeamTargetTerminalRoute());

    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.TOP_PATH + ResourcePaths.WTF_PATH )
    public GridTableResults getTopWTFs(@RequestParam("scope") Optional<TimeScope> timeScope,
                                       @RequestParam("target_type") Optional<TargetType> targetType,
                                       @RequestParam("target_name") Optional<String> targetName) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("getTopWTFs, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        QueryInputDto queryInputDto = new QueryInputDto();

        if (timeScope.isPresent()) {
            queryInputDto.setTimeScope(timeScope.get());
        }

        if (targetType.isPresent()) {
            queryInputDto.setTargetType(targetType.get());
        }

        if (targetName.isPresent()) {
            queryInputDto.setTargetName(targetName.get());
        }

        return queryCapability.getTopWTFs(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext,
                queryInputDto);
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TARGET_PATH )
    public SimpleStatusDto setQueryTarget(@RequestBody TargetInputDto targetInputDto) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("setQueryTarget, user={}", invokingMember.getBestAvailableName());

        String terminalCircuitContext = context.getTerminalCircuitContext();

        return queryCapability.setQueryTarget(invokingMember.getOrganizationId(), invokingMember.getId(), terminalCircuitContext, targetInputDto);
    }

    private class SelectTopWTFsTerminalRoute extends TerminalRoute {

        private static final String SCOPE_PARAM = "timeScope";

        SelectTopWTFsTerminalRoute() {
            super(Command.SELECT, "top wtfs in {" + SCOPE_PARAM + "}");
            describeChoiceOption(SCOPE_PARAM, TimeScope.BLOCK.name(), TimeScope.WEEK.name(), TimeScope.DAY.name(), "gt[expression]");
        }

        @Override
        public Object route(Map<String, String> params) {
            String scopeName = params.get(SCOPE_PARAM);

            if (!scopeName.startsWith("gt")) {
                try {
                    TimeScope scope = TimeScope.valueOf(scopeName.toUpperCase());
                    return getTopWTFs(Optional.of(scope), Optional.empty(), Optional.empty());
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException(ValidationErrorCodes.INVALID_COMMAND_PARAMETERS, "Invalid scope parameter "+scopeName);
                }
            } else {
                throw new BadRequestException(ValidationErrorCodes.INVALID_COMMAND_PARAMETERS, "gt[expression]s not yet supported");
            }
        }
    }

    private class SelectTopWTFsForTeamTerminalRoute extends TerminalRoute {

        private static final String SCOPE_PARAM = "timeScope";
        private static final String TEAM_PARAM = "teamName";

        SelectTopWTFsForTeamTerminalRoute() {
            super(Command.SELECT, "top wtfs in {" + SCOPE_PARAM + "} for team {"+ TEAM_PARAM + "}");
            describeChoiceOption(SCOPE_PARAM, TimeScope.BLOCK.name(), TimeScope.WEEK.name(), TimeScope.DAY.name(), "gt[expression]");
            describeTextOption(TEAM_PARAM, "name of the team to query");
        }

        @Override
        public Object route(Map<String, String> params) {
            String scopeName = params.get(SCOPE_PARAM);
            String teamName = params.get(TEAM_PARAM);

            if (!scopeName.startsWith("gt")) {
                try {
                    TimeScope scope = TimeScope.valueOf(scopeName.toUpperCase());
                    return getTopWTFs(Optional.of(scope), Optional.of(TargetType.TEAM), Optional.of(teamName));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException(ValidationErrorCodes.INVALID_COMMAND_PARAMETERS, "Invalid scope parameter "+scopeName);
                }
            } else {
                throw new BadRequestException(ValidationErrorCodes.INVALID_COMMAND_PARAMETERS, "gt[expression]s not yet supported");
            }
        }
    }

    private class SelectTopWTFsForUserTerminalRoute extends TerminalRoute {

        private static final String SCOPE_PARAM = "timeScope";
        private static final String USERNAME_PARAM = "username";

        SelectTopWTFsForUserTerminalRoute() {
            super(Command.SELECT, "top wtfs in {" + SCOPE_PARAM + "} for user {"+ USERNAME_PARAM + "}");
            describeChoiceOption(SCOPE_PARAM, TimeScope.BLOCK.name(), TimeScope.WEEK.name(), TimeScope.DAY.name(), "gt[expression]");
            describeTextOption(USERNAME_PARAM, "name of the user to query");
        }

        @Override
        public Object route(Map<String, String> params) {
            String scopeName = params.get(SCOPE_PARAM);
            String username = params.get(USERNAME_PARAM);

            if (!scopeName.startsWith("gt")) {
                try {
                    TimeScope scope = TimeScope.valueOf(scopeName.toUpperCase());
                    return getTopWTFs(Optional.of(scope), Optional.of(TargetType.MEMBER), Optional.of(username));
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException(ValidationErrorCodes.INVALID_COMMAND_PARAMETERS, "Invalid scope parameter "+scopeName);
                }
            } else {
                throw new BadRequestException(ValidationErrorCodes.INVALID_COMMAND_PARAMETERS, "gt[expression]s not yet supported");
            }
        }
    }

    private class SetUserTargetTerminalRoute extends TerminalRoute {

        private static final String USERNAME_PARAM = "username";

        SetUserTargetTerminalRoute() {
            super(Command.TARGET, "user {" + USERNAME_PARAM + "}");
            describeTextOption(USERNAME_PARAM, "user to target for all queries");
        }

        @Override
        public Object route(Map<String, String> params) {
            String username = params.get(USERNAME_PARAM);

            return setQueryTarget(new TargetInputDto(TargetType.MEMBER, username));
        }
    }

    private class SetTeamTargetTerminalRoute extends TerminalRoute {

        private static final String TEAM_PARAM = "teamName";

        SetTeamTargetTerminalRoute() {
            super(Command.TARGET, "team {" + TEAM_PARAM + "}");
            describeTextOption(TEAM_PARAM, "team to target for all queries");
        }

        @Override
        public Object route(Map<String, String> params) {
            String teamName = params.get(TEAM_PARAM);

            return setQueryTarget(new TargetInputDto(TargetType.TEAM, teamName));
        }
    }


}

