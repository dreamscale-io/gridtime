package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto
import com.dreamscale.gridtime.api.circuit.CircuitMemberStatusDto
import com.dreamscale.gridtime.api.circuit.DescriptionInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitMembersDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitWithMembersDto
import com.dreamscale.gridtime.api.circuit.TagsInputDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.api.journal.IntentionInputDto
import com.dreamscale.gridtime.api.organization.TeamMemberDto
import com.dreamscale.gridtime.api.project.CreateProjectInputDto
import com.dreamscale.gridtime.api.project.CreateTaskInputDto
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.project.TaskDto
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.TeamCircuitDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.JournalClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.MemberClient
import com.dreamscale.gridtime.client.TalkToClient
import com.dreamscale.gridtime.client.TeamCircuitClient
import com.dreamscale.gridtime.core.domain.circuit.LearningCircuitState
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.capability.membership.TeamCapability
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@Slf4j
@ComponentTest
class LearningCircuitResourceSpec extends Specification {

    @Autowired
    LearningCircuitClient circuitClient

    @Autowired
    TeamCircuitClient teamCircuitClient

    @Autowired
    JournalClient journalClient

    @Autowired
    MemberClient memberClient

    @Autowired
    AccountClient accountClient

    @Autowired
    TalkToClient talkClient

    @Autowired
    RootAccountEntity loggedInUser

    @Autowired
    RootAccountRepository rootAccountRepository

    @Autowired
    TeamCapability teamCapability

    OrganizationEntity org

    LocalDateTime time
    Long timeNano

    def setup() {

        time = LocalDateTime.now()
        timeNano = System.nanoTime()

        org = aRandom.organizationEntity().save()
    }


    def 'should create a circuit'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()

