package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.ConnectionStatusDto
import com.dreamscale.gridtime.api.account.EmailInputDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.invitation.InvitationKeyDto
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto
import com.dreamscale.gridtime.api.organization.JiraConfigDto
import com.dreamscale.gridtime.api.organization.JoinRequestInputDto
import com.dreamscale.gridtime.api.organization.MemberDetailsDto
import com.dreamscale.gridtime.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.gridtime.api.organization.MembershipInputDto
import com.dreamscale.gridtime.api.organization.OrganizationDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.status.ConnectionResultDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.InvitationClient
import com.dreamscale.gridtime.client.InviteToClient
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.client.SubscriptionClient
import com.dreamscale.gridtime.client.TeamClient
import com.dreamscale.gridtime.core.capability.integration.EmailCapability
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import com.dreamscale.gridtime.core.domain.member.*
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraUserDto
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

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

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        joinOrganization(dreamScaleSubscription.getInviteToken(), "arty@dreamscale.io")

        when:

        accountClient.login()

        String inviteKey = null;

        1 * mockEmailCapability.sendDownloadActivateAndOrgInviteEmail(_, _, _) >> { emailAddr, org, token ->
            println "YAY! :" + token
            inviteKey = token;
            return null;
        }

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

    private SimpleStatusDto joinOrganization(String inviteToken, String email) {

        return organizationClient.joinOrganizationWithInvitationAndEmail(
                new JoinRequestInputDto(inviteToken, email))
    }

    private SimpleStatusDto joinOrganizationWithValidate(String inviteToken, String email) {

        String validationCode = null;

        1 * mockEmailCapability.sendEmailToValidateOrgEmailAddress(_, _) >> { emailAddr, ticketCode -> validationCode = ticketCode; return null}

        organizationClient.joinOrganizationWithInvitationAndEmail(
                new JoinRequestInputDto(inviteToken, email))

        return organizationClient.validateMemberEmailAndJoin(validationCode)
    }

    private SubscriptionInputDto createSubscriptionInput(String domain) {
        SubscriptionInputDto orgSubscription = new SubscriptionInputDto()
        orgSubscription.setOrganizationName("MemberCompany")
        orgSubscription.setDomainName(domain)
        orgSubscription.setRequireMemberEmailInDomain(true)
        orgSubscription.setSeats(10)
        orgSubscription.setStripePaymentId("[payment.id]")

        return orgSubscription
    }


}
