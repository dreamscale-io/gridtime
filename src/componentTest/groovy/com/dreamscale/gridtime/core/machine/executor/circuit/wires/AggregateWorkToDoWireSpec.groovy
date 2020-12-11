package com.dreamscale.gridtime.core.machine.executor.circuit.wires

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberRepository
import com.dreamscale.gridtime.core.domain.time.GridCalendarEntity
import com.dreamscale.gridtime.core.domain.work.ProcessingState
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository
import com.dreamscale.gridtime.core.domain.work.WorkToDoType
import com.dreamscale.gridtime.core.domain.work.WorkItemToAggregateEntity
import com.dreamscale.gridtime.core.domain.work.WorkItemToAggregateRepository
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.gridtime.core.capability.system.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class AggregateWorkToDoWireSpec extends Specification {


    @Autowired
    AggregateWorkToDoQueueWire queuedWorkToDoWire

    @Autowired
    WorkItemToAggregateRepository workItemToAggregateRepository

    @Autowired
    TeamMemberRepository teamMemberRepository

    @Autowired
    TorchieFeedCursorRepository torchieFeedCursorRepository

    @Autowired
    CalendarService calendarService

    UUID workerId

    @Autowired
    GridClock mockTimeService

    LocalDateTime now;

    def setup() {
        //random builder for work item
        workerId = UUID.randomUUID();

        mockTimeService.now() >> LocalDateTime.now()

        this.now = LocalDateTime.now();
    }

    def "should get next work item in queue"() {
        given:

        LocalDateTime oldTime = mockTimeService.now().minusMinutes(55)

        UUID orgId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();
        UUID member3 = UUID.randomUUID();

        teamMemberRepository.save(new TeamMemberEntity(UUID.randomUUID(), orgId, member1, teamId, now))

        GeometryClock.GridTime gridTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, oldTime);

        GridCalendarEntity calendarEntity = calendarService.saveCalendar(1, gridTime)

        WorkItemToAggregateEntity workItem = aRandom.workItem().teamId(teamId).calendarId(calendarEntity.getId()).processingState(ProcessingState.Ready).save()

        TorchieFeedCursorEntity cursor = aRandom.torchieFeedCursor().torchieId(member1).save()


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

        teamMemberRepository.save(new TeamMemberEntity(UUID.randomUUID(), orgId, member1, teamId, now))
        teamMemberRepository.save(new TeamMemberEntity(UUID.randomUUID(), orgId, member2, teamId, now))
        teamMemberRepository.save(new TeamMemberEntity(UUID.randomUUID(), orgId, member3, teamId, now))

        aRandom.torchieFeedCursor().torchieId(member1).save()
        aRandom.torchieFeedCursor().torchieId(member2).save()
        aRandom.torchieFeedCursor().torchieId(member3).save()

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
