package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
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
import com.dreamscale.gridtime.core.capability.directory.TeamMembershipCapability
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

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
    TeamMembershipCapability teamService

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

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        //change active logged in user to a different user within same organization

        OrganizationMemberEntity otherMember =  createMemberWithOrgAndTeam();
        loggedInUser.setId(otherMember.getRootAccountId())

        LearningCircuitDto otherMemberCircuit = circuitClient.startWTF()
        when:

        TalkMessageDto joinMessage = talkClient.joinExistingRoom(circuit.getWtfTalkRoomName())

        List<LearningCircuitDto> participatingCircuits = circuitClient.getAllMyParticipatingCircuits();

        then:

        assert joinMessage != null

        assert participatingCircuits != null
        assert participatingCircuits.size() == 2
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

        TalkMessageDto joinMessage  = talkClient.joinExistingRoom(circuit.getWtfTalkRoomName())
        TalkMessageDto leaveMessage  = talkClient.leaveExistingRoom(circuit.getWtfTalkRoomName())

        LearningCircuitWithMembersDto fullDetailsDto = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        then:
        assert fullDetailsDto != null
        assert fullDetailsDto.getCircuitParticipants().size() == 2

        assert fullDetailsDto.getActiveWtfRoomMembers().size() == 1

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
