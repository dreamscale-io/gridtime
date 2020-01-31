package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto;
import com.dreamscale.gridtime.api.team.TeamCircuitDto;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.OrganizationService;
import com.dreamscale.gridtime.core.service.TeamCircuitOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.CIRCUIT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TeamCircuitResource {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    TeamCircuitOperator teamCircuitOperator;


    /**
     * Gets the team circuit for the active user, and all member statuses
     *
     * @return TeamCircuitDto
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.HOME_PATH)
    public TeamCircuitDto getMyTeamCircuit() {

        RequestContext context = RequestContext.get();
        log.info("getMyTeamCircuit, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return teamCircuitOperator.getMyPrimaryTeamCircuit(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    /**
     * Gets the specified team circuit for the active user, and all member statuses
     *
     * Bound within the scope of an organization
     *
     * @return TeamCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{teamName}")
    public TeamCircuitDto getTeamCircuitByName(@PathVariable("teamName") String teamName) {
        RequestContext context = RequestContext.get();
        log.info("getTeamCircuitByName, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return teamCircuitOperator.getTeamCircuitByOrganizationAndName(invokingMember.getOrganizationId(), teamName);
    }

    /**
     * Creates a new talk room, under the scope of the current team
     *
     * @return CircuitRoomDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}")
    public TeamCircuitRoomDto createTeamCircuitRoom(@PathVariable("teamName") String teamName, @PathVariable("roomName") String roomName ) {
        RequestContext context = RequestContext.get();
        log.info("createTeamCircuitRoom, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return teamCircuitOperator.createTeamCircuitRoom(invokingMember.getOrganizationId(), teamName, roomName);
    }


    /**
     * Returns the details of the specified team circuit talk room
     *
     * @return CircuitRoomDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}")
    public TeamCircuitRoomDto getTeamCircuitRoom(@PathVariable("teamName") String teamName, @PathVariable("roomName") String roomName ) {
        RequestContext context = RequestContext.get();
        log.info("getTeamCircuitRoom, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return teamCircuitOperator.getTeamCircuitRoom(invokingMember.getOrganizationId(), teamName, roomName);
    }

    /**
     * Creates a new talk room, under the scope of the current team
     *
     * @return CircuitRoomDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.CLOSE_PATH)
    public TeamCircuitRoomDto closeTeamCircuitRoom(@PathVariable("teamName") String teamName, @PathVariable("roomName") String roomName ) {
        RequestContext context = RequestContext.get();
        log.info("closeTeamCircuitRoom, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return teamCircuitOperator.closeTeamCircuitRoom(invokingMember.getOrganizationId(), teamName, roomName);
    }


}
