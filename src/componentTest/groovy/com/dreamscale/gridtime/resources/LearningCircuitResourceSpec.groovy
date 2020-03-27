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
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusEntity
import com.dreamscale.gridtime.core.domain.circuit.CircuitState
import com.dreamscale.gridtime.core.domain.circuit.RoomMemberStatus
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

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
        mockTimeService.nanoTime() >> System.nanoTime();

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

    }



    def 'should update description of a circuit'() {
        given:

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

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit1 = circuitClient.startWTF()
        circuitClient.putWTFOnHoldWithDoItLater(circuit1.getCircuitName())

        LearningCircuitDto circuit2 = circuitClient.startWTF()
        circuitClient.putWTFOnHoldWithDoItLater(circuit2.getCircuitName())

        when:
        List<LearningCircuitDto> circuits = circuitClient.getAllMyDoItLaterCircuits()

        then:
        assert circuits != null
        assert circuits.size() == 2
    }


    def 'should solve a WTF circuit'() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        LearningCircuitDto closedCircuit = circuitClient.solveWTF(circuit.getCircuitName());
        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        then:
        assert closedCircuit != null
        assert activeCircuit == null
    }

    def 'should cancel a WTF circuit'() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        LearningCircuitDto abortedCircuit = circuitClient.abortWTF(circuit.getCircuitName());
        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        then:
        assert abortedCircuit != null
        assert abortedCircuit.circuitState == CircuitState.CANCELED.name()
        assert activeCircuit == null
    }


    def "should shelf a circuit with do it later"() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:

        LearningCircuitDto circuitDto = circuitClient.putWTFOnHoldWithDoItLater(circuit.getCircuitName());

        then:
        assert circuitDto != null
        assert circuitDto.circuitState == CircuitState.ONHOLD.name()
    }

    def 'should resume a circuit from do it later'() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        LearningCircuitDto circuitShelved = circuitClient.putWTFOnHoldWithDoItLater(circuit.getCircuitName());

        when:
        LearningCircuitDto resumedCircuit = circuitClient.resumeWTF(circuit.getCircuitName());

        then:
        assert resumedCircuit != null
        assert resumedCircuit.circuitState == CircuitState.TROUBLESHOOT.name()
    }

    def 'should start a retro'() {
        given:
        OrganizationMemberEntity member = createMemberWithOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        LearningCircuitDto circuit = circuitClient.startWTF()

        when:
        LearningCircuitDto circuitWithRetroStarted = circuitClient.startRetroForWTF(circuit.circuitName)

        then:

        assert circuitWithRetroStarted.getRetroOpenNanoTime() != null
    }

    def 'join another persons chat room'() {
        given:
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
