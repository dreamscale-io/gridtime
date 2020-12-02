package com.dreamscale.gridtime.resources

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.account.SimpleStatusDto
import com.dreamscale.gridtime.api.circuit.*
import com.dreamscale.gridtime.api.grid.GridTableResults
import com.dreamscale.gridtime.api.journal.IntentionInputDto
import com.dreamscale.gridtime.api.organization.CircuitJoinType
import com.dreamscale.gridtime.api.organization.TeamMemberDto
import com.dreamscale.gridtime.api.project.CreateProjectInputDto
import com.dreamscale.gridtime.api.project.CreateTaskInputDto
import com.dreamscale.gridtime.api.project.ProjectDto
import com.dreamscale.gridtime.api.project.TaskDto
import com.dreamscale.gridtime.api.query.QueryInputDto
import com.dreamscale.gridtime.api.query.TargetType
import com.dreamscale.gridtime.api.query.TimeScope
import com.dreamscale.gridtime.api.status.Status
import com.dreamscale.gridtime.api.team.TeamCircuitDto
import com.dreamscale.gridtime.client.*
import com.dreamscale.gridtime.core.capability.membership.TeamCapability
import com.dreamscale.gridtime.core.capability.system.GridClock
import com.dreamscale.gridtime.core.domain.circuit.LearningCircuitState
import com.dreamscale.gridtime.core.domain.member.*
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@Slf4j
@ComponentTest
class QueryResourceSpec extends Specification {

    @Autowired
    LearningCircuitClient circuitClient

    @Autowired
    QueryClient queryClient

    @Autowired
    JournalClient journalClient

    @Autowired
    RootAccountEntity loggedInUser

    @Autowired
    CalendarService calendarService

    @Autowired
    GridClock gridClock

    OrganizationEntity org
    TeamEntity team

    LocalDateTime time
    Long timeNano


    def setup() {

        time = LocalDateTime.now()
        timeNano = System.nanoTime()

        org = aRandom.organizationEntity().save()
        team = aRandom.teamEntity().organizationId(org.id).save()

    }


    def 'should return wtfs for member'() {
        given:

        OrganizationMemberEntity member = createMemberWithOrgAndTeam();

        loggedInUser.setId(member.getRootAccountId())

        createIntentionForWTFContext()

        calendarService.saveCalendar(1, 3, GeometryClock.createGridTime(ZoomLevel.BLOCK, gridClock.now()))

        when:
        LearningCircuitDto circuit = circuitClient.startWTF()

        circuitClient.solveWTF(circuit.getCircuitName())

        LearningCircuitDto circuit2 = circuitClient.startWTF()

        gridClock.now() //advances the clock 1 min each call
        gridClock.now()
        gridClock.now()

        circuitClient.solveWTF(circuit2.getCircuitName())

        GridTableResults results = queryClient.getTopWTFsForTimeScope(TimeScope.BLOCK.name())

        println results

        then:
        assert results != null
        assert results.getRowsOfPaddedCells().size() == 2

    }


    def 'should return wtfs for team'() {
        given:

        calendarService.saveCalendar(1, 3, GeometryClock.createGridTime(ZoomLevel.BLOCK, gridClock.now()))

        OrganizationMemberEntity member1 = createMemberWithOrgAndTeam();
        OrganizationMemberEntity member2 = createMemberWithOrgAndTeam();

        loggedInUser.setId(member1.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit = circuitClient.startWTF()

        circuitClient.solveWTF(circuit.getCircuitName())

        loggedInUser.setId(member2.getRootAccountId())

        createIntentionForWTFContext()

        LearningCircuitDto circuit2 = circuitClient.startWTF()

        gridClock.now() //advances the clock 1 min each call
        gridClock.now()
        gridClock.now()

        circuitClient.solveWTF(circuit2.getCircuitName())

        when:

        GridTableResults results = queryClient.getTopWTFsForTimeScopeAndTarget(TimeScope.BLOCK.name(), TargetType.TEAM.name(), team.getLowerCaseName())

        println results

        then:
        assert results != null
        assert results.getRowsOfPaddedCells().size() == 2

    }

    private void createIntentionForWTFContext() {
        ProjectDto project = journalClient.findOrCreateProject(new CreateProjectInputDto("proj", "my proj", false));
        TaskDto task = journalClient.findOrCreateTask(project.getId().toString(), new CreateTaskInputDto("task", "my task", false))

        journalClient.createIntention(new IntentionInputDto("Intention", project.getId(), task.getId()))
    }


    private OrganizationMemberEntity createMemberWithOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
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
