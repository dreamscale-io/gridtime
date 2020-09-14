package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.terminal.*;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.terminal.TerminalCapability;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.CIRCUIT_PATH + ResourcePaths.TERMINAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TerminalResource {

    @Autowired
    OrganizationCapability organizationCapability;


    @Autowired
    TerminalCapability terminalCapability;

    /**
     * Create a new TerminalCircuit
     *
     * All command responses over this terminal circuit will be responded to via the talk message room
     * @return TerminalCircuitDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping()
    public TerminalCircuitDto createCircuit() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("createCircuit, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.createCircuit(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Get an existing terminal circuit by name, scoped per organization
     *
     * @return TerminalCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{circuitName}" )
    public TerminalCircuitDto getCircuit(@PathVariable("circuitName") String circuitName) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("getCircuit, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.getCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Run a specific terminal command and returns the result via terminal circuit TalkMessageDto
     *
     * @see com.dreamscale.gridtime.api.terminal.Command for the available command types
     *
     * @param commandInputDto CommandInputDto
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{circuitName}" + ResourcePaths.RUN_PATH)
    public SimpleStatusDto run(@PathVariable("circuitName") String circuitName, @RequestBody CommandInputDto commandInputDto) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("run, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.run(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName, commandInputDto);
    }

    /**
     * Set an environment variable within the context of the current session
     **
     * @param environmentParamInputDto EnvironmentParamInputDto
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{circuitName}" + ResourcePaths.SET_PATH)
    public SimpleStatusDto set(@PathVariable("circuitName") String circuitName, @RequestBody EnvironmentParamInputDto environmentParamInputDto) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("set, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.setEnvironmentParam(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName, environmentParamInputDto);
    }

    /**
     * Join an existing terminal circuit, must be a member of the org
     *
     * @return TalkMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{circuitName}" + ResourcePaths.JOIN_PATH)
    public SimpleStatusDto joinCircuit(@PathVariable("circuitName") String circuitName) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("joinCircuit, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.joinCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Leave an existing terminal circuit, must be a member of the org
     *
     * @return TalkMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{circuitName}" + ResourcePaths.LEAVE_PATH)
    public SimpleStatusDto leaveCircuit(@PathVariable("circuitName") String circuitName) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("leaveCircuit, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.leaveCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


}
