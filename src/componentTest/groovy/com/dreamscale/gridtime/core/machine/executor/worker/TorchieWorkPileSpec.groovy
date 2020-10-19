package com.dreamscale.gridtime.core.machine.executor.worker

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.machine.GridTimeEngine
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.CoordinateResults
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions
import com.dreamscale.gridtime.core.machine.executor.dashboard.DashboardActivityScope
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class TorchieWorkPileSpec extends Specification {
    @Autowired
    SystemWorkPile systemWorkPile;

    @Autowired
    TorchieWorkPile torchieWorkPile

    UUID teamId

    @Autowired
    GridTimeEngine gridTimeEngine;


    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3

    OrganizationEntity org

    TeamEntity team

    def setup() {

        teamId = UUID.randomUUID()

        org = aRandom.organizationEntity().save()
        team = aRandom.teamEntity().id(teamId).organizationId(org.id).save()

        gridTimeEngine.reset();
    }

    def "should spin up torchies that havent been processed yet"() {
        given:

        List<OrganizationMemberEntity> teamMembers = createTeamOfMembers(10);

        systemWorkPile.sync()
        TickInstructions calendarInstruction = systemWorkPile.whatsNext().call();

        for (int i = 0; i < 12; i++) {
            TickInstructions moreCalendar = systemWorkPile.whatsNext().call();
        }

        CoordinateResults coordinates = (CoordinateResults) calendarInstruction.getOutputResult();

        clockStart = coordinates.getGridtime().getClockTime()
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(15)
        time3 = clockStart.plusMinutes(43)

        for (OrganizationMemberEntity member : teamMembers) {
            createIntention(member.getId(), time1)
            createActivity(member.getId(), time2, time3)
        }

        when:
        torchieWorkPile.sync()

        TickInstructions torchieInstruction = torchieWorkPile.whatsNext().call();

        println gridTimeEngine.getDashboard(DashboardActivityScope.TORCHIE_DETAIL)

        for (int i = 0; i < 10; i++) {
            torchieInstruction = torchieWorkPile.whatsNext().call();
        }

        println gridTimeEngine.getDashboard(DashboardActivityScope.TORCHIE_DETAIL)

        then:
        assert torchieInstruction != null

        assert torchieWorkPile.size() == 10
        assert torchieWorkPile.hasWork()
    }

    def "should spin up torchies and run programs to completion"() {
        given:

        List<OrganizationMemberEntity> teamMembers = createTeamOfMembers(10);

        systemWorkPile.sync()
        TickInstructions calendarInstruction = systemWorkPile.whatsNext().call();

        for (int i = 0; i < 12; i++) {
            TickInstructions moreCalendar = systemWorkPile.whatsNext().call();
        }

        CoordinateResults coordinates = (CoordinateResults) calendarInstruction.getOutputResult();

        clockStart = coordinates.getGridtime().getClockTime()
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(15)
        time3 = clockStart.plusMinutes(43)

        for (OrganizationMemberEntity member : teamMembers) {
            createIntention(member.getId(), time1)
            createActivity(member.getId(), time2, time3)
        }

        when:
        torchieWorkPile.sync()

        println gridTimeEngine.getDashboard(DashboardActivityScope.TORCHIE_DETAIL)

        TickInstructions torchieInstruction = torchieWorkPile.whatsNext().call();

        for (int i = 0; i < 40; i++) {
            torchieInstruction = torchieWorkPile.whatsNext()

            if (torchieInstruction) {
                torchieInstruction.call()
            }
        }

        println gridTimeEngine.getDashboard(DashboardActivityScope.TORCHIE_DETAIL)

        then:
        assert torchieWorkPile.size() == 0
        assert torchieWorkPile.hasWork() == false
    }

    private OrganizationMemberEntity createMemberWithinOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(teamId).organizationId(org.id).memberId(member.id).save()

        return member;

    }

    List<OrganizationMemberEntity> createTeamOfMembers(int memberCount) {

        List<OrganizationMemberEntity> members = new ArrayList<>()

        for (int i = 0; i < memberCount; i++) {
            //change active logged in user to a different user within same organization
            OrganizationMemberEntity teamMember =  createMemberWithinOrgAndTeam();

            members.add(teamMember)
        }

        return members
    }

    void createIntention(UUID memberId, LocalDateTime time) {
        ProjectEntity projectEntity = aRandom.projectEntity().save();
        TaskEntity taskEntity = aRandom.taskEntity().forProject(projectEntity).save();

        IntentionEntity journalEntry = aRandom.intentionEntity()
                .memberId(memberId)
                .position(time)
                .projectId(projectEntity.id)
                .taskId(taskEntity.id)
                .save()

    }

    void createActivity(UUID memberId, LocalDateTime start, LocalDateTime end) {

        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(end)
                .save()

    }

}
