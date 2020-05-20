package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto
import com.dreamscale.gridtime.api.circuit.CircuitMemberStatusDto
import com.dreamscale.gridtime.api.circuit.DescriptionInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitWithMembersDto
import com.dreamscale.gridtime.api.circuit.TagsInputDto
import com.dreamscale.gridtime.api.circuit.TalkMessageDto
import com.dreamscale.gridtime.client.AccountClient
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.client.TalkToClient
import com.dreamscale.gridtime.core.domain.circuit.LearningCircuitState
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountRepository
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.capability.directory.TeamCapability
import com.dreamscale.gridtime.core.service.GridClock
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
    AccountClient accountClient

    @Autowired
    TalkToClient talkClient

    @Autowired
    RootAccountEntity loggedInUser

    @Autowired
    RootAccountRepository rootAccountRepository

    @Autowired
    TeamCapability teamCapability

    @Autowired
    GridClock mockTimeService
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
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

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
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()

        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        then:
        assert activeCircuit != null
        assert activeCircuit.circuitName == circuit.circuitName
        assert activeCircuit.circuitState == "TROUBLESHOOT"

    }


    def 'should update description of a circuit'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()

        LearningCircuitDto updatedCircuit = circuitClient.saveDescriptionForLearningCircuit(circuit.getCircuitName(), new DescriptionInputDto("desc"))

        then:
        assert updatedCircuit != null
        assert updatedCircuit.description == "desc"

    }

    def 'should update tags of a circuit'() {
        given:

        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

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

        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

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

        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

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


    def 'should solve a WTF circuit'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

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
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        LearningCircuitDto abortedCircuit = circuitClient.abortWTF(circuit.getCircuitName());
        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        then:
        assert abortedCircuit != null
        assert abortedCircuit.circuitState == LearningCircuitState.CANCELED.name()
        assert activeCircuit == null
    }


    def "should shelf a circuit with do it later"() {
        given:

        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

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

    def 'should resume a circuit from do it later'() {
        given:

        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

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
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:
        LearningCircuitDto circuitWithRetroStarted = circuitClient.startRetroForWTF(circuit.circuitName)
        then:

        assert circuitWithRetroStarted.circuitState == LearningCircuitState.RETRO.name()

        assert circuitWithRetroStarted.getSolvedCircuitNanoTime() != null
        assert circuitWithRetroStarted.getRetroOpenNanoTime() != null
    }

    def 'should start a retro with already solved circuit'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:
        LearningCircuitDto solvedCircuit = circuitClient.solveWTF(circuit.circuitName)

        LearningCircuitDto circuitWithRetroStarted = circuitClient.startRetroForWTF(circuit.circuitName)

        then:
        assert  solvedCircuit.circuitState == LearningCircuitState.SOLVED.name()
        assert  circuitWithRetroStarted.circuitState == LearningCircuitState.RETRO.name()

        assert circuitWithRetroStarted.getSolvedCircuitNanoTime() != null
        assert circuitWithRetroStarted.getRetroOpenNanoTime() != null
    }

    def 'should close a circuit with a retro started'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        LearningCircuitDto circuitWithRetroStarted = circuitClient.startRetroForWTF(circuit.circuitName)

        LearningCircuitDto closedWTF = circuitClient.closeWTF(circuit.circuitName)

        then:
        assert  circuitWithRetroStarted.circuitState == LearningCircuitState.RETRO.name()
        assert  closedWTF.circuitState == LearningCircuitState.CLOSED.name()

        assert closedWTF.getSolvedCircuitNanoTime() != null
        assert closedWTF.getRetroOpenNanoTime() != null
        assert closedWTF.getCloseCircuitNanoTime() != null

    }


    def 'should reopen a circuit with a retro started'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        LearningCircuitDto circuitWithRetroStarted = circuitClient.startRetroForWTF(circuit.circuitName)

        LearningCircuitDto reopenWTF = circuitClient.reopenWTF(circuit.circuitName)

        then:
        assert  circuitWithRetroStarted.circuitState == LearningCircuitState.RETRO.name()
        assert  reopenWTF.circuitState == LearningCircuitState.TROUBLESHOOT.name()

        assert reopenWTF.getSolvedCircuitNanoTime() == null
        assert reopenWTF.getRetroOpenNanoTime() == null
        assert reopenWTF.getCloseCircuitNanoTime() == null

    }

    def 'should calculate cumulative time'() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        2 * mockTimeService.now() >> time
        1 * mockTimeService.nanoTime() >> timeNano

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        1 * mockTimeService.now() >> time.plusMinutes(5)
        1 * mockTimeService.nanoTime() >> timeNano.plus(30000000000)

        LearningCircuitDto onHoldCircuit = circuitClient.pauseWTFWithDoItLater(circuit.circuitName)

        1 * mockTimeService.now() >> time.plusMinutes(10)
        1 * mockTimeService.nanoTime() >> timeNano.plus(60000000000)

        LearningCircuitDto resumedCircuit = circuitClient.resumeWTF(circuit.circuitName)

        1 * mockTimeService.now() >> time.plusMinutes(15)
        1 * mockTimeService.nanoTime() >> timeNano.plus(90000000000)

        LearningCircuitDto solveWTF = circuitClient.solveWTF(circuit.circuitName)

        then:
        assert  onHoldCircuit.circuitState == LearningCircuitState.ONHOLD.name()
        assert  resumedCircuit.circuitState == LearningCircuitState.TROUBLESHOOT.name()
        assert  solveWTF.circuitState == LearningCircuitState.SOLVED.name()

        assert solveWTF.getTotalCircuitElapsedNanoTime() == 60000000000
        assert solveWTF.getTotalCircuitPausedNanoTime() == 30000000000

    }

    def 'should be able to create WTF circuit and retrieve talk messages'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())
        accountClient.login()

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        talkClient.publishChatToRoom(circuit.getWtfTalkRoomName(), new ChatMessageInputDto("hi"))

        List<TalkMessageDto> wtfMessages = talkClient.getAllTalkMessagesFromRoom(circuit.getWtfTalkRoomName());

        then:
        assert wtfMessages.size() == 2

    }


    def 'should be able to create WTF circuit, post a chat, logout & login, post another chat, and retrieve all messages'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())
        accountClient.login()

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
        assert messagesBeforeLogout.size() == 2
        assert messagesAfterLogin.size() == 2
        assert secondMessage != null
        assert messagesAfterSecondMessage.size() == 3

    }


    def 'should close a circuit thats been solved with no retro'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        LearningCircuitDto solvedWTF = circuitClient.solveWTF(circuit.circuitName)

        LearningCircuitDto closedWTF = circuitClient.closeWTF(circuit.circuitName)

        then:
        assert  solvedWTF.circuitState == LearningCircuitState.SOLVED.name()
        assert  closedWTF.circuitState == LearningCircuitState.CLOSED.name()

        assert closedWTF.getSolvedCircuitNanoTime() != null
        assert closedWTF.getRetroOpenNanoTime() == null
        assert closedWTF.getCloseCircuitNanoTime() != null

    }


    def 'join another persons chat room'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();

        loggedInUser.setId(user1.getRootAccountId())
        accountClient.login()

        LearningCircuitDto user1Circuit = circuitClient.startWTF()
        talkClient.joinExistingRoom(user1Circuit.getWtfTalkRoomName())

        //change active logged in user to a different user within same organization

        OrganizationMemberEntity user2 =  createMemberWithOrgAndTeam();
        loggedInUser.setId(user2.getRootAccountId())

        accountClient.login()

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
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity user1 = createMemberWithOrgAndTeam();

        loggedInUser.setId(user1.getRootAccountId())
        accountClient.login()

        LearningCircuitDto user1Circuit = circuitClient.startWTF()

        //change active logged in user to a different user within same organization

        OrganizationMemberEntity user2 =  createMemberWithOrgAndTeam();
        loggedInUser.setId(user2.getRootAccountId())

        accountClient.login()

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

    def 'join and leave another persons chat room to update room status'() {
        given:
        mockTimeService.now() >> time
        mockTimeService.nanoTime() >> timeNano

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())
        accountClient.login()

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
