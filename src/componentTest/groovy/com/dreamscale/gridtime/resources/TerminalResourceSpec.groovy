package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.*
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchDto
import com.dreamscale.gridtime.api.grid.GridStatus
import com.dreamscale.gridtime.api.grid.GridStatusSummaryDto
import com.dreamscale.gridtime.api.grid.GridTableResults
import com.dreamscale.gridtime.api.grid.GridTileDto
import com.dreamscale.gridtime.api.journal.IntentionInputDto
import com.dreamscale.gridtime.api.journal.JournalEntryDto
import com.dreamscale.gridtime.api.organization.OrganizationSubscriptionDto
import com.dreamscale.gridtime.api.organization.SubscriptionInputDto
import com.dreamscale.gridtime.api.project.CreateProjectInputDto
import com.dreamscale.gridtime.api.project.CreateTaskInputDto
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.project.TaskDto
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
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
import org.dreamscale.exception.BadRequestException
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    FeatureCache featureCache

    String activationCode = null;

    def setup() {
        mockGridClock.now() >> LocalDateTime.now()
        mockGridClock.nanoTime() >> System.nanoTime()

        gridTimeEngine.reset()

        featureCache.clear();
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

    def "should goto and explore generated grid tiles"() {
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

        ProjectDto proj = journalClient.findOrCreateProject(new CreateProjectInputDto("proj", "proj", false))
        TaskDto task = journalClient.findOrCreateTask(proj.getId().toString(), new CreateTaskInputDto("task", "task", false))
        JournalEntryDto intention = journalClient.createIntention(new IntentionInputDto("intention", proj.getId(), task.getId()))

        gridTimeEngine.configureDoneAfterTicks(1000)
        gridTimeEngine.setAutorun(false)
        gridTimeEngine.start()

        LocalDateTime runUntil = gridClock.now().plusDays(1);

        SystemCmd systemCmd = gridTimeEngine.getSystemCmd()
        systemCmd.runCalendarUntil(runUntil)

        TorchieCmd torchieCmd = gridTimeEngine.getTorchieCmd(loginStatus.getMemberId())
        torchieCmd.runProgramUntil(runUntil)

        GridTableResults table = torchieCmd.playTile()
        println table

        gridTimeEngine.releaseTorchieCmd(torchieCmd)

        GeometryClock.GridTime day = GeometryClock.createGridTime(ZoomLevel.DAY, circuit1.getOpenTime());

        when:

        TerminalCircuitDto circuit = terminalClient.createCircuit()

        TalkMessageDto queryResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.SELECT, "top", "wtfs", "in", day.getFormattedCoords()))

        GridTableResults results = (GridTableResults) queryResult.getData();

        println results
        String cellValue = results.getCell(0, 2);

        TalkMessageDto gotoLocResults = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.GOTO, cellValue))

        GridTileDto tile = (GridTileDto) gotoLocResults.getData();

        println tile

        GridTileDto tile2

        for (int i = 0; i < 3; i++) {
            TalkMessageDto nextResult = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.PAN, "right"))

            tile2 = (GridTileDto) nextResult.getData();

            println tile2

            TalkMessageDto regenResults = terminalClient.runCommand(circuit.getCircuitName(), new CommandInputDto(Command.REGEN))

            GridTileDto regenTile = (GridTileDto) regenResults.getData();

            println "*** REGEN ***"
            println regenTile
        }

        gridTimeEngine.shutdown()

        then:

        assert tile != null
        assert tile2 != null
        assert tile.size() == 14;
        assert tile2.size() == 11;
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
