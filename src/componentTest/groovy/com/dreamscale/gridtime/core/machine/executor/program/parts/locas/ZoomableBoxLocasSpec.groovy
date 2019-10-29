package com.dreamscale.gridtime.core.machine.executor.program.parts.locas

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.api.circle.CircleMessageType
import com.dreamscale.gridtime.core.domain.circle.CircleFeedMessageEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.gridtime.core.domain.journal.IntentionEntity
import com.dreamscale.gridtime.core.domain.journal.JournalEntryEntity
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsEntity
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsRepository
import com.dreamscale.gridtime.core.machine.Torchie
import com.dreamscale.gridtime.core.machine.TorchieFactory
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.Metronome
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircleMessageEvent
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableJournalEntry
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableBoxLocas
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration
import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
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
    UUID projectId

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
        teamId = UUID.randomUUID()
        projectId = UUID.randomUUID()

        boxAggregatorLocas = locasFactory.createBoxAggregatorLocas(teamId,  torchieId);

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        metronome = new Metronome(clockStart)

        torchie = torchieFactory.wireUpMemberTorchie(teamId, torchieId, clockStart);

        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(4)
        time3 = clockStart.plusMinutes(6)
        time4 = clockStart.plusMinutes(7)

        Metronome.TickScope tick = metronome.tick();

        calendarService.saveCalendar(1, 12, tick.from);
        calendarService.saveCalendar(1, tick.from.zoomOut());

        TeamBoxConfiguration.Builder builder = new TeamBoxConfiguration.Builder()
        builder.boxMatcher(projectId, new BoxMatcherConfig("aBoxOfCode1", "/box1/*"))
        builder.boxMatcher(projectId, new BoxMatcherConfig("aBoxOfCode2", "/box2/*"))


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
        torchie.whatsNext().call();
        println torchie.getLastOutput();

        then:
        List<GridBoxMetricsEntity> boxMetrics = gridBoxMetricsRepository.findByTorchieGridTime(torchieId,
                torchie.getActiveTick().getZoomLevel().name(),
                Timestamp.valueOf(torchie.getActiveTick().getFrom().getClockTime()))

        assert boxMetrics != null;
        assert boxMetrics.size() > 0;

    }

    def "should produce BoxMetrics for each Box"() {
        given:

        //create box metrics, for the specified calendar times, create grid features for each

        TeamBoxConfiguration.Builder boxConfigBuilder = new TeamBoxConfiguration.Builder()
        boxConfigBuilder.boxMatcher(projectId, new BoxMatcherConfig("aBoxOfCode1", "/box1/*"))
        boxConfigBuilder.boxMatcher(projectId, new BoxMatcherConfig("aBoxOfCode2", "/box2/*"))

        torchie.changeBoxConfiguration(boxConfigBuilder.build())

        InputFeed journalFeed = torchie.getInputFeed(FeedStrategyFactory.FeedType.JOURNAL_FEED)
        journalFeed.addSomeData(generateIntentionStart(time1))

        InputFeed fileActivityFeed = torchie.getInputFeed(FeedStrategyFactory.FeedType.FILE_ACTIVITY_FEED)
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time1, "/box1/file.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time2, "/box1/file2.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time3, "/box2/file3.txt"))

        when:
            torchie.whatsNext().call();
            println torchie.getLastOutput();
        then:
        List<GridBoxMetricsEntity> boxMetrics = gridBoxMetricsRepository.findByTorchieGridTime(torchieId,
                torchie.getActiveTick().getZoomLevel().name(),
                Timestamp.valueOf(torchie.getActiveTick().getFrom().getClockTime()))

        assert boxMetrics != null;
        assert boxMetrics.size() == 2;
    }


  def "should aggregate BoxMetrics across Time, Grouped by Box"() {
        given:

        //create box metrics, for the specified calendar times, create grid features for each

        TeamBoxConfiguration.Builder boxConfigBuilder = new TeamBoxConfiguration.Builder()
        boxConfigBuilder.boxMatcher(projectId, new BoxMatcherConfig("aBoxOfCode1", "/box1/*"))
        boxConfigBuilder.boxMatcher(projectId, new BoxMatcherConfig("aBoxOfCode2", "/box2/*"))

        torchie.changeBoxConfiguration(boxConfigBuilder.build())

        InputFeed journalFeed = torchie.getInputFeed(FeedStrategyFactory.FeedType.JOURNAL_FEED)
        journalFeed.addSomeData(generateIntentionStart(time1))

        InputFeed fileActivityFeed = torchie.getInputFeed(FeedStrategyFactory.FeedType.FILE_ACTIVITY_FEED)
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time1, "/box1/file.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time2, "/box1/file2.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time3, "/box2/file3.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time1.plusMinutes(32), "/box1/file.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time1.plusMinutes(38), "/box2/fileZ.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchieId, time1.plusMinutes(56), "/box2/file4.txt"))

        when:
        for (int i = 0; i < 13; i++) {
            torchie.whatsNext().call();
            println torchie.getLastOutput();
        }

        GeometryClock.GridTime zoomOutGridTime = torchie.getActiveTick().getFrom().zoomOut();

        then:
        List<GridBoxMetricsEntity> boxMetrics = gridBoxMetricsRepository.findByTorchieGridTime(torchieId,
                zoomOutGridTime.getZoomLevel().toString(),
                Timestamp.valueOf(zoomOutGridTime.getClockTime()))

        assert boxMetrics != null;
        assert boxMetrics.size() == 2;
    }


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

    FlowableFlowActivity generateFileActivity(UUID memberId, LocalDateTime start, String filePath) {
        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(start.plusMinutes(1))
                .build();

        flowActivityEntity.setMetadataField(FlowActivityMetadataField.filePath, filePath)

        return new FlowableFlowActivity(flowActivityEntity);

    }

    FlowableFlowActivity generateFileActivity(UUID memberId, LocalDateTime start, LocalDateTime end) {
            FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                    .memberId(memberId)
                    .start(start)
                    .end(end)
        .build();

        return new FlowableFlowActivity(flowActivityEntity);

    }

    def generateIntentionStart(LocalDateTime localDateTime) {
        JournalEntryEntity journalEntryEntity = new JournalEntryEntity()
        journalEntryEntity.setId(UUID.randomUUID())
        journalEntryEntity.setProjectId(projectId)
        journalEntryEntity.setPosition(localDateTime)
        journalEntryEntity.setTaskId(UUID.randomUUID())
        journalEntryEntity.setTaskName("taskA")
        journalEntryEntity.setMemberId(torchieId)

        return new FlowableJournalEntry(journalEntryEntity)

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
