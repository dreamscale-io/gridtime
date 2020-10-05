package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.api.terminal.CommandManualDto;
import com.dreamscale.gridtime.api.terminal.CommandManualPageDto;
import com.dreamscale.gridtime.api.terminal.*;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
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
     * @param commandInputDto CommandInputDto
     * @return TalkMessageDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.RUN_PATH)
    public TalkMessageDto runCommand(@RequestBody CommandInputDto commandInputDto) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        log.info("runCommand, user={}", invokingMember.getBestAvailableName());

        return terminalRouteRegistry.routeCommand(invokingMember.getOrganizationId(), invokingMember.getId(), commandInputDto);
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

        return terminalRouteRegistry.getManual(invokingMember.getOrganizationId(), invokingMember.getId());
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

            return terminalRouteRegistry.getManualPage(invokingMember.getOrganizationId(), invokingMember.getId(), command);
        }


        /**
         * Gets help information summary for the specified command group
         *
         * Command groups are coherent groups of commands that all pertain to one thing,
         * for example, all the commands around the 'PROJECT' command group, allow you to do things with projects
         *
         * @see com.dreamscale.gridtime.api.terminal.CommandGroup for the available command groups
         *
         * @param commandGroupName CommandGroup
         * @return CommandManualPageDto
         */
        @PreAuthorize("hasRole('ROLE_USER')")
        @GetMapping(ResourcePaths.MANUAL_PATH + ResourcePaths.GROUP_PATH + "/{commandGroupName}")
        public CommandManualPageDto getManualPageForGroup(@PathVariable("commandGroupName") String commandGroupName) {
            RequestContext context = RequestContext.get();
            OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

            log.info("getCommandManualPage, user={}", invokingMember.getBestAvailableName());

            CommandGroup group = CommandGroup.fromString(commandGroupName);

            return terminalRouteRegistry.getManualPage(invokingMember.getOrganizationId(), invokingMember.getId(), group);
        }

    }

