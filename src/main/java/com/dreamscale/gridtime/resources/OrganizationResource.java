package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamInputDto;
import com.dreamscale.gridtime.api.team.TeamMemberDto;
import com.dreamscale.gridtime.api.team.TeamMembersToAddInputDto;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.directory.OrganizationMembershipCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamDirectoryCapability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = ResourcePaths.ORGANIZATION_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class OrganizationResource {

    @Autowired
    private OrganizationMembershipCapability organizationMembership;

    @Autowired
    private TeamDirectoryCapability teamDirectoryCapability;

    /**
     * Creates a new organization with the specified name, and Jira connection information
     * returns status of Jira connectivity, and a sharable invite link for inviting members to the Org
     *
     * @param orgInputDto
     * @return OrganizationDto
     */
    // TODO: @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public OrganizationDto createOrganization(@RequestBody OrganizationInputDto orgInputDto) {

        return organizationMembership.createOrganization(orgInputDto);
    }

    /**
     * Use an invitation token to decode the organization associated with the invite,
     * if the token is valid, the organization object will be returned, otherwise 404
     *
     * @param inviteToken
     * @return
     */
    @GetMapping(ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH)
    public OrganizationDto decodeInvitation(@RequestParam("token") String inviteToken) {

       return organizationMembership.decodeInvitation(inviteToken);
    }

    /**
     * Creates a new member within an organization associated with the specified Jira email
     * A new root account will be created using the email, along with a product activation code
     */
    // TODO: @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.MEMBER_PATH)
    public MemberRegistrationDetailsDto registerMember(@PathVariable("id") String organizationId,
                                                       @RequestBody MembershipInputDto membershipInputDto) {

        return organizationMembership.registerMember(UUID.fromString(organizationId), membershipInputDto);
    }

    /**
     * Creates a new team for organizing members, affecting what members you see on your side panel
     */
    // TODO: @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.TEAM_PATH)
    public TeamDto createTeam(@PathVariable("id") String organizationId, @RequestBody TeamInputDto teamInputDto) {

        return teamDirectoryCapability.createTeam(UUID.fromString(organizationId), teamInputDto.getName());
    }

    /**
     * Add one or more members to an existing team
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{orgId}" + ResourcePaths.TEAM_PATH + "/{teamId}" + ResourcePaths.MEMBER_PATH)
    public List<TeamMemberDto> addMemberToTeam(@PathVariable("orgId") String organizationId,
                                               @PathVariable("teamId") String teamId,
                                               @RequestBody TeamMembersToAddInputDto teamMemberInputDto) {

        return teamDirectoryCapability.addMembersToTeam(UUID.fromString(organizationId), UUID.fromString(teamId), teamMemberInputDto.getMemberIds());
    }

    /**
     * Get status of Me, and all my team members
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.TEAM_PATH )
    public TeamWithMembersDto getMeAndMyTeam() {
        RequestContext context = RequestContext.get();
        return teamDirectoryCapability.getMeAndMyTeam(context.getRootAccountId());
    }

    /**
     * Get all the teams for the Organization
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{orgId}" + ResourcePaths.TEAM_PATH)
    public List<TeamDto> getTeams(@PathVariable("orgId") String organizationId) {

        return teamDirectoryCapability.getTeams(UUID.fromString(organizationId));
    }

    /**
     * Get all my teams within the Organization
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{orgId}" + ResourcePaths.TEAM_PATH + ResourcePaths.ME_PATH)
    public List<TeamDto> getMyTeams(@PathVariable("orgId") String organizationId) {
        RequestContext context = RequestContext.get();
        return teamDirectoryCapability.getMyTeams(UUID.fromString(organizationId), context.getRootAccountId());
    }

    /**
     * Get all my team members within a team, and their status
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{orgId}"  + ResourcePaths.TEAM_PATH + "/{teamId}" + ResourcePaths.MEMBER_PATH)
    public List<TeamMemberWorkStatusDto> getStatusOfTeamMembers(@PathVariable("orgId") String organizationId,  @PathVariable("teamId") String teamId) {

        return teamDirectoryCapability.getStatusOfTeamMembers(UUID.fromString(organizationId), UUID.fromString(teamId));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TEAM_PATH + ResourcePaths.MEMBER_PATH )
    public MemberRegistrationDetailsDto addMemberToMyTeam(@RequestBody  String newMemberEmail) {
        RequestContext context = RequestContext.get();
        return teamDirectoryCapability.addMemberToMyTeam(context.getRootAccountId(), newMemberEmail);
    }


}
