package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.CircuitMemberStatusDto
import com.dreamscale.gridtime.api.circuit.DescriptionInputDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto
import com.dreamscale.gridtime.api.circuit.LearningCircuitWithMembersDto
import com.dreamscale.gridtime.api.circuit.TagsInputDto
import com.dreamscale.gridtime.client.LearningCircuitClient
import com.dreamscale.gridtime.core.domain.circuit.CircuitStatus
import com.dreamscale.gridtime.core.domain.circuit.RoomMemberStatus
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity

import com.dreamscale.gridtime.core.service.TeamService
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class LearningCircuitResourceSpec extends Specification {

    @Autowired
    LearningCircuitClient circuitClient

    @Autowired
    MasterAccountEntity loggedInUser

    @Autowired
    TeamService teamService

    @Autowired
    TimeService mockTimeService

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
    }


    def 'should create a circuit'() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        when:
        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        then:
        assert circuit != null
        assert circuit.circuitName != null
        assert circuit.circuitStatus != null
        assert circuit.openTimeStr != null

    }

    def 'should update description of a circuit'() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        when:
        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        LearningCircuitDto updatedCircuit = circuitClient.saveDescriptionForLearningCircuit(circuit.getCircuitName(), new DescriptionInputDto("desc"))

        then:
        assert updatedCircuit != null
        assert updatedCircuit.description == "desc"

    }

    def 'should update tags of a circuit'() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        when:
        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        LearningCircuitDto updatedCircuit = circuitClient.saveTagsForLearningCircuit(circuit.getCircuitName(), new TagsInputDto(Arrays.asList("tag1", "tag2")))

        then:
        assert updatedCircuit != null
        assert updatedCircuit.tags != null
        assert updatedCircuit.tags.size() == 2
    }

    def "should return active circuit"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        LearningCircuitDto circuit1 = circuitClient.startLearningCircuitForWTF()

        when:
        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit()

        then:
        assert activeCircuit != null
        assert circuit1 != null
        assert circuit1.getCircuitName() == activeCircuit.getCircuitName()
    }


    def "should return all shelved do it later circuits"() {
        given:

        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        LearningCircuitDto circuit1 = circuitClient.startLearningCircuitForWTF()
        circuitClient.putCircuitOnHoldWithDoItLater(circuit1.getCircuitName())

        LearningCircuitDto circuit2 = circuitClient.startLearningCircuitForWTF()
        circuitClient.putCircuitOnHoldWithDoItLater(circuit2.getCircuitName())

        when:
        List<LearningCircuitDto> circuits = circuitClient.getAllMyDoItLaterCircuits()

        then:
        assert circuits != null
        assert circuits.size() == 2
    }


    def 'should close a circuit'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        when:

        LearningCircuitDto closedCircuit = circuitClient.closeExistingCircuit(circuit.getCircuitName());
        LearningCircuitDto activeCircuit = circuitClient.getActiveCircuit();

        then:
        assert closedCircuit != null
        assert activeCircuit == null
    }


    def "should shelf a circuit with do it later"() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        when:

        LearningCircuitDto circuitDto = circuitClient.putCircuitOnHoldWithDoItLater(circuit.getCircuitName());

        then:
        assert circuitDto != null
        assert circuitDto.circuitStatus == CircuitStatus.ONHOLD.name()
    }

    def 'should resume a circuit from do it later'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        LearningCircuitDto circuitShelved = circuitClient.putCircuitOnHoldWithDoItLater(circuit.getCircuitName());

        when:
        LearningCircuitDto resumedCircuit = circuitClient.resumeCircuit(circuit.getCircuitName());

        then:
        assert resumedCircuit != null
        assert resumedCircuit.circuitStatus == CircuitStatus.ACTIVE.name()
    }

    def 'should start a retro'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        when:
        LearningCircuitDto circuitWithRetroStarted = circuitClient.startRetroForWTF(circuit.circuitName)

        then:

        assert circuitWithRetroStarted.getRetroStartedTime() != null
    }

    def 'join another persons chat room'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        //change active logged in user to a different user within same organization
        MasterAccountEntity otherAccount = aRandom.masterAccountEntity().save()
        OrganizationMemberEntity otherMember =  aRandom.memberEntity().organizationId(org.id).masterAccountId(otherAccount.id).save()
        loggedInUser.setId(otherMember.getMasterAccountId())

        when:

        LearningCircuitDto myOwnCircuit = circuitClient.startLearningCircuitForWTF()
        LearningCircuitDto circuitJoined = circuitClient.joinExistingCircuit(circuit.getCircuitName())

        List<LearningCircuitDto> participatingCircuits = circuitClient.getAllMyParticipatingCircuits();

        then:
        assert participatingCircuits != null
        assert participatingCircuits.size() == 2
    }

    def 'join and leave another persons chat room to update room status'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity me = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(me.getMasterAccountId())

        LearningCircuitDto circuit = circuitClient.startLearningCircuitForWTF()

        //change active logged in user to a different user within same organization
        MasterAccountEntity otherAccount = aRandom.masterAccountEntity().save()
        OrganizationMemberEntity otherMember =  aRandom.memberEntity().organizationId(org.id).masterAccountId(otherAccount.id).save()
        loggedInUser.setId(otherMember.getMasterAccountId())

        when:

        LearningCircuitDto circuitJoined = circuitClient.joinExistingCircuit(circuit.getCircuitName())
        LearningCircuitDto circuitLeft = circuitClient.leaveExistingCircuit(circuit.getCircuitName())

        LearningCircuitWithMembersDto fullDetailsDto = circuitClient.getCircuitWithAllDetails(circuit.getCircuitName());

        then:
        assert fullDetailsDto != null
        assert fullDetailsDto.getCircuitMembers().size() == 2

        CircuitMemberStatusDto memberMe = getMemberStatusById(fullDetailsDto.getCircuitMembers(), me.id);
        CircuitMemberStatusDto memberOther = getMemberStatusById(fullDetailsDto.getCircuitMembers(), otherMember.id);


        assert memberMe != null
        assert memberOther != null

        assert memberMe.wtfRoomStatus == RoomMemberStatus.ACTIVE.name()
        assert memberOther.wtfRoomStatus == RoomMemberStatus.INACTIVE.name()


    }

    CircuitMemberStatusDto getMemberStatusById(List<CircuitMemberStatusDto> members, UUID memberId) {
        for (CircuitMemberStatusDto memberStatus: members) {
            if (memberStatus.memberId == memberId) {
                return memberStatus;
            }
        }
        return null;
    }
}
