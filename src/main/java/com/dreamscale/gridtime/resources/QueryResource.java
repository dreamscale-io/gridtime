package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.api.query.QueryInputDto;
import com.dreamscale.gridtime.api.query.TargetInputDto;
import com.dreamscale.gridtime.api.query.TargetType;
import com.dreamscale.gridtime.api.query.TimeScope;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.query.QueryCapability;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.QUERY_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class QueryResource {

    @Autowired
    OrganizationCapability organizationCapability;

    @Autowired
    QueryCapability queryCapability;

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


}

