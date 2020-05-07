package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.TeamWithMembersDto;
import com.dreamscale.gridtime.api.team.HomeTeamConfigInputDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamMemberDto;
import com.dreamscale.gridtime.core.capability.operator.TeamCircuitOperator;
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
     * Get all the teams for the Organization
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping()
    public List<TeamDto> getAllTeams() {

        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.getAllTeams(invokingMember.getOrganizationId());
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
     * Gets all the teams the invoking member is a participating in
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH )
    public List<TeamDto> getMyParticipatingTeams() {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.getMyParticipatingTeams(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Adds an existing member to the team using their username
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.USERNAME_PATH + "/{userName}")

    public TeamMemberDto addMemberToTeam(@PathVariable("teamName") String teamName,
                                         @PathVariable("userName") String userName) {

        RequestContext context = RequestContext.get();
        log.info("addMemberToTeam, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.addMemberToTeam(invokingMember.getOrganizationId(), invokingMember.getId(), teamName, userName);
    }

    /**
     * Adds an existing member to the team using their memberId
     *
     * This API can be used, even when no username has been configured on the account
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.MEMBER_PATH + "/{memberId}")

    public TeamMemberDto addMemberToTeamWithMemberId(@PathVariable("teamName") String teamName,
                                         @PathVariable("memberId") String memberId) {

        RequestContext context = RequestContext.get();
        log.info("addMemberToTeamWithMemberId, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.addMemberToTeamWithMemberId(invokingMember.getOrganizationId(), invokingMember.getId(), teamName, UUID.fromString(memberId));
    }


    /**
     * Removes the specified member from the team using their username
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.USERNAME_PATH + "/{userName}" + ResourcePaths.REMOVE_PATH)

    public TeamMemberDto removeMemberFromTeam(@PathVariable("teamName") String teamName,
                                         @PathVariable("userName") String userName) {

        RequestContext context = RequestContext.get();
        log.info("removeMemberFromTeam, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.removeMemberFromTeam(invokingMember.getOrganizationId(), invokingMember.getId(), teamName, userName);
    }


    /**
     * Removes the specified member from the team using their username
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{teamName}" + ResourcePaths.MEMBER_PATH + "/{memberId}" + ResourcePaths.REMOVE_PATH)

    public TeamMemberDto removeMemberFromTeamWithMemberId(@PathVariable("teamName") String teamName,
                                              @PathVariable("memberId") String memberId) {

        RequestContext context = RequestContext.get();
        log.info("removeMemberFromTeamWithMemberId, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamCapability.removeMemberFromTeamWithMemberId(invokingMember.getOrganizationId(), invokingMember.getId(), teamName, UUID.fromString(memberId));
    }
}
