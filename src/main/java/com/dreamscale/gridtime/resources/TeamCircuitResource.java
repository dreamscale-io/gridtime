package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.DescriptionInputDto;
import com.dreamscale.gridtime.api.circuit.TagsInputDto;
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
        log.info("getMyTeamCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

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
        log.info("getTeamCircuitByName, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

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
        log.info("createTeamCircuitRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return teamCircuitOperator.createTeamCircuitRoom(invokingMember.getOrganizationId(), teamName, roomName);
    }


    /**
     * Returns the details of the specified team circuit talk room
     *
     * @return TeamCircuitRoomDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}")
    public TeamCircuitRoomDto getTeamCircuitRoom(@PathVariable("teamName") String teamName, @PathVariable("roomName") String roomName ) {
        RequestContext context = RequestContext.get();
        log.info("getTeamCircuitRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return teamCircuitOperator.getTeamCircuitRoom(invokingMember.getOrganizationId(), teamName, roomName);
    }

    /**
     * Closes an existing team circuit room so it no longer shows up on the Team's talk rooms list
     *
     * @return TeamCircuitRoomDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.CLOSE_PATH)
    public TeamCircuitRoomDto closeTeamCircuitRoom(@PathVariable("teamName") String teamName, @PathVariable("roomName") String roomName ) {
        RequestContext context = RequestContext.get();
        log.info("closeTeamCircuitRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return teamCircuitOperator.closeTeamCircuitRoom(invokingMember.getOrganizationId(), teamName, roomName);
    }

    /**
     * Saves a new description for the team circuit room
     *
     * @return TeamCircuitRoomDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.PROPERTY_PATH + ResourcePaths.DESCRIPTION_PATH)
    public TeamCircuitRoomDto saveDescriptionForTeamCircuitRoom(@PathVariable("teamName") String teamName, @PathVariable("roomName") String roomName, @RequestBody DescriptionInputDto descriptionInputDto ) {
        RequestContext context = RequestContext.get();
        log.info("saveDescriptionForTeamCircuitRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return teamCircuitOperator.saveDescriptionForTeamCircuitRoom(invokingMember.getOrganizationId(), teamName, roomName, descriptionInputDto);
    }

    /**
     * Saves a new description for the team circuit room
     *
     * @return TeamCircuitRoomDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.PROPERTY_PATH + ResourcePaths.TAGS_PATH)
    public TeamCircuitRoomDto saveTagsForTeamCircuitRoom(@PathVariable("teamName") String teamName, @PathVariable("roomName") String roomName, @RequestBody TagsInputDto tagsInputDto ) {
        RequestContext context = RequestContext.get();
        log.info("saveTagsForTeamCircuitRoom, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());

        return teamCircuitOperator.saveTagsForTeamCircuitRoom(invokingMember.getOrganizationId(), teamName, roomName, tagsInputDto);
    }
}
