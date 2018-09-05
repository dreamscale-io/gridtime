package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.*;
import com.dreamscale.htmflow.api.team.TeamInputDto;
import com.dreamscale.htmflow.api.team.TeamDto;
import com.dreamscale.htmflow.api.team.TeamMemberDto;
import com.dreamscale.htmflow.api.team.TeamMembersToAddInputDto;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Get a list of all the members in the Organization
     */
    @GetMapping("/{id}" + ResourcePaths.MEMBER_PATH)
    public List<OrgMemberStatusDto> getMembers(@PathVariable("id") String organizationId) {
        RequestContext context = RequestContext.get();
        UUID organizationUuid = UUID.fromString(organizationId);
        return organizationService.getMembersForOrganization(organizationUuid, context.getMasterAccountId());
    }

    /**
     * Creates a new member within an organization associated with the specified Jira email
     * A new master account will be created using the email, along with a product activation code
     */
    @PostMapping("/{id}" + ResourcePaths.MEMBER_PATH)
    public MemberRegistrationDetailsDto registerMember(@PathVariable("id") String organizationId,
                                                       @RequestBody MembershipInputDto membershipInputDto) {

        return organizationService.registerMember(UUID.fromString(organizationId), membershipInputDto);
    }

    /**
     * Creates a new team for organizing members, affecting what members you see on your side panel
     */
    @PostMapping("/{id}" + ResourcePaths.TEAM_PATH)
    public TeamDto createTeam(@PathVariable("id") String organizationId, @RequestBody TeamInputDto teamInputDto) {

        return teamService.createTeam(UUID.fromString(organizationId), teamInputDto.getName());
    }

    /**
     * Add one or more members to an existing team
     */
    @PostMapping("/{orgId}" + ResourcePaths.TEAM_PATH + "/{id}" + ResourcePaths.MEMBER_PATH)
    public List<TeamMemberDto> addMemberToTeam(@PathVariable("orgId") String organizationId,
                                               @PathVariable("id") String teamId,
                                               @RequestBody TeamMembersToAddInputDto teamMemberInputDto) {

        return teamService.addMembersToTeam(UUID.fromString(organizationId), UUID.fromString(teamId), teamMemberInputDto);
    }


}
