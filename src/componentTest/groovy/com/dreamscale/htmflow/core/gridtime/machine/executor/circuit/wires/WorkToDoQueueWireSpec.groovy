package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.member.TeamMemberEntity
import com.dreamscale.htmflow.core.domain.member.TeamMemberRepository
import com.dreamscale.htmflow.core.domain.work.ProcessingState
import com.dreamscale.htmflow.core.domain.work.WorkToDoType
import com.dreamscale.htmflow.core.domain.work.WorkItemToAggregateEntity
import com.dreamscale.htmflow.core.domain.work.WorkItemToAggregateRepository
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.htmflow.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class WorkToDoQueueWireSpec extends Specification {


    @Autowired
    WorkToDoQueueWire queuedWorkToDoWire

    @Autowired
    WorkItemToAggregateRepository workItemToAggregateRepository

    @Autowired
    TeamMemberRepository teamMemberRepository

    @Autowired
    CalendarService calendarService

    UUID workerId

    @Autowired
    TimeService mockTimeService

    def setup() {
        //random builder for work item
        workerId = UUID.randomUUID();

        mockTimeService.now() >> LocalDateTime.now()
    }

    def "should get next work item in queue"() {
        given:

        LocalDateTime oldTime = mockTimeService.now().minusMinutes(55)

        UUID orgId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();
        UUID member3 = UUID.randomUUID();

        teamMemberRepository.save(new TeamMemberEntity(UUID.randomUUID(), orgId, member1, teamId))

        GeometryClock.GridTime gridTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, oldTime);
        WorkItemToAggregateEntity workItem = aRandom.workItem().teamId(teamId).forGridTime(gridTime).processingState(ProcessingState.Ready).save()

        calendarService.saveCalendar(workItem.tileSeq, gridTime)

        when:
        AggregateStreamEvent aggregateStreamEvent = queuedWorkToDoWire.pullNext(workerId)

        then:
        assert aggregateStreamEvent != null
        assert aggregateStreamEvent.gridTime.toDisplayString() == gridTime.toDisplayString()

    }

    def "should write singular events and write aggregate events"() {
        given:
        GeometryClock.GridTime gridTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, aRandom.localDateTime());
        calendarService.saveCalendar(5, gridTime)

        UUID orgId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();
        UUID member3 = UUID.randomUUID();

        teamMemberRepository.save(new TeamMemberEntity(UUID.randomUUID(), orgId, member1, teamId))
        teamMemberRepository.save(new TeamMemberEntity(UUID.randomUUID(), orgId, member2, teamId))
        teamMemberRepository.save(new TeamMemberEntity(UUID.randomUUID(), orgId, member3, teamId))


        TileStreamEvent torchieStreamEvent1 = new TileStreamEvent(teamId, member1, gridTime, WorkToDoType.AggregateToTeam);
        TileStreamEvent torchieStreamEvent2 = new TileStreamEvent(teamId, member2, gridTime, WorkToDoType.AggregateToTeam);
        TileStreamEvent torchieStreamEvent3 = new TileStreamEvent(teamId, member3, gridTime, WorkToDoType.AggregateToTeam);

        when:
        queuedWorkToDoWire.push(torchieStreamEvent1)
        queuedWorkToDoWire.push(torchieStreamEvent2)
        queuedWorkToDoWire.push(torchieStreamEvent3)

        AggregateStreamEvent aggregateStreamEvent1 = queuedWorkToDoWire.pullNext(workerId)
        AggregateStreamEvent aggregateStreamEvent2 = queuedWorkToDoWire.pullNext(workerId)

        then:
        assert aggregateStreamEvent1 != null
        assert aggregateStreamEvent1.gridTime.toDisplayString() == gridTime.toDisplayString()

        assert aggregateStreamEvent2 == null


        //maybe have a clear events thing, event done thing for reporting status
    }


}
