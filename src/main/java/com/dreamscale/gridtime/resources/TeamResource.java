package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.TeamWithMembersDto;
import com.dreamscale.gridtime.api.team.HomeTeamConfigInputDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamMemberOldDto;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamCapability;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.TEAM_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TeamResource {

    @Autowired
    private OrganizationCapability organizationCapability;

    @Autowired
    private TeamCapability teamCapability;


    /**
     * Gets all the teams the invoking member is participating in, for the active organization
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping()
    public List<TeamDto> getAllMyTeams() {

        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.getMyParticipatingTeamsWithMembers(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the currently configured default home team
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.HOME_PATH)
    public TeamDto getMyHomeTeam() {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.getMyActiveTeam(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Sets the currently configured default home team
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.HOME_PATH )
    public TeamDto setMyHomeTeam(@RequestBody HomeTeamConfigInputDto homeTeamConfigInputDto) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.setMyHomeTeam(invokingMember.getOrganizationId(), invokingMember.getId(), homeTeamConfigInputDto.getHomeTeam());
    }

    /**
     * Creates the team specified by the team name, that includes one member, the creator of the team.
     *
     * Creates a new TeamCircuit for the team to coordinate work, that will be accessible at /circuit/{teamName}
     *
     * Additional members can be invited with their username once the team is created
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( "/{teamName}")
    public TeamDto createTeam(@PathVariable("teamName") String teamName) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.createTeam(invokingMember.getOrganizationId(), invokingMember.getId(), teamName);
    }

    /**
     * Retrieves a team and all it's members.
     *
     * You can only get the list of team members if you are a member of the team.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping( "/{teamName}")
    public TeamWithMembersDto getTeam(@PathVariable("teamName") String teamName) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.getTeam(invokingMember.getOrganizationId(), invokingMember.getId(), teamName);
    }



}
