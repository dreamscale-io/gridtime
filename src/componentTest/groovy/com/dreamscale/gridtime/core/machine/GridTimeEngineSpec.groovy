package com.dreamscale.gridtime.core.machine

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.CoordinateResults
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.GridTableResults
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions
import com.dreamscale.gridtime.core.machine.executor.dashboard.DashboardActivityScope
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom
import static com.dreamscale.gridtime.core.CoreARandom.aRandom
import static com.dreamscale.gridtime.core.CoreARandom.aRandom
import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class GridTimeEngineSpec extends Specification {

    @Autowired
    GridTimeEngine gridTimeEngine

    @Autowired
    GridTimeWorkPile gridTimeWorkPile

    UUID torchieId
    UUID teamId

    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    OrganizationEntity org

    TeamEntity team

    def setup() {

        torchieId = UUID.randomUUID()
        teamId = UUID.randomUUID()

        org = aRandom.organizationEntity().save()
        team = aRandom.teamEntity().id(teamId).organizationId(org.id).save()

        clockStart = LocalDateTime.now()
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)
        time3 = clockStart.plusMinutes(60)
        time4 = clockStart.plusMinutes(95)

        gridTimeEngine.reset()
        //because the cleanup script that deletes the data in tables gets out of sync with the engine state
        gridTimeWorkPile.reset()
    }

    def "should spin up engine and run for 1000 ticks"() {
        given:

        gridTimeEngine.configureDoneAfterTicks(1000)

        gridTimeEngine.start()

        gridTimeEngine.waitForDone()
        gridTimeEngine.shutdown()

        when:
        GridTableResults results = gridTimeEngine.getDashboardStatus(DashboardActivityScope.GRID_SUMMARY)
        println results

        then:
        assert results != null
    }

    def "should timeout engine and exit cleanly with logged exceptions"() {
        given:

        gridTimeEngine.configureDoneAfterTicks(200)

        gridTimeEngine.start()

        gridTimeEngine.waitForDone(1000)
        gridTimeEngine.shutdown()

        when:
        GridTableResults results = gridTimeEngine.getDashboardStatus(DashboardActivityScope.GRID_SUMMARY)
        println results

        then:
        assert results != null
    }

    def "should spin up torchies in the engine and create detailed activity report"() {
        given:

        gridTimeEngine.configureDoneAfterTicks(100)
        gridTimeWorkPile.pauseSystemJobs()

        List<OrganizationMemberEntity> teamMembers = createTeamOfMembers(10);

        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(15)
        time3 = clockStart.plusMinutes(60)

        for (OrganizationMemberEntity member : teamMembers) {
            createIntention(member.getId(), time1)
            createActivity(member.getId(), time2, time3)
        }

        when:

        gridTimeEngine.start()

        gridTimeEngine.waitForDone()
        gridTimeEngine.shutdown()

        GridTableResults results = gridTimeEngine.getDashboardStatus(DashboardActivityScope.GRID_SUMMARY)
        println results

        then:
        assert results != null
    }


    def "should reset engine after done and run for another 10 ticks"() {
        given:
        gridTimeEngine.configureDoneAfterTicks(10)

        gridTimeEngine.start()

        gridTimeEngine.waitForDone()
        gridTimeEngine.reset()
        gridTimeEngine.waitForDone()
        gridTimeEngine.reset()
        gridTimeEngine.waitForDone()
        gridTimeEngine.reset()
        gridTimeEngine.waitForDone()

        gridTimeEngine.shutdown()

        when:
        GridTableResults results = gridTimeEngine.getDashboardStatus(DashboardActivityScope.GRID_SUMMARY)
        println results

        then:
        assert results != null
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
