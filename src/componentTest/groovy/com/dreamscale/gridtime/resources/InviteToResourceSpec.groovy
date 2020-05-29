package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.EmailInputDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UsernameInputDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.invitation.InvitationKeyDto
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto
import com.dreamscale.gridtime.api.organization.MemberDetailsDto
import com.dreamscale.gridtime.api.organization.OrganizationDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.InvitationClient
import com.dreamscale.gridtime.client.InviteToClient
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.client.SubscriptionClient
import com.dreamscale.gridtime.client.TeamClient
import com.dreamscale.gridtime.core.capability.integration.EmailCapability
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import com.dreamscale.gridtime.core.domain.member.*
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class InviteToResourceSpec extends Specification {

    @Autowired
    InvitationClient invitationClient

    @Autowired
    InviteToClient inviteToClient

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    SubscriptionClient subscriptionClient

    @Autowired
    TeamClient teamClient

    @Autowired
    AccountClient accountClient

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
    RootAccountRepository masterAccountRepository

    @Autowired
    TeamRepository teamRepository

    @Autowired
    TeamMemberRepository teamMemberRepository

    @Autowired
    EmailCapability mockEmailCapability

    @Autowired
    JiraCapability mockJiraService

    @Autowired
    RootAccountEntity testUser

    @Autowired
    RootAccountRepository rootAccountRepository

    @Autowired
    GridClock mockTimeService;

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
        mockTimeService.nanoTime() >> System.nanoTime()
    }

    def "should allow a user to link their personal account to an organization by using the invitation key from the email"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        when:

        accountClient.login()

        String inviteKey = null;

        1 * mockEmailCapability.sendDownloadActivateAndOrgInviteEmail(_, _, _) >> { emailAddr, org, token -> inviteKey = token; return null; }

        inviteToClient.inviteToActiveOrganization(new EmailInputDto("zoe@dreamscale.io"));

        AccountActivationDto zoeProfile = register("zoe@personal.com");

        switchUser(zoeProfile)

        accountClient.login()

        InvitationKeyDto invitationKey = invitationClient.useInvitationKey(new InvitationKeyInputDto(inviteKey))

        accountClient.logout()
        accountClient.login()

        OrganizationDto zoesOrg = organizationClient.getMyActiveOrganization();

        accountClient.logout()

        switchUser(artyProfile)

        accountClient.login()

        List<MemberDetailsDto> artysOrgMembers = organizationClient.getOrganizationMembers();

        then:
        assert invitationKey.status.status == Status.JOINED

        assert zoesOrg.getId() == dreamScaleSubscription.getOrganizationId()
        assert artysOrgMembers.size() == 2
    }


    def "should allow a user to invite a member to their team via username"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        accountClient.login()

        String inviteToOrgKey = inviteToOrgWithEmail("zoe@dreamscale.io")

        accountClient.logout()

        switchUser(zoeProfile)

        accountClient.login()

        invitationClient.useInvitationKey(new InvitationKeyInputDto(inviteToOrgKey))

        accountClient.logout()
        accountClient.login()

        accountClient.updateOrgProfileUsername(new UsernameInputDto("zoe"))

        accountClient.logout()

        switchUser(artyProfile)

        accountClient.login()

        when:

        TeamDto phoenixTeam = teamClient.createTeam("Phoenix")

        //this should be instant, since it doesn't require email validation

        SimpleStatusDto inviteStatus = inviteToClient.inviteToActiveTeamWithUsername(new UsernameInputDto("zoe"))

        TeamDto artysHomeTeam = teamClient.getMyHomeTeam()

        then:
        assert inviteStatus.status == Status.JOINED

        assert artysHomeTeam.getId() == phoenixTeam.getId()
        assert artysHomeTeam.getTeamMembers().size() == 2
    }



    private void switchUser(AccountActivationDto artyProfile) {
        RootAccountEntity account = rootAccountRepository.findByApiKey(artyProfile.getApiKey());

        testUser.setId(account.getId())
        testUser.setApiKey(account.getApiKey())
    }

    private AccountActivationDto register(String email) {

        RootAccountCredentialsInputDto rootAccountInput = new RootAccountCredentialsInputDto();
        rootAccountInput.setEmail(email)

        String inviteKey = null;

        1 * mockEmailCapability.sendDownloadAndActivationEmail(_, _) >> { emailAddr, token -> inviteKey = token; return null}

        UserProfileDto userProfileDto = accountClient.register(rootAccountInput)
        return accountClient.activate(new ActivationCodeDto(inviteKey))
    }

    private String inviteToOrgWithEmail(String email) {

        String activationToken = null;

        1 * mockEmailCapability.sendDownloadActivateAndOrgInviteEmail(_, _, _) >> { emailAddr, org, token -> activationToken = token; return null}

        inviteToClient.inviteToActiveOrganization(new EmailInputDto(email))

        return activationToken;
    }

    private OrganizationSubscriptionDto createSubscriptionAndValidateEmail(String domain, String ownerEmail) {
        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput(domain, ownerEmail)

        String validateToken = null;

        1 * mockEmailCapability.sendEmailToValidateOrgEmailAddress(_, _) >> { emailAddr, token -> validateToken = token; return null}

        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        invitationClient.useInvitationKey(new InvitationKeyInputDto(validateToken))

        return dreamScaleSubscription;
    }

    private OrganizationSubscriptionDto createSubscription(String domain, String ownerEmail) {
        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput(domain, ownerEmail)

        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        return dreamScaleSubscription;
    }

    private SubscriptionInputDto createSubscriptionInput(String domain, String ownerEmail) {
        SubscriptionInputDto orgSubscription = new SubscriptionInputDto()
        orgSubscription.setOrganizationName("MemberCompany")
        orgSubscription.setDomainName(domain)
        orgSubscription.setRequireMemberEmailInDomain(true)
        orgSubscription.setSeats(10)
        orgSubscription.setOwnerEmail(ownerEmail)
        orgSubscription.setStripePaymentId("[payment.id]")

        return orgSubscription
    }


}
