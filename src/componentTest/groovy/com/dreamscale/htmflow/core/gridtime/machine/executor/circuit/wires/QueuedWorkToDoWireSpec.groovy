package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.work.ProcessingState
import com.dreamscale.htmflow.core.domain.work.WorkToDoType
import com.dreamscale.htmflow.core.domain.work.WorkItemToAggregateEntity
import com.dreamscale.htmflow.core.domain.work.WorkItemToAggregateRepository
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class QueuedWorkToDoWireSpec extends Specification {


    @Autowired
    QueuedWorkToDoWire queuedWorkToDoWire

    @Autowired
    WorkItemToAggregateRepository workItemToAggregateRepository

    @Autowired
    CalendarService calendarService

    UUID workerId

    def setup() {
        //random builder for work item
        workerId = UUID.randomUUID();
    }

    def "should get next work item in queue"() {
        given:
        GeometryClock.GridTime gridTime = GeometryClock.createGridTime(ZoomLevel.TWENTY, aRandom.localDateTime());
        WorkItemToAggregateEntity workItem = aRandom.workItem().forGridTime(gridTime).processingState(ProcessingState.Ready).save()

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

        UUID teamId = UUID.randomUUID();

        TileStreamEvent torchieStreamEvent1 = new TileStreamEvent(teamId, UUID.randomUUID(), gridTime, WorkToDoType.AggregateToTeam);
        TileStreamEvent torchieStreamEvent2 = new TileStreamEvent(teamId, UUID.randomUUID(), gridTime, WorkToDoType.AggregateToTeam);
        TileStreamEvent torchieStreamEvent3 = new TileStreamEvent(teamId, UUID.randomUUID(), gridTime, WorkToDoType.AggregateToTeam);

        when:
        queuedWorkToDoWire.publish(torchieStreamEvent1)
        queuedWorkToDoWire.publish(torchieStreamEvent2)
        queuedWorkToDoWire.publish(torchieStreamEvent3)

        AggregateStreamEvent aggregateStreamEvent1 = queuedWorkToDoWire.pullNext(workerId)
        AggregateStreamEvent aggregateStreamEvent2 = queuedWorkToDoWire.pullNext(workerId)

        then:
        assert aggregateStreamEvent1 != null
        assert aggregateStreamEvent1.gridTime.toDisplayString() == gridTime.toDisplayString()

        assert aggregateStreamEvent2 == null


        //maybe have a clear events thing, event done thing for reporting status
    }


}
