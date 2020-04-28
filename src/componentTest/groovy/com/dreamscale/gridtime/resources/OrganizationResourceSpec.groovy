package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.AccountActivationDto
import com.dreamscale.gridtime.api.account.ActivationCodeDto
import com.dreamscale.gridtime.api.account.ConnectionStatusDto
import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.account.UserProfileDto
import com.dreamscale.gridtime.api.organization.JoinRequestInputDto
import com.dreamscale.gridtime.api.organization.MemberRegistrationDto
import com.dreamscale.gridtime.api.organization.MembershipInputDto
import com.dreamscale.gridtime.api.organization.OrganizationDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.MemberRegistrationDetailsDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.organization.TeamWithMembersDto
import com.dreamscale.gridtime.api.status.ConnectionResultDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.api.team.TeamMemberDto
import com.dreamscale.gridtime.api.team.TeamMembersToAddInputDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.client.SubscriptionClient
import com.dreamscale.gridtime.client.TeamClient
import com.dreamscale.gridtime.core.capability.integration.EmailCapability
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
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

        SubscriptionInputDto orgSubscription = createSubscriptionInput("dreamscale.io")

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

        SubscriptionInputDto orgSubscription1 = createSubscriptionInput("dreamscale.io")
        SubscriptionInputDto orgSubscription2 = createSubscriptionInput("onprem.com")

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

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        when:

        SimpleStatusDto artyJoined = joinOrganization(dreamScaleSubscription.getInviteToken(), "arty@dreamscale.io")

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

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io")
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

        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io")
        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)

        SubscriptionInputDto onpremSubscriptionInput = createSubscriptionInput("onprem.com")
        OrganizationSubscriptionDto onpremSubscription = subscriptionClient.createSubscription(onpremSubscriptionInput)

        SimpleStatusDto artyJoinedDS = joinOrganization(dreamScaleSubscription.getInviteToken(), "arty@dreamscale.io")
        SimpleStatusDto artyJoinedOP = joinOrganizationWithValidate(onpremSubscription.getInviteToken(), "arty@onprem.com")

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


    //TODO now, when I'm logged into the active organization

    //I should be able to retrieve all the members of the org
    //I shouldn't be able to join if theres no seats left

    //get active, participating, members of the logged in org

    //can join an org created by someone else, consume their seats

    //be able to remove member from an org, and reclaim the subscription seat

    //configure jira
