package com.dreamscale.gridtime.core.machine

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.GridTableResults
import com.dreamscale.gridtime.core.machine.executor.dashboard.DashboardActivityScope
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class GridTimeEngineSpec extends Specification {

    @Autowired
    GridTimeEngine gridTimeEngine

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

    def generateWTFStart(LocalDateTime startTime) {
        WTFFeedMessageEntity wtfMessage = new WTFFeedMessageEntity()
        wtfMessage.setCircuitMessageType(CircuitMessageType.TEAM_WTF_STARTED)
        wtfMessage.setPosition(startTime)
        wtfMessage.setCircuitId(UUID.randomUUID())

        return new FlowableCircuitWTFMessageEvent(wtfMessage)
    }

    def generateWTFEnd(LocalDateTime endTime) {
        WTFFeedMessageEntity wtfMessage = new WTFFeedMessageEntity()
        wtfMessage.setCircuitMessageType(CircuitMessageType.TEAM_WTF_SOLVED)
        wtfMessage.setPosition(endTime)
        wtfMessage.setCircuitId(UUID.randomUUID())

        return new FlowableCircuitWTFMessageEvent(wtfMessage)
    }
}
