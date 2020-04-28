package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
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
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.ACTIVE_PATH)
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
    public List<MemberRegistrationDto> getMembersOfActiveOrganization() {

        RequestContext context = RequestContext.get();

        log.info("getMembersOfActiveOrganization, user={}", context.getRootAccountId());

        return organizationCapability.getMembersOfActiveOrganization(context.getRootAccountId());
    }

    /**
     * Retrieves all the members of the active organization.
     *
     * @return List<MemberRegistrationDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping( ResourcePaths.MEMBER_PATH + "/{memberId}" )
    public MemberRegistrationDto getMemberOfActiveOrganization(@PathVariable("memberId") String memberIdStr) {

        RequestContext context = RequestContext.get();

        UUID memberId = UUID.fromString(memberIdStr);

        log.info("getMemberOfActiveOrganization, user={}", context.getRootAccountId());

        return organizationCapability.getMemberOfActiveOrganization(context.getRootAccountId(), memberId);
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
    public SimpleStatusDto removeOrganizationMember(@PathVariable("memberId") String memberIdStr) {
        RequestContext context = RequestContext.get();

        UUID memberId = UUID.fromString(memberIdStr);

        log.info("removeOrganizationMember, user={}", context.getRootAccountId());

        return organizationCapability.removeOrganizationMember(context.getRootAccountId(), memberId);
    }


    /**
     * Joins an existing organization via the Invitation Token and an org email.
     *
     * If this organization requires validated emails within the domain, this call will fail if the email
     * provided doesn't match the domain specified for the organization.
     *
     * If this requirement is passed, an email is sent to the provided email with a validation code.
     * Once the email is validated, the user will be added to the org.
     *
     * @return SimpleStatusDto SENT if the validation email is sent
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.JOIN_PATH)
    public SimpleStatusDto joinOrganizationWithInvitationAndEmail(@RequestBody JoinRequestInputDto joinRequestInputDto) {

        RequestContext context = RequestContext.get();
        log.info("joinOrganizationWithInvitationAndEmail, user={}", context.getRootAccountId());

        return organizationCapability.joinOrganizationWithInvitationAndEmail(context.getRootAccountId(), joinRequestInputDto);
    }


    /**
     * Validates the organization member has access to the specified email,
     * if this is a requirement for the org, then officially adds the member to the org
     *
     * @return SimpleStatusDto SUCCESS if the change succeeded, FAILED if the validation code is expired or cant be found
     */

    @PreAuthorize("permitAll")
    @PostMapping(ResourcePaths.JOIN_PATH + ResourcePaths.EMAIL_PATH + ResourcePaths.VALIDATE_PATH)
    SimpleStatusDto validateMemberEmailAndJoin(@RequestParam("validationCode") String validationCode) {

        log.info("validateMemberEmailAndJoin" );

        return organizationCapability.validateMemberEmailAndJoin(validationCode);
    }


    /**
     * Configures the JIRA capability associated with the org, to support auto-completion of journal tasks
     * with JIRA ticket numbers, and automatic filling in of Jira task descriptions.
     *
     * Jira projects should be added for each Jira project that you plan to associate work.
     *
     * Each project configuration, can have a different Idea Flow data collection spec.
     *
     * Must be the *owner* of the organization to use this API
     *
     * @return SimpleStatusDto
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
     * Must be the *owner* of the organization to use this API
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



    //paymentMethod.id from stripe, from create payment method response

    //customer.id from stripe, when creating customer and subscription

    //server needs to create a customer in stripe, and then associate the paymentMethod.id for default payment method for invoices

    //server code for creating a customer object

    //okay, so I'm on the website, and I want to buy TorchieTalk for my org, with a number of seats.


    //first thing I need to do, is make an account.

    //set this up like a wizard...

    //either create an accountt, or login under an existting account.

    //Then from witthin your accountt, you've got a "create organization" button.

    //I setup the org name, domain, paymentMethod.id for the subscription.

    //and whether I want to require members to be in the domain (check box)

    //Then your org, gives you an "Invitation Key"

    //To join your organization, they will need to press the "Join Organization" button.

    //they go to their terminal, and do "join orgname"

    //it asks "What is the Invitation Key?"

    //Paste the Key.

    //You need to have a validated email within this organization's domain

    //>joe@domain.com

    //Please validate your email.

    //You are now a member of orgname.

    //Create a team, get an invitation key.

    //You are now a member of team.

    //this email doesn't count as a torchie account, it's the billing email, won't count as a seat.

    //so we create a customer in stripe for the organization, attached to the payment, setup the org.

    //and then now, send the

    //okay now, send an org invitation to token to the specified billing email.

    // now all the people need to create their accounts, validate their emails...

}
