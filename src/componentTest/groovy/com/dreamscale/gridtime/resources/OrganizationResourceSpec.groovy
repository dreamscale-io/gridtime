package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.ConnectionStatusDto
import com.dreamscale.gridtime.api.account.EmailInputDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto
import com.dreamscale.gridtime.api.organization.JiraConfigDto
import com.dreamscale.gridtime.api.organization.MemberDetailsDto
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
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberRepository
import com.dreamscale.gridtime.core.domain.member.TeamRepository
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class OrganizationResourceSpec extends Specification {

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    SubscriptionClient subscriptionClient

    @Autowired
    InviteToClient inviteToClient

    @Autowired
    InvitationClient invitationClient

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

    String validationCode = null;
    String activationToken = null;

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
        mockTimeService.nanoTime() >> System.nanoTime()

    }

    def "should create organization subscription"() {

        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io")
        switchUser(artyProfile)
        accountClient.login()

        SubscriptionInputDto orgSubscription = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")

        when:
        OrganizationSubscriptionDto subscription = subscriptionClient.createSubscription(orgSubscription)

        then:
        assert subscription != null
        assert subscription.getOrganizationId() != null
        assert subscription.getOrganizationName() == orgSubscription.getOrganizationName()
        assert subscription.getDomainName() == orgSubscription.getDomainName()
    }


    def "should retrieve organization subscriptions"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io")
        switchUser(artyProfile)
        accountClient.login()

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto onPremSubscription = createSubscriptionAndValidateEmail("onprem.com", "arty@onprem.com")

        when:
        List<OrganizationSubscriptionDto> subscriptions = subscriptionClient.getOrganizationSubscriptions();

        then:
        assert subscriptions != null
        assert subscriptions.size() == 2
    }

    def "should allow user to login to the public organization"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        when:
        ConnectionStatusDto connectionToPublicOrg = accountClient.login();

        then:

        assert connectionToPublicOrg != null
        assert connectionToPublicOrg.getParticipatingOrganizations().size() == 1
        assert connectionToPublicOrg.getParticipatingOrganizations().get(0).orgName == "Open"

    }

    private void switchUser(AccountActivationDto userProfile) {
        RootAccountEntity account = rootAccountRepository.findByApiKey(userProfile.getApiKey());

        testUser.setId(account.getId())
        testUser.setApiKey(account.getApiKey())
    }

    def "should allow user to login to a private organization"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        when:
        ConnectionStatusDto connectionStatusDto = accountClient.login()

        then:

        assert connectionStatusDto != null
        assert connectionStatusDto.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert connectionStatusDto.getParticipatingOrganizations().size() == 2
    }

    def "should allow joining an organization from a personal email root account"() {
        given:

        AccountActivationDto artyProfile = register("personal.arty@gmail.com");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscriptionAndValidateEmail("dreamscale.io", "arty@dreamscale.io")

        when:

        ConnectionStatusDto connectionStatusDto = accountClient.login()

        then:

        assert connectionStatusDto != null
        assert connectionStatusDto.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert connectionStatusDto.getParticipatingOrganizations().size() == 2

    }


    def "should allow user to join and login to multiple private organizations"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto onpremSubscription = createSubscriptionAndValidateEmail("onprem.com", "arty@onprem.com")

        when:

        ConnectionStatusDto loginToDSStatus = accountClient.loginToOrganization(dreamScaleSubscription.getOrganizationId())

        OrganizationDto dsIsActive = organizationClient.getMyActiveOrganization();

        accountClient.logout()

        ConnectionStatusDto loginToOPStatus = accountClient.loginToOrganization(onpremSubscription.getOrganizationId())

        OrganizationDto opIsActive = organizationClient.getMyActiveOrganization();

        then:

        assert loginToDSStatus != null
        assert loginToDSStatus.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginToDSStatus.getParticipatingOrganizations().size() == 3
        assert dsIsActive.getId() == dreamScaleSubscription.getOrganizationId()

        assert loginToOPStatus != null
        assert loginToOPStatus.getOrganizationId() == onpremSubscription.getOrganizationId()
        assert loginToOPStatus.getParticipatingOrganizations().size() == 3
        assert opIsActive.getId() == onpremSubscription.getOrganizationId()
    }

    def "should join an organization created by someone else"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        String inviteKey = inviteToOrgWithEmail("zoe@dreamscale.io")

        AccountActivationDto zoeProfile = registerWithInviteKey("zoe@personal.com", inviteKey);

        switchUser(zoeProfile)

        when:

        ConnectionStatusDto loginToDSFromZoe = accountClient.login()

        OrganizationDto dsIsActiveForZoe = organizationClient.getMyActiveOrganization();

        accountClient.logout()

        switchUser(artyProfile)

        ConnectionStatusDto loginToDSFromArty =  accountClient.login()

        OrganizationDto dsIsActiveForArty = organizationClient.getMyActiveOrganization();

        List<MemberDetailsDto> memberships = organizationClient.getOrganizationMembers();

        then:

        assert loginToDSFromZoe != null
        assert loginToDSFromZoe.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginToDSFromZoe.getParticipatingOrganizations().size() == 2

        assert loginToDSFromArty != null
        assert loginToDSFromArty.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginToDSFromArty.getParticipatingOrganizations().size() == 2

        assert dsIsActiveForZoe.getId() == dreamScaleSubscription.getOrganizationId()
        assert dsIsActiveForArty.getId() == dreamScaleSubscription.getOrganizationId()

        assert memberships.size() == 2
    }

    def "should join an organization from an existing private account using invite key"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        AccountActivationDto zoeProfile = register("zoe@personal.com");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        String zoeInviteKey = inviteToOrgWithEmail("zoe@dreamscale.io")

        switchUser(zoeProfile)

        invitationClient.useInvitationKey(new InvitationKeyInputDto(zoeInviteKey))

        when:

        ConnectionStatusDto loginToDSFromZoe = accountClient.login()

        OrganizationDto dsIsActiveForZoe = organizationClient.getMyActiveOrganization();

        accountClient.logout()

        switchUser(artyProfile)

        ConnectionStatusDto loginToDSFromArty =  accountClient.login()

        OrganizationDto dsIsActiveForArty = organizationClient.getMyActiveOrganization();

        List<MemberDetailsDto> memberships = organizationClient.getOrganizationMembers();

        then:

        assert loginToDSFromZoe != null
        assert loginToDSFromZoe.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginToDSFromZoe.getParticipatingOrganizations().size() == 2

        assert loginToDSFromArty != null
        assert loginToDSFromArty.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginToDSFromArty.getParticipatingOrganizations().size() == 2

        assert dsIsActiveForZoe.getId() == dreamScaleSubscription.getOrganizationId()
        assert dsIsActiveForArty.getId() == dreamScaleSubscription.getOrganizationId()

        assert memberships.size() == 2
    }

    def "should remove a member from an organization"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto shakyProfile = register("shaky.piano@dreamscale.io");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        String inviteKey = inviteToOrgWithEmail("shaky.piano@dreamscale.io")

        switchUser(shakyProfile)

        invitationClient.useInvitationKey(new InvitationKeyInputDto(inviteKey))

        when:

        ConnectionStatusDto loginFromShakyBeforeRemove = accountClient.login()

        OrganizationDto activeOrgForShakyBeforeRemove = organizationClient.getMyActiveOrganization();

        accountClient.logout()

        switchUser(artyProfile)

        ConnectionStatusDto loginFromArty =  accountClient.login()

        OrganizationDto activeOrgForArty = organizationClient.getMyActiveOrganization();

        List<MemberDetailsDto> membershipsShownForArtyBeforeRemove = organizationClient.getOrganizationMembers();

        organizationClient.removeMember(loginFromShakyBeforeRemove.getMemberId().toString())

        List<MemberDetailsDto> membershipsShownForArtyAfterRemove = organizationClient.getOrganizationMembers();

        switchUser(shakyProfile)

        ConnectionStatusDto loginFromShakyAfterRemove = accountClient.login()

        OrganizationDto activeOrgForShakyAfterRemove = organizationClient.getMyActiveOrganization();

        List<OrganizationDto> shakysOrganizationsAfterRemove =  organizationClient.getParticipatingOrganizations();

        then:

        assert loginFromShakyBeforeRemove != null
        assert loginFromShakyBeforeRemove.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginFromShakyBeforeRemove.getParticipatingOrganizations().size() == 2

        assert activeOrgForShakyBeforeRemove.id == dreamScaleSubscription.getOrganizationId()

        assert loginFromArty != null
        assert loginFromArty.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginFromArty.getParticipatingOrganizations().size() == 2
        assert activeOrgForArty.id  == dreamScaleSubscription.getOrganizationId()

        assert membershipsShownForArtyBeforeRemove.size() == 2
        assert membershipsShownForArtyAfterRemove.size() == 1

        assert loginFromShakyAfterRemove != null
        assert loginFromShakyAfterRemove.getOrganizationId() != dreamScaleSubscription.getOrganizationId()
        assert loginFromShakyAfterRemove.getParticipatingOrganizations().size() == 1

        assert activeOrgForShakyAfterRemove.orgName == "Open"
        assert shakysOrganizationsAfterRemove.size() == 1

    }

    def "should configure an organization with jira capabilities"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        JiraConfigDto jiraInputConfig = createJiraConfig()

        when:

        accountClient.login()

        1 * mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.VALID, "Connected")

        SimpleStatusDto jiraConfigValidStatus = organizationClient.updateJiraConfiguration(jiraInputConfig)

        JiraConfigDto jiraRetrievedConfig = organizationClient.getJiraConfiguration();

        then:

        assert jiraConfigValidStatus != null
        assert jiraConfigValidStatus.getStatus() == Status.VALID

        assert jiraRetrievedConfig != null
        assert jiraRetrievedConfig.getJiraSiteUrl() == jiraInputConfig.getJiraSiteUrl()
        assert jiraRetrievedConfig.getJiraUser() == jiraInputConfig.getJiraUser()
        assert jiraRetrievedConfig.getJiraApiKey() == jiraInputConfig.getJiraApiKey()

    }

    private AccountActivationDto register(String email) {

        RootAccountCredentialsInputDto rootAccountInput = new RootAccountCredentialsInputDto();
        rootAccountInput.setEmail(email)

        String activationToken = null;

        1 * mockEmailCapability.sendDownloadAndActivationEmail(_, _) >> { emailAddr, token -> activationToken = token; return null}

        UserProfileDto userProfileDto = accountClient.register(rootAccountInput)
        return accountClient.activate(new ActivationCodeDto(activationToken))
    }

    AccountActivationDto registerWithInviteKey(String email, String invitationKey) {

        RootAccountCredentialsInputDto rootAccountInput = new RootAccountCredentialsInputDto();
        rootAccountInput.setEmail(email)
        rootAccountInput.setInvitationKey(invitationKey)

        UserProfileDto userProfile = accountClient.register(rootAccountInput)


        return accountClient.activate(new ActivationCodeDto(invitationKey))
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

    private JiraConfigDto createJiraConfig() {
        return new JiraConfigDto("company.atlassian.net", "jiraUser", "143143WRU143APIKEY143WRU143")
    }

    private SubscriptionInputDto createSubscriptionInput(String domain, String orgEmail) {
        SubscriptionInputDto orgSubscription = new SubscriptionInputDto()
        orgSubscription.setOrganizationName("MemberCompany")
        orgSubscription.setDomainName(domain)
        orgSubscription.setRequireMemberEmailInDomain(true)
        orgSubscription.setSeats(10)
        orgSubscription.setOwnerEmail(orgEmail)
        orgSubscription.setStripePaymentId("[payment.id]")

        return orgSubscription
    }


}
