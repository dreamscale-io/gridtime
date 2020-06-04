package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.ORGANIZATION_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class OrganizationResource {

    @Autowired
    private OrganizationCapability organizationCapability;


    /**
     * Gets the active organization for the logged in user
     *
     * @return OrganizationDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping()
    public OrganizationDto getMyActiveOrganization() {

        RequestContext context = RequestContext.get();
        log.info("getMyActiveOrganization, user={}", context.getRootAccountId());

        return organizationCapability.getActiveOrganization(context.getRootAccountId());
    }

    /**
     * Gets all organizations that the logged in user is participating in
     *
     * @return List<OrganizationDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    public List<OrganizationDto> getMyParticipatingOrganizations() {

        RequestContext context = RequestContext.get();
        log.info("getMyParticipatingOrganizations, user={}", context.getRootAccountId());

        return organizationCapability.getParticipatingOrganizations(context.getRootAccountId());
    }


    /**
     * Retrieves all the members of the active organization.
     *
     * @return List<MemberRegistrationDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping( ResourcePaths.MEMBER_PATH )
    public List<MemberDetailsDto> getMembersOfActiveOrganization() {

        RequestContext context = RequestContext.get();

        log.info("getMembersOfActiveOrganization, user={}", context.getRootAccountId());

        return organizationCapability.getMembersOfActiveOrganization(context.getRootAccountId());
    }

    /**
     * Removes a member of the active organization.
     *
     * This member will still be available to support old data references created by the user,
     * but the member's root account will be detached from accessing this organization.
     *
     * The seat for this user's license will be recovered by the org, and usable for other members.
     *
     * Must be the *owner* of the organization to use this API
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.MEMBER_PATH + "/{memberId}" + ResourcePaths.REMOVE_PATH)
    public SimpleStatusDto removeMember(@PathVariable("memberId") String memberIdStr) {
        RequestContext context = RequestContext.get();

        UUID memberId = UUID.fromString(memberIdStr);

        log.info("removeMember, user={}", context.getRootAccountId());

        return organizationCapability.removeMember(context.getRootAccountId(), memberId);
    }


    /**
     * Configures the JIRA capability associated with the org, to support auto-completion of journal tasks
     * with JIRA ticket numbers, and automatic filling in of Jira task descriptions.
     *
     * Must be the owner or a moderator of the organization, to use this API
     *
     * @return SimpleStatusDto returns VALID, or FAILED depending on the Jira connection
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping( ResourcePaths.CONFIG_PATH + ResourcePaths.JIRA_PATH)
    public SimpleStatusDto updateJiraConfiguration(@RequestBody JiraConfigDto jiraConfigDto) {

        RequestContext context = RequestContext.get();

        log.info("updateJiraConfiguration, user={}", context.getRootAccountId());

        return organizationCapability.updateJiraConfiguration(context.getRootAccountId(), jiraConfigDto);
    }


    /**
     * Retrieves the existing Jira configuration information for the organization
     *
     * Must be the owner or a moderator of the organization, to use this API
     *
     * @return JiraConfigDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping( ResourcePaths.CONFIG_PATH + ResourcePaths.JIRA_PATH)
    public JiraConfigDto getJiraConfiguration() {

        RequestContext context = RequestContext.get();

        log.info("getJiraConfiguration, user={}", context.getRootAccountId());

        return organizationCapability.getJiraConfiguration(context.getRootAccountId());
    }


}
