package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.*
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.grid.GridStatus
import com.dreamscale.gridtime.api.grid.GridStatusSummaryDto
import com.dreamscale.gridtime.api.grid.GridTableResults
import com.dreamscale.gridtime.api.invitation.InvitationKeyInputDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.project.CreateProjectInputDto
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.api.terminal.*
import com.dreamscale.gridtime.client.*
import com.dreamscale.gridtime.core.capability.external.EmailCapability
import com.dreamscale.gridtime.core.capability.system.GridClock
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.machine.GridTimeEngine
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class TerminalResourceSpec extends Specification {

    @Autowired
    TerminalClient terminalClient

    @Autowired
    InviteToClient inviteToClient

    @Autowired
    InvitationClient invitationClient

    @Autowired
    OrganizationClient organizationClient

    @Autowired
    SubscriptionClient subscriptionClient

    @Autowired
    TeamClient teamClient

    @Autowired
    AccountClient accountClient

    @Autowired
    RootAccountEntity testUser

    @Autowired
    RootAccountRepository rootAccountRepository

    @Autowired
    EmailCapability mockEmailCapability

    @Autowired
    JournalClient journalClient

    @Autowired
    GridClock mockGridClock;

    @Autowired
    GridTimeEngine gridTimeEngine;

    @Autowired
    GridClient gridClient;

    String activationCode = null;

    def setup() {
        mockGridClock.now() >> LocalDateTime.now()
        mockGridClock.nanoTime() >> System.nanoTime()

        gridTimeEngine.reset()
    }

    def "should test terminal loop for a basic invite command"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        when:

        accountClient.login()

        OrganizationSubscriptionDto dreamScaleSubscription = createSubscription("dreamscale.io", "arty@dreamscale.io")

        accountClient.logout()
        accountClient.login()

        1 * mockEmailCapability.sendDownloadActivateAndOrgInviteEmail(_, _, _) >> { emailAddr, org, token -> activationCode = token;
            return new SimpleStatusDto(Status.SENT, "Sent!")}

        TalkMessageDto inviteResult = terminalClient.runCommand(new CommandInputDto(Command.INVITE, "zoe@dreamscale.io", "to", "org"))

        then:
        assert activationCode != null
        assert inviteResult != null
    }

    def "should invite with user names"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(zoeProfile)

        accountClient.login()
        accountClient.updateOrgProfileUsername(new UsernameInputDto("zoe"))

        switchUser(artyProfile)

        accountClient.login()
        TeamDto team = teamClient.createTeam("Phoenix")

        when:

        TalkMessageDto inviteResult = terminalClient.runCommand(new CommandInputDto(Command.INVITE, "zoe", "to", "team"))

        switchUser(zoeProfile)

        TeamDto zoesTeam = teamClient.getMyHomeTeam();

        then:
        assert inviteResult != null

        assert inviteResult.messageType == "SimpleStatusDto"
        assert ((SimpleStatusDto)inviteResult.data).status == Status.JOINED;

        assert  zoesTeam.getName() == team.getName()

    }

    def "should invite to public from terminal"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        when:

        accountClient.login()

        1 * mockEmailCapability.sendDownloadAndInviteToPublicEmail(_, _, _, _) >> { from, emailAddr, org, token -> activationCode = token;
            return new SimpleStatusDto(Status.SENT, "Sent!")}

        TalkMessageDto inviteResult = terminalClient.runCommand(new CommandInputDto(Command.INVITE, "zoe@dreamscale.io", "to", "public"))

        AccountActivationDto accountActivation = accountClient.activate(new ActivationCodeDto(activationCode: activationCode))

        then:
        assert activationCode != null
        assert inviteResult != null
        assert accountActivation.status == Status.VALID
        assert accountActivation.apiKey != null

    }


    def "should share project with user from terminal"() {

        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        ProjectDto proj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        when:

        TalkMessageDto shareResult = terminalClient.runCommand(new CommandInputDto(Command.SHARE, "project", "proj1", "with", "user", "zoe"))

        switchUser(zoeProfile)

        accountClient.login()

        ProjectDto zoesProj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        then:
        assert zoesProj1.getId() == proj1.getId()

    }


    def "should start stop grid from terminal"() {

        given:
        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()
        gridTimeEngine.configureDoneAfterTicks(20)

        when:

        TalkMessageDto gridStartResult = terminalClient.runCommand(new CommandInputDto(Command.GRID, "start"))

        GridStatusSummaryDto statusBefore = gridClient.getStatus();

        TalkMessageDto gridStopResult = terminalClient.runCommand(new CommandInputDto(Command.GRID, "stop"))

        GridStatusSummaryDto statusAfter = gridClient.getStatus();

        then:
        assert gridStartResult != null
        assert gridStopResult != null

        assert statusBefore.getGridStatus().equals(GridStatus.RUNNING)
        assert statusAfter.getGridStatus().equals(GridStatus.STOPPED)
    }

    def "should display grid processes from terminal"() {

        given:
        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()
        gridTimeEngine.configureDoneAfterTicks(20)

        when:

        TalkMessageDto gridStartResult = terminalClient.runCommand(new CommandInputDto(Command.GRID, "start"))

        gridTimeEngine.waitForDone()

        TalkMessageDto psResult = terminalClient.runCommand(new CommandInputDto(Command.PS, "all"))
        GridTableResults results = (GridTableResults) psResult.getData();

        println results.toDisplayString()

        then:
        assert psResult != null
    }


    def "should unshare project for user from terminal"() {

        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        ProjectDto proj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        when:


        TalkMessageDto shareResult = terminalClient.runCommand( new CommandInputDto(Command.SHARE, "project", "proj1", "with", "user", "zoe"))
        TalkMessageDto unshareResult = terminalClient.runCommand( new CommandInputDto(Command.UNSHARE, "project", "proj1", "for", "user", "zoe"))

        switchUser(zoeProfile)

        accountClient.login()

        ProjectDto zoesProj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        then:
        assert zoesProj1.getId() != proj1.getId()

    }

    def "should share project with team from terminal"() {

        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        ProjectDto proj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        teamClient.createTeam("Phoenix")

        when:

        TalkMessageDto shareResult = terminalClient.runCommand(new CommandInputDto(Command.SHARE, "project", "proj1", "with", "team", "Phoenix"))

        switchUser(zoeProfile)

        accountClient.login()

        teamClient.joinTeam("Phoenix")

        ProjectDto zoesProj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        then:
        assert zoesProj1.getId() == proj1.getId()
    }

    def "should unshare project for team from terminal"() {

        given:

        mockGridClock.now() >> LocalDateTime.now()

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        ProjectDto proj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        teamClient.createTeam("Phoenix")

        when:

        TalkMessageDto shareResult = terminalClient.runCommand( new CommandInputDto(Command.SHARE, "project", "proj1", "with", "team", "Phoenix"))
        TalkMessageDto unshareResult = terminalClient.runCommand( new CommandInputDto(Command.UNSHARE, "project", "proj1", "for", "team", "Phoenix"))

        switchUser(zoeProfile)

        accountClient.login()

        teamClient.joinTeam("Phoenix")

        ProjectDto zoesProj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        then:
        assert zoesProj1.getId() != proj1.getId()

    }

    def "should get terminal help manual"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        when:

        accountClient.login()

        CommandManualDto manual = terminalClient.getCommandManual()
        CommandManualPageDto inviteManPage = terminalClient.getManualPageForCommand("invite");
        CommandManualPageDto projectManPage = terminalClient.getManualPageForGroup("project");

        println inviteManPage

        println projectManPage

        then:
        assert manual != null
        assert manual.getActivityContexts().size() == 3;

        assert inviteManPage != null
        assert inviteManPage.getContextName() == Command.INVITE.name()
        assert inviteManPage.getCommandDescriptors().size() == 1;

        CommandDescriptorDto inviteCmd = inviteManPage.getCommandDescriptors().get(0);

        assert inviteCmd.terminalRoutes.size() == 1
        assert inviteCmd.terminalRoutes.get(0).argsTemplate != null
        assert inviteCmd.terminalRoutes.get(0).optionsHelp.size() == 2

        assert projectManPage != null

        assert projectManPage.getContextName() == ActivityContext.PROJECT.name()
        assert projectManPage.getCommandDescriptors().size() == 3

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
