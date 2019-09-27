package com.dreamscale.gridtime.core.machine.executor.program.parts.locas

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circle.CircleMessageType
import com.dreamscale.gridtime.core.domain.circle.CircleFeedMessageEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsEntity
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsRepository
import com.dreamscale.gridtime.core.machine.Torchie
import com.dreamscale.gridtime.core.machine.TorchieFactory
import com.dreamscale.gridtime.core.machine.clock.Metronome
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircleMessageEvent
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableBoxLocas
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.sql.Timestamp
import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class ZoomableBoxLocasSpec extends Specification {

    @Autowired
    LocasFactory locasFactory

    @Autowired
    GridBoxMetricsRepository gridBoxMetricsRepository;

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

    ZoomableBoxLocas boxAggregatorLocas
    Metronome metronome

    Torchie torchie


    def setup() {

        torchieId = UUID.randomUUID()
        teamId = UUID.randomUUID();

        boxAggregatorLocas = locasFactory.createBoxAggregatorLocas(teamId,  torchieId);

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        metronome = new Metronome(clockStart)

        torchie = torchieFactory.wireUpMemberTorchie(teamId, torchieId, clockStart);

        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)
        time3 = clockStart.plusMinutes(60)
        time4 = clockStart.plusMinutes(95)

        Metronome.TickScope tick = metronome.tick();

        calendarService.saveCalendar(1, 12, tick.from);
        calendarService.saveCalendar(1, tick.from.zoomOut());

    }

    def "should produce BoxMetrics for each GridTile"() {
        given:
        InputFeed wtfFeed = torchie.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        wtfFeed.addSomeData(generateWTFStart(time1))
        wtfFeed.addSomeData(generateWTFEnd(time2))

        InputFeed fileActivityFeed = torchie.getInputFeed(FeedStrategyFactory.FeedType.FILE_ACTIVITY_FEED)
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time1, time1.plusMinutes(3)))

        //make a tile, make sure box metrics generated
        when:
        torchie.pullNext().call();
        println torchie.playAllTracks();

        then:
        List<GridBoxMetricsEntity> boxMetrics = gridBoxMetricsRepository.findByTorchieGridTime(torchieId,
                torchie.getActiveTick().getZoomLevel().name(),
                Timestamp.valueOf(torchie.getActiveTick().getFrom().getClockTime()))

        assert boxMetrics != null;
        assert boxMetrics.size() > 0;

    }

    def "should aggregate BoxMetrics across Time, Grouped by Box"() {
        given:
            //create box metrics, for the specified calendar times, create grid features for each
        when:
            println "ji"
            //when cloud breathe in/out
        then:
        assert true
            //generate aggregate metrics, grouped by box
    }


//
//    def "should aggregate BoxMetrics by Box"() {
//        given:
//        InputFeed feed = torchie.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
//        feed.addSomeData(generateWTFStart(time1))
//        feed.addSomeData(generateWTFEnd(time2))
//        feed.addSomeData(generateWTFStart(time3))
//        feed.addSomeData(generateWTFEnd(time4))
//
//        for (int i = 0; i < 12; i++) {
//            torchie.whatsNext().call()
//            println torchie.playAllTracks();
//        }
//
//        Metronome.Tick tick = torchie.getActiveTick();
//        Metronome.Tick aggregateTick = tick.aggregateTicks.get(0);
//
//        when:
//        boxAggregatorLocas.runProgram(aggregateTick)
//
//        MusicGridResults results = boxAggregatorLocas.playAllTracks();
//        println results
//
//        then:
//        GridIdeaFlowMetricsEntity metrics = gridBoxMetricsRepository.findByTorchieGridTime(torchieId,
//                aggregateTick.getZoomLevel().name(),
//                Timestamp.valueOf(aggregateTick.from.getClockTime()))
//
//        assert metrics != null
//
//    }

    FlowableFlowActivity generateFileActivity(UUID memberId, LocalDateTime start, LocalDateTime end) {
            FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                    .memberId(memberId)
                    .start(start)
                    .end(end)
        .build();

        return new FlowableFlowActivity(flowActivityEntity);

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
