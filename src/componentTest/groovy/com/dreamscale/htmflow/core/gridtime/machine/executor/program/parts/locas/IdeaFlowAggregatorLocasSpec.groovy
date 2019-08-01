package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.circle.CircleMessageType
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsEntity
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsRepository
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.htmflow.core.gridtime.machine.Torchie
import com.dreamscale.htmflow.core.gridtime.machine.TorchieFactory
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategyFactory
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.flowable.FlowableCircleMessageEvent
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyTorchieState
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.InputFeed
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.sql.Timestamp
import java.time.LocalDateTime

@ComponentTest
class IdeaFlowAggregatorLocasSpec extends Specification {

    @Autowired
    LocasFactory locasFactory

    @Autowired
    GridIdeaFlowMetricsRepository gridMetricsIdeaFlowRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    TorchieFactory torchieFactory

    UUID torchieId

    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    IdeaFlowAggregatorLocas ideaflowAggregatorLocas
    Metronome metronome

    Torchie torchie


    def setup() {

        torchieId = UUID.randomUUID()

        ideaflowAggregatorLocas = locasFactory.createIdeaFlowAggregatorLocas(torchieId);

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        metronome = new Metronome(clockStart)

        torchie = torchieFactory.wireUpTeamMemberTorchie(UUID.randomUUID(), torchieId, clockStart);

        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)
        time3 = clockStart.plusMinutes(60)
        time4 = clockStart.plusMinutes(95)

        Metronome.Tick tick = metronome.tick();

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
            println torchie.playAllTracks();
        }

        Metronome.Tick tick = torchie.getActiveTick();
        Metronome.Tick aggregateTick = tick.aggregateTicks.get(0);

        when:
        ideaflowAggregatorLocas.runProgram(aggregateTick)

        MusicGridResults results = ideaflowAggregatorLocas.playAllTracks();
        println results

        then:
        GridIdeaFlowMetricsEntity metrics = gridMetricsIdeaFlowRepository.findByTorchieGridTime(torchieId,
                aggregateTick.getZoomLevel().name(),
                Timestamp.valueOf(aggregateTick.from.getClockTime()))

        assert metrics != null

    }

    def generateWTFStart(LocalDateTime startTime) {
        CircleFeedMessageEntity wtfMessage = new CircleFeedMessageEntity()
        wtfMessage.setMessageType(CircleMessageType.CIRCLE_START)
        wtfMessage.setPosition(startTime)
        wtfMessage.setCircleId(UUID.randomUUID())

        return new FlowableCircleMessageEvent(wtfMessage)
    }

    def generateWTFEnd(LocalDateTime endTime) {
        CircleFeedMessageEntity wtfMessage = new CircleFeedMessageEntity()
        wtfMessage.setMessageType(CircleMessageType.CIRCLE_CLOSED)
        wtfMessage.setPosition(endTime)
        wtfMessage.setCircleId(UUID.randomUUID())

        return new FlowableCircleMessageEvent(wtfMessage)
    }
}
