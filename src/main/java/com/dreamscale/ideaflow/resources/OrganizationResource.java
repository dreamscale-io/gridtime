package com.dreamscale.ideaflow.resources;

import com.dreamscale.ideaflow.api.ResourcePaths;
import com.dreamscale.ideaflow.api.organization.*;
import com.dreamscale.ideaflow.api.team.TeamInputDto;
import com.dreamscale.ideaflow.api.team.TeamDto;
import com.dreamscale.ideaflow.api.team.TeamMemberDto;
import com.dreamscale.ideaflow.api.team.TeamMembersToAddInputDto;
import com.dreamscale.ideaflow.core.security.RequestContext;
import com.dreamscale.ideaflow.core.service.OrganizationService;
import com.dreamscale.ideaflow.core.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = ResourcePaths.ORGANIZATION_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class OrganizationResource {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private TeamService teamService;

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

        return organizationService.createOrganization(orgInputDto);
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

       return organizationService.decodeInvitation(inviteToken);
    }

    /**
     * Creates a new member within an organization associated with the specified Jira email
     * A new master account will be created using the email, along with a product activation code
     */
    // TODO: @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.MEMBER_PATH)
    public MemberRegistrationDetailsDto registerMember(@PathVariable("id") String organizationId,
                                                       @RequestBody MembershipInputDto membershipInputDto) {

        return organizationService.registerMember(UUID.fromString(organizationId), membershipInputDto);
    }

    /**
     * Creates a new team for organizing members, affecting what members you see on your side panel
     */
    // TODO: @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{id}" + ResourcePaths.TEAM_PATH)
    public TeamDto createTeam(@PathVariable("id") String organizationId, @RequestBody TeamInputDto teamInputDto) {

        return teamService.createTeam(UUID.fromString(organizationId), teamInputDto.getName());
    }

    /**
     * Add one or more members to an existing team
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/{orgId}" + ResourcePaths.TEAM_PATH + "/{teamId}" + ResourcePaths.MEMBER_PATH)
    public List<TeamMemberDto> addMemberToTeam(@PathVariable("orgId") String organizationId,
                                               @PathVariable("teamId") String teamId,
                                               @RequestBody TeamMembersToAddInputDto teamMemberInputDto) {

        return teamService.addMembersToTeam(UUID.fromString(organizationId), UUID.fromString(teamId), teamMemberInputDto.getMemberIds());
    }

    /**
     * Get status of Me, and all my team members
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.TEAM_PATH )
    public TeamWithMembersDto getMeAndMyTeam() {
        RequestContext context = RequestContext.get();
        return teamService.getMeAndMyTeam(context.getMasterAccountId());
    }

    /**
     * Get all the teams for the Organization
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{orgId}" + ResourcePaths.TEAM_PATH)
    public List<TeamDto> getTeams(@PathVariable("orgId") String organizationId) {

        return teamService.getTeams(UUID.fromString(organizationId));
    }

    /**
     * Get all my teams within the Organization
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{orgId}" + ResourcePaths.TEAM_PATH + ResourcePaths.ME_PATH)
    public List<TeamDto> getMyTeams(@PathVariable("orgId") String organizationId) {
        RequestContext context = RequestContext.get();
        return teamService.getMyTeams(UUID.fromString(organizationId), context.getMasterAccountId());
    }

    /**
     * Get all my team members within a team, and their status
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{orgId}"  + ResourcePaths.TEAM_PATH + "/{teamId}" + ResourcePaths.MEMBER_PATH)
    public List<TeamMemberWorkStatusDto> getStatusOfTeamMembers(@PathVariable("orgId") String organizationId,  @PathVariable("teamId") String teamId) {

        return teamService.getStatusOfTeamMembers(UUID.fromString(organizationId), UUID.fromString(teamId));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TEAM_PATH + ResourcePaths.MEMBER_PATH )
    public MemberRegistrationDetailsDto addMemberToMyTeam(@RequestBody  String newMemberEmail) {
        RequestContext context = RequestContext.get();
        return teamService.addMemberToMyTeam(context.getMasterAccountId(), newMemberEmail);
    }


}
