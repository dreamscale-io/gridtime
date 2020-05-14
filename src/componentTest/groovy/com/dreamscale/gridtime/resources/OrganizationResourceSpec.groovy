package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.ConnectionStatusDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.organization.JiraConfigDto
import com.dreamscale.gridtime.api.organization.JoinRequestInputDto
import com.dreamscale.gridtime.api.organization.MemberDetailsDto
import com.dreamscale.gridtime.api.organization.MembershipInputDto
import com.dreamscale.gridtime.api.organization.OrganizationDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.status.ConnectionResultDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.client.AccountClient
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
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraUserDto
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class OrganizationResourceSpec extends Specification {

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
        assert subscription.getInviteToken() != null
    }


    def "should retrieve organization subscriptions"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io")
        switchUser(artyProfile)
        accountClient.login()

        SubscriptionInputDto orgSubscription1 = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")
        SubscriptionInputDto orgSubscription2 = createSubscriptionInput("onprem.com", "arty@onprem.com")

        OrganizationSubscriptionDto subscription1 = subscriptionClient.createSubscription(orgSubscription1)
        OrganizationSubscriptionDto subscription2 = subscriptionClient.createSubscription(orgSubscription2)

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
        assert connectionToPublicOrg.getParticipatingOrganizations().get(0).orgName == "Public"

    }

    private void switchUser(AccountActivationDto artyProfile) {
        RootAccountEntity account = rootAccountRepository.findByApiKey(artyProfile.getApiKey());

        testUser.setId(account.getId())
        testUser.setApiKey(account.getApiKey())
    }

    def "should allow user to login to a private organization"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

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

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        when:

        SimpleStatusDto artyJoined = joinOrganizationWithValidate(dreamScaleSubscription.getInviteToken(), "arty@dreamscale.io")

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

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        String validationCode = null;

        1 * mockEmailCapability.sendEmailToValidateOrgEmailAddress(_, _) >> { emailAddr, token -> validationCode = token; return null}

        SubscriptionInputDto onpremSubscriptionInput = createSubscriptionInput("onprem.com", "arty@onprem.com")
        OrganizationSubscriptionDto onpremSubscription = subscriptionClient.createSubscription(onpremSubscriptionInput)

        organizationClient.validateMemberEmailAndJoin(validationCode)

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
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        switchUser(zoeProfile)

        SimpleStatusDto zoeJoinedDS = joinOrganization(dreamScaleSubscription.getInviteToken(), "zoe@dreamscale.io")

        when:

        ConnectionStatusDto loginToDSFromZoe = accountClient.login()

        OrganizationDto dsIsActiveForZoe = organizationClient.getMyActiveOrganization();

        accountClient.logout()

        switchUser(artyProfile)

        ConnectionStatusDto loginToDSFromArty =  accountClient.login()

        OrganizationDto dsIsActiveForArty = organizationClient.getMyActiveOrganization();


        List<MemberDetailsDto> memberships = organizationClient.getOrganizationMembers();

        then:

        assert zoeJoinedDS.status == Status.JOINED

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

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        switchUser(shakyProfile)

        SimpleStatusDto shakyJoinedDS = joinOrganization(dreamScaleSubscription.getInviteToken(), "shaky.piano@dreamscale.io")

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

        assert shakyJoinedDS.status == Status.JOINED

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

        assert activeOrgForShakyAfterRemove.orgName == "Public"
        assert shakysOrganizationsAfterRemove.size() == 1

    }

    def "should configure an organization with jira capabilities"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io", "arty@dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

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

        activationToken = null;

        1 * mockEmailCapability.sendDownloadAndActivationEmail(_, _) >> { emailAddr, token -> activationToken = token; return null}

        UserProfileDto userProfileDto = accountClient.register(rootAccountInput)
        return accountClient.activate(new ActivationCodeDto(activationToken))
    }

    private SimpleStatusDto joinOrganization(String inviteToken, String email) {

        return organizationClient.joinOrganizationWithInvitationAndEmail(
                new JoinRequestInputDto(inviteToken, email))
    }

    private SimpleStatusDto joinOrganizationWithValidate(String inviteToken, String email) {

        1 * mockEmailCapability.sendEmailToValidateOrgEmailAddress(_, _) >> { emailAddr, ticketCode -> validationCode = ticketCode; return null}

        organizationClient.joinOrganizationWithInvitationAndEmail(
                new JoinRequestInputDto(inviteToken, email))

        return organizationClient.validateMemberEmailAndJoin(validationCode)
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
