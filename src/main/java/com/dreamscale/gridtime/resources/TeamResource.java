package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.team.HomeTeamConfigInputDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamLinkDto;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<TeamDto> getMyTeams() {

        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.getAllMyParticipatingTeamsWithMembers(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    /**
     * Gets all the teams within the scope of the active organization
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ALL_PATH)
    public List<TeamLinkDto> getAllTeamsWithinOrg() {

        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.getLinksToAllOpenTeams(invokingMember.getOrganizationId());
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
    public SimpleStatusDto setMyHomeTeam(@RequestBody HomeTeamConfigInputDto homeTeamConfigInputDto) {
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
     * Join the team specified by the team name, must be an existing team within the current org context,
     * that is an "open" team, otherwise the only way to join is via an invite.
     *
     * For the public organization, all teams are open teams and can be joined with join
     *
     * Creates a new TeamCircuit for the team to coordinate work, that will be accessible at /circuit/{teamName}
     *
     * Additional members can be invited with their username once the team is created
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( "/{teamName}" + ResourcePaths.JOIN_PATH)
    public SimpleStatusDto joinTeam(@PathVariable("teamName") String teamName) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.joinTeam(invokingMember.getOrganizationId(), invokingMember.getId(), teamName);
    }

    /**
     * Retrieves a team and all it's members.
     *
     * You can only get the list of team members if you are a member of the team.
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping( "/{teamName}")
    public TeamDto getTeam(@PathVariable("teamName") String teamName) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.getTeam(invokingMember.getOrganizationId(), invokingMember.getId(), teamName);
    }

}
