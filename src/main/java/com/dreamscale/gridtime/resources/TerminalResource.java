package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.terminal.RunCommandInputDto;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRouteRegistry;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.TERMINAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TerminalResource {

    @Autowired
    OrganizationCapability organizationCapability;

    @Autowired
    TerminalRouteRegistry terminalRouteRegistry;

    /**
     * Run a specific command on the grid and return the result synchronously as a TalkMessageDto
     *
     * @see com.dreamscale.gridtime.api.terminal.Command for the available command types
     *
     * @param runCommandInputDto RunCommandInputDto
     * @return TalkMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.RUN_PATH)
    public TalkMessageDto runCommand(@RequestBody RunCommandInputDto runCommandInputDto) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("runCommand, user={}", invokingMember.getBestAvailableName());

        return terminalRouteRegistry.routeCommand(invokingMember.getOrganizationId(), invokingMember.getId(), runCommandInputDto);
    }

}
