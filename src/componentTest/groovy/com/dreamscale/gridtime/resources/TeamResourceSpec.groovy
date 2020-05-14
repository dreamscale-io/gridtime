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
import com.dreamscale.gridtime.api.organization.JoinRequestInputDto
import com.dreamscale.gridtime.api.organization.MemberDetailsDto
import com.dreamscale.gridtime.api.organization.OrganizationDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.organization.TeamMemberDto
import com.dreamscale.gridtime.api.status.ConnectionResultDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.HomeTeamConfigInputDto
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.InvitationClient
import com.dreamscale.gridtime.client.InviteToClient
import com.dreamscale.gridtime.client.MemberClient
import com.dreamscale.gridtime.client.OrganizationClient
import com.dreamscale.gridtime.client.SubscriptionClient
import com.dreamscale.gridtime.client.TeamClient
import com.dreamscale.gridtime.core.capability.integration.EmailCapability
import com.dreamscale.gridtime.core.capability.integration.JiraCapability
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.capability.directory.TeamCapability
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.TeamMemberRepository
import com.dreamscale.gridtime.core.domain.member.TeamRepository
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class TeamResourceSpec extends Specification {

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    SubscriptionClient subscriptionClient

    @Autowired
    TeamClient teamClient

    @Autowired
    MemberClient memberClient

    @Autowired
    InviteToClient inviteToClient

    @Autowired
    InvitationClient invitationClient

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

    def "should create and join a team and retrieve members"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@personal.com");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        when:

        ConnectionStatusDto loginToDSFromArty = accountClient.login()
        OrganizationDto dsIsActiveForArty = organizationClient.getMyActiveOrganization();

        TeamDto phoenixTeam = teamClient.createTeam("Phoenix")

        String invitationKey = inviteToTeamWithEmail("zoe@dreamscale.io")

        accountClient.logout()

        switchUser(zoeProfile)

        accountClient.login()

        invitationClient.useInvitationKey(new InvitationKeyInputDto(invitationKey))

        accountClient.logout()
        accountClient.login()

        OrganizationDto dsIsActiveForZoe = organizationClient.getMyActiveOrganization();

        TeamDto phoenixIsActiveForZoe = teamClient.getMyHomeTeam();

        then:

        assert loginToDSFromArty != null
        assert loginToDSFromArty.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginToDSFromArty.getParticipatingOrganizations().size() == 2

        assert dsIsActiveForArty.getId() == dreamScaleSubscription.getOrganizationId()
        assert dsIsActiveForZoe.getId() == dreamScaleSubscription.getOrganizationId()

        assert phoenixIsActiveForZoe.id == phoenixTeam.id

        assert phoenixIsActiveForZoe.teamMembers.size() == 2
    }



    def "should create multiple teams and switch home teams"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        when:

        ConnectionStatusDto loginToDSFromArty = accountClient.login()
        OrganizationDto dsIsActiveForArty = organizationClient.getMyActiveOrganization();

        TeamDto phoenixTeam = teamClient.createTeam("Phoenix")
        String phoenixInvitation = inviteToTeamWithEmail("zoe@dreamscale.io")

        TeamDto circleTeam = teamClient.createTeam("Circle")
        teamClient.setMyHomeTeam(new HomeTeamConfigInputDto(circleTeam.getName()))

        String circleInvitation = inviteToTeamWithEmail("zoe@dreamscale.io")

        accountClient.logout()

        switchUser(zoeProfile)

        accountClient.login()

        invitationClient.useInvitationKey(new InvitationKeyInputDto(phoenixInvitation))
        invitationClient.useInvitationKey(new InvitationKeyInputDto(circleInvitation))

        accountClient.logout()
        accountClient.login()

        OrganizationDto dsIsActiveForZoe = organizationClient.getMyActiveOrganization();

        TeamDto phoenixIsActiveForZoe = teamClient.getMyHomeTeam();

        teamClient.setMyHomeTeam(new HomeTeamConfigInputDto(circleTeam.getName()));

        TeamDto circleIsActiveForZoe = teamClient.getMyHomeTeam();

        List<TeamDto> teams = teamClient.getAllMyTeams();

        then:

        assert loginToDSFromArty != null
        assert loginToDSFromArty.getOrganizationId() == dreamScaleSubscription.getOrganizationId()
        assert loginToDSFromArty.getParticipatingOrganizations().size() == 2

        assert dsIsActiveForArty.getId() == dreamScaleSubscription.getOrganizationId()
        assert dsIsActiveForZoe.getId() == dreamScaleSubscription.getOrganizationId()

        assert phoenixIsActiveForZoe.id == phoenixTeam.id
        assert phoenixIsActiveForZoe.teamMembers.size() == 2
        assert phoenixIsActiveForZoe.isHomeTeam() == true

        assert circleIsActiveForZoe.id == circleTeam.id
        assert circleIsActiveForZoe.teamMembers.size() == 2
        assert circleIsActiveForZoe.isHomeTeam() == true

        assert teams.size() == 3
        assert teams.get(0).isHomeTeam() == true
        assert teams.get(1).isHomeTeam() == false
        assert teams.get(2).isHomeTeam() == false


    }



    private AccountActivationDto register(String email) {

        RootAccountCredentialsInputDto rootAccountInput = new RootAccountCredentialsInputDto();
        rootAccountInput.setEmail(email)

        String activationToken = null;

        1 * mockEmailCapability.sendDownloadAndActivationEmail(_, _) >> { emailAddr, token -> activationToken = token; return null}

        UserProfileDto userProfileDto = accountClient.register(rootAccountInput)
        return accountClient.activate(new ActivationCodeDto(activationToken))
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


    private void switchUser(AccountActivationDto artyProfile) {
        RootAccountEntity account = rootAccountRepository.findByApiKey(artyProfile.getApiKey());

        testUser.setId(account.getId())
        testUser.setApiKey(account.getApiKey())
    }


    AccountActivationDto registerWithInviteKey(String email, String invitationKey) {

        RootAccountCredentialsInputDto rootAccountInput = new RootAccountCredentialsInputDto();
        rootAccountInput.setEmail(email)
        rootAccountInput.setInvitationKey(invitationKey)

        UserProfileDto userProfile = accountClient.register(rootAccountInput)


        return accountClient.activate(new ActivationCodeDto(invitationKey))
    }

    private String inviteToTeamWithEmail(String email) {

        String inviteToken = null;

        1 * mockEmailCapability.sendDownloadActivateAndOrgInviteEmail(_, _, _) >> { emailAddr, org, token -> inviteToken = token; return null}

        inviteToClient.inviteToActiveTeamWithEmail(new EmailInputDto(email))

        return inviteToken;
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

}
