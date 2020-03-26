package com.dreamscale.gridtime.core.machine.executor.worker

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class SystemWorkPileSpec extends Specification {

    @Autowired
    SystemWorkPile systemWorkPile

    @Autowired
    CircuitActivityDashboard dashboard

    @Autowired
    GridClock mockGridClock

    UUID torchieId
    UUID teamId

    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4



    def setup() {

        torchieId = UUID.randomUUID()
        teamId = UUID.randomUUID()

        clockStart = LocalDateTime.now()
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)
        time3 = clockStart.plusMinutes(60)
        time4 = clockStart.plusMinutes(95)

        mockGridClock.now() >> clockStart

        systemWorkPile.reset()
    }

    def "should spin up work pile on sync and start calendar process"() {
        given:
        systemWorkPile.sync()

        when:

        TickInstructions calendarInstruction1 = systemWorkPile.whatsNext().call();
        TickInstructions calendarInstruction2 = systemWorkPile.whatsNext().call();
        TickInstructions calendarInstruction3 = systemWorkPile.whatsNext().call();

        then:
        assert systemWorkPile.size() == 2
        assert systemWorkPile.hasWork()

        assert calendarInstruction1 != null
        assert calendarInstruction2 != null
        assert calendarInstruction3 != null

    }

//    def "should run dashboard updates on demand via submitting work"() {
//        given:
//        systemWorkPile.sync()
//
//        RefreshDashboardTick refreshTick = new RefreshDashboardTick(dashboard);
//
//        when:
//
//        systemWorkPile.submitWork(ProcessType.Dashboard, refreshTick)
//
//        TickInstructions instruction1 = systemWorkPile.whatsNext().call();
//        TickInstructions instruction2 = systemWorkPile.whatsNext().call();
//
//        then:
//        assert systemWorkPile.size() == 2
//        assert systemWorkPile.hasWork()
//
//        assert instruction1 != null
//        assert instruction2 != null
//        assert instruction1 == refreshTick || instruction2 == refreshTick
//
//    }

    def generateWTFStart(LocalDateTime startTime) {
        WTFFeedMessageEntity wtfMessage = new WTFFeedMessageEntity()
        wtfMessage.setMessageType(CircuitMessageType.WTF_STARTED)
        wtfMessage.setPosition(startTime)
        wtfMessage.setCircuitId(UUID.randomUUID())

        return new FlowableCircuitWTFMessageEvent(wtfMessage)
    }

    def generateWTFEnd(LocalDateTime endTime) {
        WTFFeedMessageEntity wtfMessage = new WTFFeedMessageEntity()
        wtfMessage.setMessageType(CircuitMessageType.WTF_SOLVED)
        wtfMessage.setPosition(endTime)
        wtfMessage.setCircuitId(UUID.randomUUID())

        return new FlowableCircuitWTFMessageEvent(wtfMessage)
    }
}