        then:
        assert circuit != null
        assert circuit.circuitName != null
        assert circuit.circuitState != null
        assert circuit.openTimeStr != null
        assert circuit.circuitState == LearningCircuitState.TROUBLESHOOT.name()

    }

    def 'should start a WTF that becomes the active WTF'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()

        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit()

        then:
        assert activeCircuit != null
        assert activeCircuit.circuitName == circuit.circuitName
        assert activeCircuit.circuitState == "TROUBLESHOOT"

    }

    private void createIntentionForWTFContext() {
        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("proj", "my proj", false));
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("task", "my task", false))

        journalClient.createIntention(new IntentionInputDto("Intention", project.getId(), task.getId()))
    }


    def 'should update description of a circuit'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()

        LearningCircuitDto updatedCircuit = circuitClient.saveDescriptionForLearningCircuit(circuit.getCircuitName(), new DescriptionInputDto("desc"))

        then:
        assert updatedCircuit != null
        assert updatedCircuit.description == "desc"

    }

    def 'should update tags of a circuit'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()

        LearningCircuitDto updatedCircuit = circuitClient.saveTagsForLearningCircuit(circuit.getCircuitName(), new TagsInputDto(Arrays.asList("tag1", "tag2")))

        then:
        assert updatedCircuit != null
        assert updatedCircuit.tags != null
        assert updatedCircuit.tags.size() == 2
    }

    def "should return active circuit"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit1 = circuitClient.startWTF()

        when:
        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit()

        then:
        assert activeCircuit != null
        assert circuit1 != null
        assert circuit1.getCircuitName() == activeCircuit.getCircuitName()
    }


    def "should return all shelved do it later circuits"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit1 = circuitClient.startWTF()
        circuitClient.pauseWTFWithDoItLater(circuit1.getCircuitName())

        LearningCircuitDto circuit2 = circuitClient.startWTF()
        circuitClient.pauseWTFWithDoItLater(circuit2.getCircuitName())

        when:
        List<LearningCircuitDto> circuits = circuitClient.getAllMyDoItLaterCircuits()

        then:
        assert circuits != null
        assert circuits.size() == 2
    }

    def "should clear circuit members on pause so resume includes only owner "() {
        given:

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();
        loggedInUser.setId(user1.getRootAccountId())
        accountClient.login()

        createIntentionForWTFContext()

        LearningCircuitDto user1Circuit = circuitClient.startWTF()

        //change active logged in user to a different user within same organization

        OrganizationMemberEntity user2 =  createMemberWithOrgAndTeam();
        loggedInUser.setId(user2.getRootAccountId())
        accountClient.login()

        circuitClient.joinWTF(user1Circuit.getCircuitName())

        loggedInUser.setId(user1.getRootAccountId())

        when:
        LearningCircuitDto startingCircuit = circuitClient.getCircuitWithAllDetails(user1Circuit.getCircuitName());


        circuitClient.pauseWTFWithDoItLater(user1Circuit.getCircuitName())

        LearningCircuitDto afterPause = circuitClient.getCircuitWithAllDetails(user1Circuit.getCircuitName());

        circuitClient.resumeWTF(user1Circuit.getCircuitName())

        LearningCircuitDto afterResume = circuitClient.getCircuitWithAllDetails(user1Circuit.getCircuitName());

        println startingCircuit.getCircuitParticipants()

        println afterPause.getCircuitParticipants()

        println afterResume.getCircuitParticipants()

        then:

        assert startingCircuit != null
        assert startingCircuit.circuitName == user1Circuit.circuitName
        assert startingCircuit.getCircuitParticipants().size() == 2

        assert afterPause != null
        assert afterPause.getCircuitParticipants().size() == 1

        assert afterResume != null
        assert afterResume.getCircuitParticipants().size() == 1

    }



    def 'should solve a WTF circuit'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:
        LearningCircuitDto solvedCircuit = circuitClient.solveWTF(circuit.getCircuitName());
        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        then:
        assert solvedCircuit != null
        assert solvedCircuit.circuitState == LearningCircuitState.SOLVED.name()
        assert activeCircuit == null
    }

    def 'should cancel a WTF circuit'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        accountClient.login()

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        TeamCircuitDto teamCircuit = teamCircuitClient.getMyHomeTeamCircuit();

        when:

        TeamMemberDto meStatusBefore = memberClient.getMe();

        LearningCircuitDto canceledCircuit = circuitClient.cancelWTF(circuit.getCircuitName());
        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        TeamMemberDto meStatusAfter = memberClient.getMe();

        List<TalkMessageDto> wtfRoomMessages = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName())

        List<TalkMessageDto> teamCircuitMessages = talkClient.getAllTalkMessagesFromRoom(teamCircuit.getDefaultRoom().talkRoomName)

        then:
        assert canceledCircuit != null
        assert canceledCircuit.circuitState == LearningCircuitState.CANCELED.name()
        assert activeCircuit == null

        assert meStatusBefore != null
        assert meStatusBefore.getActiveCircuit() != null

        assert meStatusAfter != null
        assert meStatusAfter.getActiveCircuit() == null

        for (TalkMessageDto message : wtfRoomMessages) {
            println message
        }

        for (TalkMessageDto message : teamCircuitMessages) {
            println message
        }

    }


    def "should pause a circuit with do it later"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:
        LearningCircuitDto circuitDto = circuitClient.pauseWTFWithDoItLater(circuit.getCircuitName());

        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        then:
        assert circuitDto != null
        assert circuitDto.circuitState == LearningCircuitState.ONHOLD.name()
        assert circuitDto.getPauseCircuitNanoTime() != null

        assert activeCircuit == null
    }

    def "should cancel a circuit thats paused"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:
        LearningCircuitDto pausedDto = circuitClient.pauseWTFWithDoItLater(circuit.getCircuitName());

        LearningCircuitDto canceledDto = circuitClient.cancelWTF(circuit.circuitName);

        then:
        assert pausedDto != null
        assert pausedDto.circuitState == LearningCircuitState.ONHOLD.name()
        assert pausedDto.getPauseCircuitNanoTime() != null

        assert canceledDto != null
        assert canceledDto.circuitState == LearningCircuitState.CANCELED.name()
        assert canceledDto.getCancelCircuitNanoTime() != null
    }


    def "should pause and resume and re-pause a circuit and calculate correct elapsed time"() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:
        LearningCircuitDto circuitAfterPause = circuitClient.pauseWTFWithDoItLater(circuit.getCircuitName());

        LearningCircuitDto activeCircuitAfterPause = circuitClient.getActiveCircuit();

        LearningCircuitDto circuitAfterResume = circuitClient.resumeWTF(circuit.circuitName)

        LearningCircuitDto activeCircuitAfterResume = circuitClient.getActiveCircuit();

        LearningCircuitDto circuitAfterPauseAgain = circuitClient.pauseWTFWithDoItLater(circuit.getCircuitName());


        then:
        assert circuitAfterPause != null
        assert circuitAfterPause.circuitState == LearningCircuitState.ONHOLD.name()
        assert circuitAfterPause.getPauseCircuitNanoTime() != null

        assert activeCircuitAfterPause == null

        assert circuitAfterPauseAgain.getTotalCircuitElapsedNanoTime() > 0
    }


    def 'should resume a circuit from do it later'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        LearningCircuitDto pausedCircuit = circuitClient.pauseWTFWithDoItLater(circuit.getCircuitName());

        when:
        LearningCircuitDto resumedCircuit = circuitClient.resumeWTF(circuit.getCircuitName());

        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        then:
        assert pausedCircuit != null
        assert pausedCircuit.circuitState == LearningCircuitState.ONHOLD.name()

        assert resumedCircuit != null
        assert resumedCircuit.circuitState == LearningCircuitState.TROUBLESHOOT.name()

        assert activeCircuit != null
    }

    def 'should start a retro'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        circuitClient.solveWTF(circuit.circuitName)

        when:
        circuitClient.markForReview(circuit.circuitName)

        LearningCircuitDto circuitWithRetroStarted = circuitClient.getCircuitWithAllDetails(circuit.circuitName)

        then:

        assert circuitWithRetroStarted.circuitState == LearningCircuitState.RETRO.name()

        assert circuitWithRetroStarted.getSolvedCircuitNanoTime() != null
        assert circuitWithRetroStarted.getRetroOpenNanoTime() != null
    }


    def 'should start a retro with already solved circuit'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:
        LearningCircuitDto solvedCircuit = circuitClient.solveWTF(circuit.circuitName)

        circuitClient.markForReview(circuit.circuitName)

        LearningCircuitDto circuitWithRetroStarted = circuitClient.getCircuitWithAllDetails(circuit.circuitName)

        then:
        assert  solvedCircuit.circuitState == LearningCircuitState.SOLVED.name()
        assert  circuitWithRetroStarted.circuitState == LearningCircuitState.RETRO.name()

        assert circuitWithRetroStarted.getSolvedCircuitNanoTime() != null
        assert circuitWithRetroStarted.getRetroOpenNanoTime() != null
    }

    def 'should close a circuit with a retro started'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        circuitClient.solveWTF(circuit.circuitName)

        when:

        circuitClient.markForReview(circuit.circuitName)

        LearningCircuitDto circuitWithRetroStarted = circuitClient.getCircuitWithAllDetails(circuit.circuitName)

        circuitClient.markForClose(circuit.circuitName)

        LearningCircuitDto closedWTF = circuitClient.getCircuitWithAllDetails(circuit.circuitName)

        then:
        assert  circuitWithRetroStarted.circuitState == LearningCircuitState.RETRO.name()
        assert  closedWTF.circuitState == LearningCircuitState.CLOSED.name()

        assert closedWTF.getSolvedCircuitNanoTime() != null
        assert closedWTF.getRetroOpenNanoTime() != null
        assert closedWTF.getCloseCircuitNanoTime() != null

    }


    def 'should calculate cumulative time'() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        //1 min go by each tick

        when:

        LearningCircuitDto circuit = circuitClient.startWTF()

        //1

        LearningCircuitDto onHoldCircuit = circuitClient.pauseWTFWithDoItLater(circuit.circuitName)

        //1

        LearningCircuitDto resumedCircuit = circuitClient.resumeWTF(circuit.circuitName)

        //1

        LearningCircuitDto solveWTF = circuitClient.solveWTF(circuit.circuitName)

        then:
        assert  onHoldCircuit.circuitState == LearningCircuitState.ONHOLD.name()
        assert  resumedCircuit.circuitState == LearningCircuitState.TROUBLESHOOT.name()
        assert  solveWTF.circuitState == LearningCircuitState.SOLVED.name()

        println "onHoldCircuit ====" + onHoldCircuit

        println "resumedCircuit ====" + resumedCircuit

        println "solveWTF ====" + solveWTF

        assert solveWTF.getTotalCircuitElapsedNanoTime() == 12000000000 //2 min
        assert solveWTF.getTotalCircuitPausedNanoTime() == 6000000000 //1 min

    }

    def 'should be able to create WTF circuit and retrieve talk messages'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())
        accountClient.login()

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(), new ChatMessageInputDto("hi"))

        List<TalkMessageDto> wtfMessages = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName());

        then:
        assert wtfMessages.size() == 1

    }


    def 'should be able to create WTF circuit, post a chat, logout & login, post another chat, and retrieve all messages'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())
        accountClient.login()

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        TalkMessageDto firstMessage = talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(),
                new ChatMessageInputDto("hi"))

        List<TalkMessageDto> messagesBeforeLogout = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName());

        accountClient.logout();

        accountClient.login();

        List<TalkMessageDto> messagesAfterLogin = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName());

        TalkMessageDto secondMessage = talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(),
                new ChatMessageInputDto("there"))

        List<TalkMessageDto> messagesAfterSecondMessage = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName());

        then:
        log.info("firstMessage = {}", firstMessage)
        log.info("messagesBeforeLogout = {}", messagesBeforeLogout);
        log.info("messagesAfterLogin = {}", messagesAfterLogin);
        log.info("secondMessage = {}", secondMessage);

        log.info("messagesAfterSecondMessage = {}", messagesAfterSecondMessage)

        assert firstMessage != null
        assert messagesBeforeLogout.size() == 1
        assert messagesAfterLogin.size() == 1
        assert secondMessage != null
        assert messagesAfterSecondMessage.size() == 2

    }


    def 'should close a circuit thats been solved with no retro'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        LearningCircuitDto solvedWTF = circuitClient.solveWTF(circuit.circuitName)

        circuitClient.markForClose(circuit.circuitName)

        LearningCircuitDto closedWTF = circuitClient.getCircuitWithAllDetails(circuit.circuitName)

        then:
        assert  solvedWTF.circuitState == LearningCircuitState.SOLVED.name()
        assert  closedWTF.circuitState == LearningCircuitState.CLOSED.name()

        assert closedWTF.getSolvedCircuitNanoTime() != null
        assert closedWTF.getRetroOpenNanoTime() == null
        assert closedWTF.getCloseCircuitNanoTime() != null

    }

    def 'should get a list of circuits ready for review'() {
        given:

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();
        loggedInUser.setId(user1.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto user1Circuit1 = circuitClient.startWTF()

        OrganizationMemberEntity user2 =  createMemberWithOrgAndTeam();
        loggedInUser.setId(user2.getRootAccountId())

        accountClient.login()

        createIntentionForWTFContext()

        when:

        circuitClient.joinWTF(user1Circuit1.getCircuitName())

        LearningCircuitDto user2Circuit = circuitClient.startWTF()

        circuitClient.solveWTF(user2Circuit.getCircuitName())

        loggedInUser.setId(user1.getRootAccountId())

        circuitClient.solveWTF(user1Circuit1.getCircuitName())
        circuitClient.startWTF()

        loggedInUser.setId(user2.getRootAccountId())

        List<LearningCircuitDto> circuitsForRetro = circuitClient.getAllMyRetroCircuits();

        then:

        assert circuitsForRetro.size() == 2
        assert circuitsForRetro.get(0).circuitName == user1Circuit1.circuitName
        assert circuitsForRetro.get(1).circuitName == user2Circuit.circuitName

    }

    def 'should be able to mark my solved WTF for retro'() {
        given:

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();
        loggedInUser.setId(user1.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        circuitClient.solveWTF(circuit.getCircuitName())

        when:

        SimpleStatusDto status = circuitClient.markForReview(circuit.getCircuitName())

        LearningCircuitWithMembersDto circuitWithMembers = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        then:

        assert status.status == Status.VALID

        assert circuitWithMembers.marksForReview == 1

    }

    def 'should be able to mark retro WTF for close and auto-transition circuit state'() {
        given:

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();
        loggedInUser.setId(user1.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        circuitClient.solveWTF(circuit.getCircuitName())

        when:

        LearningCircuitWithMembersDto circuitBeforeMark = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        circuitClient.markForReview(circuit.getCircuitName())

        //should be kicked into retro state when marks over threshold

        LearningCircuitWithMembersDto circuitAfterReviewMark = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        //should be kicked into close state when marks over threshold

        circuitClient.markForClose(circuit.getCircuitName())

        LearningCircuitWithMembersDto circuitAfterCloseMark = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        then:

        assert circuitBeforeMark.circuitState == LearningCircuitState.SOLVED.name()
        assert circuitBeforeMark.getMarksForReview() == 0

        assert circuitAfterReviewMark.circuitState == LearningCircuitState.RETRO.name()
        assert circuitAfterReviewMark.getMarksForReview() == 1
        assert circuitAfterReviewMark.getMarksRequiredForReview() == 1

        assert circuitAfterCloseMark.circuitState == LearningCircuitState.CLOSED.name()
        assert circuitAfterCloseMark.getMarksForClose() == 1
        assert circuitAfterCloseMark.getMarksRequiredForClose() == 1

    }


    def 'should be able to mark WTF multiple times and still get unique tally'() {
        given:

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();
        loggedInUser.setId(user1.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        OrganizationMemberEntity user2 = createMemberWithOrgAndTeam();
        loggedInUser.setId(user2.getRootAccountId())

        circuitClient.joinWTF(circuit.getCircuitName())

        loggedInUser.setId(user1.getRootAccountId())

        circuitClient.solveWTF(circuit.getCircuitName())

        when:

        circuitClient.markForReview(circuit.getCircuitName())

        loggedInUser.setId(user2.getRootAccountId())

        circuitClient.markForReview(circuit.getCircuitName())
        circuitClient.markForReview(circuit.getCircuitName())

        LearningCircuitWithMembersDto circuitWithMembers = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        then:

        assert circuitWithMembers.marksForReview == 2

    }


    def 'join another persons chat room'() {
        given:

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();

        loggedInUser.setId(user1.getRootAccountId())
        accountClient.login()

        createIntentionForWTFContext()

        LearningCircuitDto user1Circuit = circuitClient.startWTF()
        talkClient.joinExistingRoom(user1Circuit.getWtfTalkRoomName())

        //change active logged in user to a different user within same organization

        OrganizationMemberEntity user2 =  createMemberWithOrgAndTeam();
        loggedInUser.setId(user2.getRootAccountId())

        accountClient.login()

        createIntentionForWTFContext()

        when:

        LearningCircuitDto user2Circuit = circuitClient.startWTF()

        circuitClient.joinWTF(user1Circuit.getCircuitName())

        talkClient.joinExistingRoom(user2Circuit.getWtfTalkRoomName())
        talkClient.joinExistingRoom(user1Circuit.getWtfTalkRoomName())

        List<LearningCircuitDto> user2Participating = circuitClient.getAllMyParticipatingCircuits();

        LearningCircuitWithMembersDto user1CircuitFromUser2 = circuitClient.getCircuitWithAllDetails(user1Circuit.getCircuitName())
        LearningCircuitWithMembersDto user2CircuitFromUser2 = circuitClient.getCircuitWithAllDetails(user2Circuit.getCircuitName())

        then:

        assert user2Participating != null
        assert user2Participating.size() == 1 //only includes joined, not my own

        assert user1CircuitFromUser2.getActiveWtfRoomMembers().size() == 2
        assert user2CircuitFromUser2.getActiveWtfRoomMembers().size() == 1
    }


    def 'should join another persons circuit and put existing WTF on hold'() {
        given:

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();

        loggedInUser.setId(user1.getRootAccountId())
        accountClient.login()

        createIntentionForWTFContext()

        LearningCircuitDto user1Circuit = circuitClient.startWTF()

        //change active logged in user to a different user within same organization

        OrganizationMemberEntity user2 =  createMemberWithOrgAndTeam();
        loggedInUser.setId(user2.getRootAccountId())

        accountClient.login()

        createIntentionForWTFContext()

        when:

        LearningCircuitDto user2Circuit = circuitClient.startWTF()

        LearningCircuitDto joinedCircuit = circuitClient.joinWTF(user1Circuit.getCircuitName())

        List<LearningCircuitDto> doItLaterCircuits = circuitClient.getAllMyDoItLaterCircuits();

        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        List<LearningCircuitDto> user2Participating = circuitClient.getAllMyParticipatingCircuits();

        then:

        assert joinedCircuit.getCircuitName() == user1Circuit.getCircuitName()

        assert doItLaterCircuits.size() == 1
        assert doItLaterCircuits.get(0).getCircuitName() == user2Circuit.getCircuitName()

        assert activeCircuit.getCircuitName() == user1Circuit.getCircuitName()

        assert user2Participating.size() == 1 //only includes joined, not my own
    }

    def 'should get all members of a circuit'() {
        given:

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();

        loggedInUser.setId(user1.getRootAccountId())
        accountClient.login()

        createIntentionForWTFContext()

        LearningCircuitDto user1Circuit = circuitClient.startWTF()

        //change active logged in user to a different user within same organization

        OrganizationMemberEntity user2 =  createMemberWithOrgAndTeam();
        loggedInUser.setId(user2.getRootAccountId())

        accountClient.login()

        circuitClient.joinWTF(user1Circuit.getCircuitName())


        when:

        LearningCircuitMembersDto membersDto = circuitClient.getCircuitMembers(user1Circuit.getCircuitName())

        then:

        assert membersDto.getCircuitMembers() != null

        assert membersDto.getCircuitMembers().size() == 2

    }


    def 'join and leave another persons chat room to update room status'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())
        accountClient.login()

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        //change active logged in user to a different user within same organization
        OrganizationMemberEntity otherMember =  createMemberWithOrgAndTeam();
        loggedInUser.setId(otherMember.getRootAccountId())
        accountClient.login()

        when:

        circuitClient.joinWTF(circuit.getCircuitName())

        //below no longer has side-effects, have to join the circuit

        TalkMessageDto joinMessage  = talkClient.joinExistingRoom(circuit.getWtfTalkRoomId().toString())
        TalkMessageDto leaveMessage  = talkClient.leaveExistingRoom(circuit.getWtfTalkRoomId().toString())

        LearningCircuitWithMembersDto fullDetailsDto = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        then:
        assert fullDetailsDto != null
        assert fullDetailsDto.getCircuitParticipants().size() == 2

        //nobody is automatically joined.
        assert fullDetailsDto.getActiveWtfRoomMembers().size() == 0

    }

    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(team.id).organizationId(org.id).memberId(member.id).save()

        return member;

    }

    private CircuitMemberStatusDto getMemberStatusById(List<CircuitMemberStatusDto> members, UUID memberId) {
        for (CircuitMemberStatusDto memberStatus: members) {
            if (memberStatus.memberId == memberId) {
                return memberStatus;
            }
        }
        return null;
    }
}
