package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circuit.*
import com.dreamscale.gridtime.api.team.TeamCircuitDto
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto
import com.dreamscale.gridtime.client.TeamCircuitClient
import com.dreamscale.gridtime.core.domain.member.MasterAccountEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.service.TeamService
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class TeamCircuitResourceSpec extends Specification {

    @Autowired
    TeamCircuitClient teamCircuitClient

    @Autowired
    MasterAccountEntity loggedInUser

    @Autowired
    TeamService teamService

    @Autowired
    TimeService mockTimeService

    def setup() {
        mockTimeService.now() >> LocalDateTime.now()
    }


    def 'should retrieve team circuit'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save();
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().organizationId(org.id).memberId(member.id).teamId(team.id).save();

        when:

        TeamCircuitDto circuit = teamCircuitClient.getMyTeamCircuit();

        then:
        assert circuit != null
        assert circuit.getDefaultRoom().getOwnerName() != null;
        assert circuit.getTeamMembers().size() == 1
    }


    def 'should spin up a new room on the team circuit'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save();
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().organizationId(org.id).memberId(member.id).teamId(team.id).save();

        when:

        //circuit initialized on first retrieve
        TeamCircuitDto circuitInit = teamCircuitClient.getMyTeamCircuit();

        TeamCircuitRoomDto circuitRoom1 = teamCircuitClient.createTeamCircuitRoom(team.name, "angry_teachers");
        TeamCircuitRoomDto circuitRoom2 = teamCircuitClient.createTeamCircuitRoom(team.name, "angry_lemmings");

        TeamCircuitDto circuit = teamCircuitClient.getMyTeamCircuit();

        then:
        assert circuitRoom1 != null
        assert circuitRoom2 != null

        assert circuit.getTeamRooms().size() == 2
    }


    def 'should close a room and remove from the team circuit'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save();
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().organizationId(org.id).memberId(member.id).teamId(team.id).save();

        when:

        //circuit initialized on first retrieve
        TeamCircuitDto circuitInit = teamCircuitClient.getMyTeamCircuit();

        TeamCircuitRoomDto circuitRoom1 = teamCircuitClient.createTeamCircuitRoom(team.name, "angry_teachers");
        TeamCircuitRoomDto circuitRoom2 = teamCircuitClient.createTeamCircuitRoom(team.name, "angry_lemmings");

        teamCircuitClient.closeTeamCircuitRoom(team.name, "angry_teachers")

        TeamCircuitRoomDto closedRoom = teamCircuitClient.getTeamCircuitRoom(team.name, "angry_teachers")

        TeamCircuitDto circuit = teamCircuitClient.getMyTeamCircuit();

        then:
        assert circuit.getTeamRooms().size() == 1

        assert closedRoom.circuitStatus == "CLOSED"

    }

    def 'should add properties to a circuit room'() {
        given:
        MasterAccountEntity account = aRandom.masterAccountEntity().save()
        OrganizationEntity org = aRandom.organizationEntity().save()
        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).masterAccountId(account.id).save()
        loggedInUser.setId(member.getMasterAccountId())

        TeamEntity team = aRandom.teamEntity().organizationId(org.id).save();
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().organizationId(org.id).memberId(member.id).teamId(team.id).save();

        when:

        //circuit initialized on first retrieve
        TeamCircuitDto circuitInit = teamCircuitClient.getMyTeamCircuit();

        TeamCircuitRoomDto circuitRoom = teamCircuitClient.createTeamCircuitRoom(team.name, "angry_teachers");

        TeamCircuitRoomDto updatedRoom = teamCircuitClient.saveDescriptionForTeamCircuitRoom(team.name, "angry_teachers",
                new DescriptionInputDto("ugh"))
        TeamCircuitRoomDto updatedAgain = teamCircuitClient.saveTagsForTeamCircuitRoom(team.name, "angry_teachers",
                new TagsInputDto("tag1", "tag2"))


        TeamCircuitDto circuit = teamCircuitClient.getMyTeamCircuit();

        then:
        assert circuit.getTeamRooms().size() == 1

        TeamCircuitRoomDto room = circuit.getTeamRooms().get(0);

        assert room.description == "ugh"
        assert room.jsonTags != null
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
