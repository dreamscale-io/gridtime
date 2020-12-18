package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.*
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchDto
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
import com.dreamscale.gridtime.core.machine.capabilities.cmd.SystemCmd
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

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

    @Autowired
    CalendarService calendarService;

    @Autowired
    LearningCircuitClient circuitClient;

    @Autowired
    FlowClient flowClient;

    @Autowired
    GridClock gridClock;

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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto inviteResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.INVITE, "zoe@dreamscale.io", "to", "org"))

        then:
        assert activationCode != null
        assert inviteResult != null
    }

    def "should throw an error with invalid circuit"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        when:

        accountClient.login()

        TalkMessageDto result = terminalClient.runCommand("invalid", new CommandInputDto(Command.GRID, "status"))

        then:
        thrown BadRequestException
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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto inviteResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.INVITE, "zoe", "to", "team"))

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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto inviteResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.INVITE, "zoe@dreamscale.io", "to", "public"))

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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto shareResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SHARE, "project", "proj1", "with", "user", "zoe"))

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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto gridStartResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.GRID, "start"))

        GridStatusSummaryDto statusBefore = gridClient.getStatus();

        TalkMessageDto gridStopResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.GRID, "stop"))

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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto gridStartResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.GRID, "start"))

        gridTimeEngine.waitForDone()

        TalkMessageDto psResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.PS, "all"))
        GridTableResults results = (GridTableResults) psResult.getData();

        println results.toDisplayString()

        then:
        assert psResult != null
    }

    def "should show failure details from terminal"() {

        given:
        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()
        gridTimeEngine.configureDoneAfterTicks(20)

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto gridStartResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.GRID, "start"))

        gridTimeEngine.waitForDone()

        TalkMessageDto failResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.ERROR))
        GridTableResults results = (GridTableResults) failResult.getData();

        println results.toDisplayString()

        then:
        assert failResult != null
    }

    def "should purge grid feed data from terminal"() {

        given:
        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()
        gridTimeEngine.configureDoneAfterTicks(20)

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto gridStartResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.GRID, "start"))

        gridTimeEngine.waitForDone()

        TalkMessageDto purgeResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.PURGE, "feeds"))
        SimpleStatusDto statusDto = (SimpleStatusDto) purgeResult.getData();

        println statusDto

        then:
        assert purgeResult != null
        assert statusDto.getStatus() == Status.SUCCESS
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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto shareResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SHARE, "project", "proj1", "with", "user", "zoe"))
        TalkMessageDto unshareResult = terminalClient.runCommand(circuit.getCircuitName(),  new CommandInputDto(Command.UNSHARE, "project", "proj1", "for", "user", "zoe"))

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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto shareResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SHARE, "project", "proj1", "with", "team", "Phoenix"))

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

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto shareResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SHARE, "project", "proj1", "with", "team", "Phoenix"))
        TalkMessageDto unshareResult = terminalClient.runCommand(circuit.getCircuitName(),  new CommandInputDto(Command.UNSHARE, "project", "proj1", "for", "team", "Phoenix"))

        switchUser(zoeProfile)

        accountClient.login()

        teamClient.joinTeam("Phoenix")

        ProjectDto zoesProj1 = journalClient.findOrCreateProject(new CreateProjectInputDto("proj1", "desc", true))

        then:
        assert zoesProj1.getId() != proj1.getId()

    }

    def "should query top wtfs for invoking user"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        calendarService.saveCalendar(1, 3, GeometryClock.createGridTime(ZoomLevel.BLOCK, gridClock.now()))

        LearningCircuitDto circuit1 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit1.getCircuitName())

        LearningCircuitDto circuit2 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit2.getCircuitName())

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto queryResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SELECT, "top", "wtfs", "in", "BLOCK"))

        GridTableResults results = (GridTableResults) queryResult.getData();

        println results.toDisplayString()

        then:
        assert queryResult != null
        assert results.getRowsOfPaddedCells().size() == 2
    }


    def "should query top wtfs with gridtime expression"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        calendarService.saveCalendar(1, 3, GeometryClock.createGridTime(ZoomLevel.BLOCK, gridClock.now()))

        LearningCircuitDto circuit1 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit1.getCircuitName())

        LearningCircuitDto circuit2 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit2.getCircuitName())

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto queryResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SELECT, "top", "wtfs", "in", "gt[2020, 1]"))

        GridTableResults results = (GridTableResults) queryResult.getData();

        println results.toDisplayString()

        then:
        assert queryResult != null
        assert results.getRowsOfPaddedCells().size() == 2
    }


    def "should query top wtfs for targeted user"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        calendarService.saveCalendar(1, 3, GeometryClock.createGridTime(ZoomLevel.BLOCK, gridClock.now()))

        LearningCircuitDto circuit1 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit1.getCircuitName())

        LearningCircuitDto circuit2 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit2.getCircuitName())

        switchUser(zoeProfile)

        accountClient.login()

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto targetResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.TARGET, "user", "arty"))

        TalkMessageDto queryResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SELECT, "top", "wtfs", "in", "BLOCK"))

        GridTableResults results = (GridTableResults) queryResult.getData();

        println results.toDisplayString()

        then:
        assert targetResult != null
        assert queryResult != null
        assert results.getRowsOfPaddedCells().size() == 2
    }



    def "should query top wtfs across team"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");
        AccountActivationDto zoeProfile = register("zoe@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        calendarService.saveCalendar(1, 3, GeometryClock.createGridTime(ZoomLevel.BLOCK, gridClock.now()))

        LearningCircuitDto circuit1 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit1.getCircuitName())

        switchUser(zoeProfile)

        accountClient.login()

        LearningCircuitDto circuit2 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit2.getCircuitName())

        TeamDto team = teamClient.createTeam("Phoenix")

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto inviteResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.INVITE, "arty", "to", "team"))

        TalkMessageDto targetResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.TARGET, "team", "phoenix"))

        TalkMessageDto queryResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SELECT, "top", "wtfs", "in", "BLOCK"))

        GridTableResults results = (GridTableResults) queryResult.getData();

        println results.toDisplayString()

        then:
        assert targetResult != null
        assert queryResult != null
        assert results.getRowsOfPaddedCells().size() == 2
    }


    def "should get current gridtime clock"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        accountClient.login()

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto gtResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.GT))

        SimpleStatusDto results = (SimpleStatusDto) gtResult.getData();

        println results.message

        then:
        assert results != null
    }

    @Ignore
    def "should goto specific grid tile"() {
        given:

        AccountActivationDto artyProfile = register("arty@dreamscale.io");

        switchUser(artyProfile)

        ConnectionStatusDto loginStatus = accountClient.login()

        LearningCircuitDto circuit1 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit1.getCircuitName())

        LearningCircuitDto circuit2 = circuitClient.startWTF()
        circuitClient.solveWTF(circuit2.getCircuitName())

        NewFlowBatchDto batch = aRandom.flowBatch().timeSent(gridClock.now()).build();
        flowClient.publishBatch(batch)

        gridTimeEngine.setAutorun(false)
        gridTimeEngine.start()

        SystemCmd systemCmd = gridTimeEngine.getSystemCmd()
        systemCmd.runCalendarUntil(gridClock.now().plusDays(3))

        //I need an intention, and some published data to get the torchie cursor to load

        TorchieCmd cmd = gridTimeEngine.getTorchieCmd(loginStatus.getMemberId())

        cmd.runProgram()

        println "CALENDAR DONE!"


        //runProgramForTickCount()

        //claim on submitToLiveQueue if not already claimed, reject if claimed by another server, but okay if claimed by this server

        //manually, has it load the torchie without a claim, without submitting to the queue, but giving back a handle
        //first submitToLiveQueue creates a claim, this way, we can configure the program to run how we want before submitting,
        //or just do manual stuff.

        //but if I don't have a claim, it's possible during this time, another torchie could be spun up,
        //so maybe I claim this one when I get the command, even though its not in queue?  It will expire if it gets abandonded
        //it will expire if it gets evicted too.

        //why is torchie still trying to run?

        //autorun should be disabled, calendar seems to be running anyway?

        //maybe do some wtfs here

        //so what I want to be able to do isn't pause, but block the automated process, pulling,
        //be able to run an empty engine, so I can load manually with my own work that I want to run.

        //disable auto-run.
        //be able to manual enable jobs.
        //then start one torchie via cmd, and run to completion.
        //if the torchie is already running, the additional cmd requests would go in the queue,
        //and the Cmd wrapper would handle any required sync and locking requirements for coordinating
        //with a potentially running process.  For the most part, if all I'm doing is queuing instructions,
        //they would run in sync on the engine.

        //but I should be able to add monitor jobs, and feedback loops, triggers, and so forth.
        //triggers should be able to connect to the talk network, send a message over a terminal circuit, or via notifications
        //so there should be a notification target for the response of the instruction, whenever its done

        //in general torchieCmd should be a sync wrapper that interfaces with the engine and lets you do stuff

        //lets remove the pause commands since it's not really what I want to do

        //gtconfig autorun off

        //watch for tag {tagname} then notify circuit /wtf/{wtfname}
        //watch for threshold wtf > .70 / day
        //watch for box {component}

        //then the thing I'm trying to test is the ability to goto a tile, and see the detail
        //but I need an easy way to configure the data loading and generation of a tile for testing

        //the cmd should be able to create tile reprocessing jobs?



        println "DONNE!"
        //cmd runUntil (tile)

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto queryResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SELECT, "top", "wtfs", "in", "BLOCK"))

        GridTableResults results = (GridTableResults) queryResult.getData();

        println results.toDisplayString()

        then:
        assert queryResult != null
        assert results.getRowsOfPaddedCells().size() == 2
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

        println manual

        println inviteManPage

        println projectManPage

        then:
        assert manual != null
        assert manual.getActivityContexts().size() == 4;

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
