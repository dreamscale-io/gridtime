package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.api.terminal.CommandManualDto;
import com.dreamscale.gridtime.api.terminal.CommandManualPageDto;
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
@RequestMapping(path = ResourcePaths.TERMINAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
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
    @PostMapping(ResourcePaths.CIRCUIT_PATH)
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
    @GetMapping(ResourcePaths.CIRCUIT_PATH + "/{circuitName}" )
    public TerminalCircuitDto getCircuit(@PathVariable("circuitName") String circuitName) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("getCircuit, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.getCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Run a specific terminal command and returns the result via circuit (also response)
     *
     * @see com.dreamscale.gridtime.api.terminal.Command for the available command types
     *
     * @param commandInputDto CommandInputDto
     * @return TalkMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.CIRCUIT_PATH + "/{circuitName}" + ResourcePaths.RUN_PATH)
    public TalkMessageDto runCommand(@PathVariable("circuitName") String circuitName, @RequestBody CommandInputDto commandInputDto) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("run, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.runCommand(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName, commandInputDto);
    }

    /**
     * Join an existing terminal circuit, must be a member of the org
     *
     * @return TalkMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.CIRCUIT_PATH + "/{circuitName}" + ResourcePaths.JOIN_PATH)
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
    @PostMapping(ResourcePaths.CIRCUIT_PATH + "/{circuitName}" + ResourcePaths.LEAVE_PATH)
    public SimpleStatusDto leaveCircuit(@PathVariable("circuitName") String circuitName) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("leaveCircuit, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.leaveCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * Returns the entire manual for all available registered terminal commands
     *
     * @return CommandManualDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MANUAL_PATH )
    public CommandManualDto getManual() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("getCommandManual, user={}", invokingMember.getBestAvailableName());

        return terminalCapability.getManual(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    /**
     * Gets help information summary for the specified command
     *
     * All variations of the command across all command groups will be compiled together into one help.
     * For example, all the varations of the 'SHARE' command, will show you how to share different sorts of things.
     *
     * @see com.dreamscale.gridtime.api.terminal.Command for the available command types
     *
     * @param commandName
     * @param commandName Command
     * @return CommandManualPageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MANUAL_PATH + ResourcePaths.COMMAND_PATH + "/{commandName}")
        public CommandManualPageDto getManualPageForCommand(@PathVariable("commandName") String commandName) {
            RequestContext context = RequestContext.get();
            OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

            log.info("getCommandManualPage, user={}", invokingMember.getBestAvailableName());

            Command command = Command.fromString(commandName);

            return terminalCapability.getManualPage(invokingMember.getOrganizationId(), invokingMember.getId(), command);
        }


        /**
         * Gets help information summary for the specified Activity Context
         *
         * Activity Contexts are coherent groups of commands that all pertain to one activity type,
         * for example, all the commands around the 'PROJECT' activity type, allow you to do things with projects
         *
         * @see ActivityContext
         *
         * @param activityContextName
         * @return CommandManualPageDto
         */
        @PreAuthorize("hasRole('ROLE_USER')")
        @GetMapping(ResourcePaths.MANUAL_PATH + ResourcePaths.CONTEXT_PATH + "/{activityContextName}")
        public CommandManualPageDto getManualPageForContext(@PathVariable("activityContextName") String activityContextName) {
            RequestContext context = RequestContext.get();
            OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

            log.info("getCommandManualPage, user={}", invokingMember.getBestAvailableName());

            ActivityContext activityContext = ActivityContext.fromString(activityContextName);

            return terminalCapability.getManualPage(invokingMember.getOrganizationId(), invokingMember.getId(), activityContext);
        }

    }

