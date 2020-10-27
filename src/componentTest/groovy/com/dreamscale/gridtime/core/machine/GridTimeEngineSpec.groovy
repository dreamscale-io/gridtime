package com.dreamscale.gridtime.core.machine

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.core.capability.system.GridClock
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.gridtime.core.domain.flow.FlowActivityRepository
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.domain.member.*
import com.dreamscale.gridtime.api.grid.GridTableResults
import com.dreamscale.gridtime.core.machine.executor.dashboard.DashboardActivityScope
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity
import org.junit.Ignore
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class GridTimeEngineSpec extends Specification {

    @Autowired
    GridTimeEngine gridTimeEngine

    @Autowired
    GridTimeWorkPile gridTimeWorkPile

    @Autowired
    FlowActivityRepository flowActivityRepository

    @Autowired
    GridClock gridClock

    UUID torchieId
    UUID teamId

    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    OrganizationEntity org

    TeamEntity team
    ProjectEntity projectEntity

    def setup() {

        torchieId = UUID.randomUUID()
        teamId = UUID.randomUUID()

        projectEntity = aRandom.projectEntity().save();

        org = aRandom.organizationEntity().save()
        team = aRandom.teamEntity().id(teamId).organizationId(org.id).save()

        clockStart = gridClock.getGridStart()
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)
        time3 = clockStart.plusMinutes(60)
        time4 = clockStart.plusMinutes(95)

        gridTimeEngine.reset()
    }

    def cleanup() {
        gridTimeEngine.shutdown()
    }

    def "should spin up engine and run for 200 ticks"() {
        given:

        gridTimeEngine.configureDaysToKeepAhead(30)
        gridTimeEngine.configureDoneAfterTicks(200)

        gridTimeEngine.start()

        gridTimeEngine.waitForDone()
        gridTimeEngine.shutdown()

        when:
        GridTableResults results = gridTimeEngine.getDashboard(DashboardActivityScope.GRID_SUMMARY)
        println results

        then:
        assert results != null
    }

    def "should timeout engine and exit cleanly with logged exceptions"() {
        given:

        gridTimeEngine.configureDoneAfterTicks(200)

        SimpleStatusDto status = gridTimeEngine.start()

        println status;

        gridTimeEngine.waitForDone(1000)
        gridTimeEngine.shutdown()

        when:
        GridTableResults results = gridTimeEngine.getDashboard(DashboardActivityScope.GRID_SUMMARY)
        println results

        then:
        assert results != null
    }

    def "should spin up torchies in the engine and create detailed activity report"() {
        given:

        gridTimeEngine.configureDaysToKeepAhead(1)
        gridTimeEngine.configureDoneAfterTicks(180)

        List<OrganizationMemberEntity> teamMembers = createTeamOfMembers(10);

        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(93)
        time3 = clockStart.plusMinutes(113)
        time4 = clockStart.plusMinutes(200)

        for (OrganizationMemberEntity member : teamMembers) {
            createIntention(member.getId(), time1)
            createActivity(member.getId(), time2, "/box/file1.java")
        }

        Iterator teamIter = teamMembers.iterator();
        for (int i = 0; i < 5; i ++) {
            OrganizationMemberEntity member = teamIter.next();

            createIntention(member.getId(), time3)
            createActivity(member.getId(), time4, "/box/file2.java")
        }

        when:

        gridTimeEngine.start()

        gridTimeEngine.waitForDone()
        gridTimeEngine.shutdown()

        GridTableResults results = gridTimeEngine.getDashboard(DashboardActivityScope.GRID_SUMMARY)
        println results

        GridTableResults results2 = gridTimeEngine.getDashboard(DashboardActivityScope.TORCHIE_DETAIL)
        println results2

        GridTableResults results1 = gridTimeEngine.getDashboard(DashboardActivityScope.PLEXER_DETAIL)
        println results1

        GridTableResults results3 = gridTimeEngine.getDashboard(DashboardActivityScope.ALL_DETAIL)
        println results3

        then:
        assert results1 != null
        assert results2 != null
        assert results3 != null

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
        GridTableResults results = gridTimeEngine.getDashboard(DashboardActivityScope.GRID_SUMMARY)
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

        TaskEntity taskEntity = aRandom.taskEntity().forProject(projectEntity).save();

        IntentionEntity journalEntry = aRandom.intentionEntity()
                .memberId(memberId)
                .position(time)
                .projectId(projectEntity.id)
                .taskId(taskEntity.id)
                .save()

    }

    void createActivity(UUID memberId, LocalDateTime start, String filePath) {

        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(start.plusMinutes(5))
                .build()

        flowActivityEntity.setMetadataField(FlowActivityMetadataField.filePath, filePath)

        flowActivityRepository.save(flowActivityEntity)
    }

    FlowableFlowActivity generateFileActivity(UUID memberId, LocalDateTime start, String filePath) {
        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(start.plusMinutes(3))
                .build();

        flowActivityEntity.setMetadataField(FlowActivityMetadataField.filePath, filePath)

        return new FlowableFlowActivity(flowActivityEntity);

    }
}