//
//    def "should create a subscription and add member "() {
//        given:
//
//        rootAccountRepository.save(testUser)
//
//        SubscriptionInputDto dreamScaleSubscriptionInput = createSubscriptionInput("dreamscale.io")
//        OrganizationSubscriptionDto dreamScaleSubscription = subscriptionClient.createSubscription(dreamScaleSubscriptionInput)
//
//        when:
//
//        1 * mockEmailCapability.sendEmailToValidateOrgEmailAddress(_, _) >> { email, ticketCode -> validationCode = ticketCode; return null}
//
//        organizationClient.joinOrganizationWithInvitationAndEmail(
//                new JoinRequestInputDto(dreamScaleSubscription.getInviteToken(), "arty@dreamscale.io"))
//
//        organizationClient.validateMemberEmailAndJoin(validationCode)
//
//        //okay, for this to work, I've gotta be logged in as this org.
//
//        accountClient.login()
//
//
//        List<MemberRegistrationDto> members = organizationClient.getOrganizationMembers();
//
//        then:
//        assert members != null
//        assert members.size() == 1
//    }
//
//
//    def "should retrieve public as the active organization when no orgs exist"() {
//        given:
//
//        accountClient.login()
//
//        organizationClient.getMyActiveOrganization();
//
//        SubscriptionInputDto orgSubscription1 = createSubscriptionInput("dreamscale.io")
//        SubscriptionInputDto orgSubscription2 = createSubscriptionInput("onprem.com")
//
//        OrganizationSubscriptionDto subscription1 = subscriptionClient.createSubscription(orgSubscription1)
//        OrganizationSubscriptionDto subscription2 = subscriptionClient.createSubscription(orgSubscription2)
//
//        when:
//        List<OrganizationSubscriptionDto> subscriptions = subscriptionClient.getOrganizationSubscriptions();
//
//        then:
//        assert subscriptions != null
//        assert subscriptions.size() == 2
//    }
//
//
//
//
//
//
//    def "should decode member invitation from link"() {
//        given:
//        SubscriptionInputDto organization = createSubscriptionInput("dreamscale.io")
//
//        when:
//        OrganizationDto organizationDto = organizationClient.createOrganization(organization)
//        OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken())
//
//        then:
//        assert inviteOrg != null
//        assert inviteOrg.getId() != null
//        assert inviteOrg.getOrgName() == organization.getOrgName()
//        assert inviteOrg.getDomainName() == organization.getDomainName()
//        assert inviteOrg.getConnectionStatus() == Status.VALID
//        assert inviteOrg.getInviteLink() != null
//    }
//
//    @Ignore
//    def "should add member to organization if valid user"() {
//        given:
//        SubscriptionInputDto organization = createSubscriptionInput("dreamscale.io")
//
//        OrganizationDto organizationDto = organizationClient.createOrganization(organization)
//        OrganizationDto inviteOrg = organizationClient.decodeInvitation(organizationDto.getInviteToken())
//
//        MembershipInputDto membershipInputDto = new MembershipInputDto()
//        membershipInputDto.setInviteToken(organizationDto.getInviteToken())
//        membershipInputDto.setOrgEmail("janelle@dreamscale.io")
//
//        JiraUserDto jiraUserDto = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build()
//        mockJiraService.getUserByEmail(_, _) >> jiraUserDto
//
//        when:
//
//        MemberRegistrationDetailsDto membershipDto = organizationClient.registerMember(inviteOrg.getId().toString(), membershipInputDto)
//
//        then:
//        assert membershipDto != null
//        assert membershipDto.getMemberId() != null
//        assert membershipDto.getRootAccountId() != null
//        assert membershipDto.getOrgEmail() == membershipInputDto.getOrgEmail()
//        assert membershipDto.getFullName() == jiraUserDto.displayName
//        assert membershipDto.getActivationCode() != null
//
//    }
//
//
//    def "should create a team within the org"() {
//        given:
//        OrganizationDto org = createOrganizationWithClient()
//
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
//        testUser.setId(member.getRootAccountId())
//
//        when:
//        TeamDto team = teamClient.createTeam( "unicorn")
//
//        then:
//        assert team != null
//        assert team.id != null
//        assert team.name == "unicorn"
//    }
//
//    def "should get teams within the org"() {
//        given:
//        OrganizationDto org = createOrganizationWithClient()
//
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
//        testUser.setId(member.getRootAccountId())
//
//
//        TeamDto team1 = teamClient.createTeam("unicorn")
//        TeamDto team2 = teamClient.createTeam("lightning")
//
//        when:
//        List<TeamDto> teams = teamClient.getAllTeams() //everyone team is here too
//
//        then:
//        assert teams != null
//        assert teams.size() == 3
//    }
//
//
//    def "should add members to team"() {
//        given:
//
//        OrganizationDto org = createOrganizationWithClient()
//
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
//        testUser.setId(member.getRootAccountId())
//
//
//        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
//        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")
//
//        TeamDto team = teamClient.createTeam("unicorn")
//
//        TeamMembersToAddInputDto teamMembersToAdd = new TeamMembersToAddInputDto([registration1.memberId, registration2.memberId])
//
//        when:
//        TeamMemberDto member1 = teamClient.addMemberToTeamWithMemberId("unicorn", registration1.memberId.toString())
//        TeamMemberDto member2 = teamClient.addMemberToTeamWithMemberId("unicorn", registration2.memberId.toString())
//
//        then:
//        assert member1.memberId == registration1.memberId
//
//        assert member2.memberId == registration2.memberId
//    }
//
//
//    def "should retrieve my teams that I am a member of"() {
//        given:
//
//        OrganizationDto org = createOrganizationWithClient()
//
//        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
//        testUser.setId(member.getRootAccountId())
//
//
//        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
//        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")
//
//        TeamDto team1 = teamClient.createTeam("Team1")
//        TeamDto team2 = teamClient.createTeam("Team2")
//        TeamDto teamOther = teamClient.createTeam("other")
//
//        teamClient.addMemberToTeamWithMemberId("Team1", registration1.memberId.toString())
//        teamClient.addMemberToTeamWithMemberId("Team1", registration2.memberId.toString())
//        teamClient.addMemberToTeamWithMemberId("Team2", registration1.memberId.toString())
//        teamClient.addMemberToTeamWithMemberId("Team2", registration2.memberId.toString())
//
//        teamClient.addMemberToTeamWithMemberId("other", registration2.memberId.toString())
//
//        //active request coming from janelle
//        testUser.id = registration1.rootAccountId
//
//        when:
//        List<TeamDto> myTeams = teamClient.getAllMyParticipatingTeams()
//
//        then:
//        assert myTeams != null //everyone here too
//        assert myTeams.size() == 3
//
//    }
//
//    def "should retrieve team member status of specified team"() {
//        given:
//
//        OrganizationDto org = createOrganizationWithClient()
//
//        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
//        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")
//        MemberRegistrationDetailsDto registration3 = registerMemberWithClient(org, "mike@dreamscale.io")
//
//        testUser.setId(registration1.getRootAccountId())
//
//        //login cant find home team, so this dont work, should be able to login without a team.
//        accountClient.login()
//
//        TeamDto team = teamClient.createTeam("Unicorn")
//
//        println registration1.memberId
//        println registration2.memberId
//        println registration3.memberId
//
//        teamClient.addMemberToTeamWithMemberId("Unicorn", registration1.memberId.toString())
//        teamClient.addMemberToTeamWithMemberId("Unicorn", registration2.memberId.toString())
//        teamClient.addMemberToTeamWithMemberId("Unicorn", registration3.memberId.toString())
//
//        when:
//        TeamWithMembersDto teamWithMembers = teamClient.getTeam("Unicorn")
//
//        then:
//        assert teamWithMembers.me.id == registration1.memberId
//        assert teamWithMembers.teamMembers.size() == 2
//    }
//
//    def "should retrieve team member status of me and my team"() {
//        given:
//
//        OrganizationDto org = createOrganizationWithClient()
//
//        MemberRegistrationDetailsDto registration1 = registerMemberWithClient(org, "janelle@dreamscale.io")
//        MemberRegistrationDetailsDto registration2 = registerMemberWithClient(org, "kara@dreamscale.io")
//        MemberRegistrationDetailsDto registration3 = registerMemberWithClient(org, "mike@dreamscale.io")
//
//        testUser.setId(registration1.getRootAccountId())
//
//        accountClient.login()
//
//        TeamDto team = teamClient.createTeam("Unicorn")
//
//        teamClient.addMemberToTeamWithMemberId("Unicorn", registration1.memberId.toString())
//        teamClient.addMemberToTeamWithMemberId("Unicorn", registration2.memberId.toString())
//        teamClient.addMemberToTeamWithMemberId("Unicorn", registration3.memberId.toString())
//
//        when:
//        TeamWithMembersDto meAndMyTeam = teamClient.getTeam("Unicorn")
//
//        then:
//        assert meAndMyTeam != null
//        assert meAndMyTeam.getMe() != null
//        assert meAndMyTeam.getTeamMembers().size() == 2
//    }

    private MemberRegistrationDetailsDto registerMemberWithClient(OrganizationDto organizationDto, String memberEmail) {

        MembershipInputDto membershipInputDto = new MembershipInputDto()
        membershipInputDto.setInviteToken(organizationDto.getInviteToken())
        membershipInputDto.setOrgEmail(memberEmail)

        JiraUserDto jiraUserDto = aRandom.jiraUserDto().emailAddress(membershipInputDto.orgEmail).build()
        1 * mockJiraService.getUserByEmail(_, _) >> jiraUserDto

        return organizationClient.registerMember(organizationDto.getId().toString(), membershipInputDto)

    }

    private OrganizationDto createOrganizationWithClient() {
        SubscriptionInputDto organizationSubscription = createSubscriptionInput("dreamscale.io")

        return organizationClient.createOrganizationSubscription(organizationSubscription)
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

    private SubscriptionInputDto createOrganizationWithInvalidJira() {
        SubscriptionInputDto organization = new SubscriptionInputDto()
        organization.setOrgName("DreamScale")
        organization.setJiraUser("fake")
        organization.setJiraSiteUrl("dreamscale.atlassian.net")
        organization.setJiraApiKey("blabla")

        mockJiraService.validateJiraConnection(_) >> new ConnectionResultDto(Status.FAILED, "failed")

        return organization
    }

}
