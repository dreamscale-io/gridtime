package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamInputDto;
import com.dreamscale.gridtime.api.team.TeamMemberDto;
import com.dreamscale.gridtime.api.team.TeamMembersToAddInputDto;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.directory.OrganizationMembershipCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamMembershipCapability;
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
    private TeamMembershipCapability teamMembership;

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
     * Use an invitation key to decode the organization associated with the invite,
     * if the key is valid, the organization object will be returned, otherwise 404
     *
     * @param invitationKey
     * @return
     */
    @GetMapping(ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH)
    public OrganizationDto decodeInvitation(@RequestParam("invitationKey") String invitationKey) {

       return organizationMembership.decodeInvitation(invitationKey);
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



}
