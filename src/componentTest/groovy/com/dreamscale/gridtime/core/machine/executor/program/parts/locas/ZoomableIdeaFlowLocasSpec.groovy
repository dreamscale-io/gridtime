package com.dreamscale.gridtime.core.machine.executor.program.parts.locas

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.grid.Results
import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsEntity
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.api.grid.GridTableResults
import com.dreamscale.gridtime.core.machine.Torchie
import com.dreamscale.gridtime.core.machine.TorchieFactory
import com.dreamscale.gridtime.core.machine.clock.Metronome
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableIdeaFlowLocas
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.sql.Timestamp
import java.time.LocalDateTime

@ComponentTest
class ZoomableIdeaFlowLocasSpec extends Specification {

    @Autowired
    LocasFactory locasFactory

    @Autowired
    GridIdeaFlowMetricsRepository gridMetricsIdeaFlowRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    TorchieFactory torchieFactory

    UUID torchieId
    UUID teamId

    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    ZoomableIdeaFlowLocas ideaflowAggregatorLocas
    Metronome metronome

    Torchie torchie
    UUID orgId


    def setup() {

        orgId = UUID.randomUUID()
        torchieId = UUID.randomUUID()
        teamId = UUID.randomUUID()

        ideaflowAggregatorLocas = locasFactory.createIdeaFlowAggregatorLocas(torchieId);

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        metronome = new Metronome(clockStart)

        torchie = torchieFactory.wireUpMemberTorchie(orgId, torchieId, teamId, clockStart);

        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)
        time3 = clockStart.plusMinutes(60)
        time4 = clockStart.plusMinutes(95)

        Metronome.TickScope tick = metronome.tick();

        calendarService.saveCalendar(1, 12, tick.from);
        calendarService.saveCalendar(1, tick.from.zoomOut());
    }


    def "should aggregate IdeaFlowMetrics by GridTime, i.e. aggregation of Twenties into DayParts"() {
        given:
        InputFeed feed = torchie.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed.addSomeData(generateWTFStart(time1))
        feed.addSomeData(generateWTFEnd(time2))
        feed.addSomeData(generateWTFStart(time3))
        feed.addSomeData(generateWTFEnd(time4))

        for (int i = 0; i < 12; i++) {
            torchie.whatsNext().call()
            println torchie.getLastOutput();
        }

        Metronome.TickScope tick = torchie.getActiveTick();
        Metronome.TickScope aggregateTick = tick.getAggregateTickScopes().get(0);

        when:
        ideaflowAggregatorLocas.runProgram(aggregateTick)

        Results results = ideaflowAggregatorLocas.playAllTracks();
        println results

        then:
        GridIdeaFlowMetricsEntity metrics = gridMetricsIdeaFlowRepository.findByTorchieGridTime(torchieId,
                aggregateTick.getZoomLevel().name(),
                Timestamp.valueOf(aggregateTick.from.getClockTime()))

        assert metrics != null

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
