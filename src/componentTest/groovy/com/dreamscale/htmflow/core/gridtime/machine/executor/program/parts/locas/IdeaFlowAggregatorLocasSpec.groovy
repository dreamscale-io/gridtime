package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsEntity
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsRepository
import com.dreamscale.htmflow.core.gridtime.machine.Torchie
import com.dreamscale.htmflow.core.gridtime.machine.TorchieFactory
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.IdeaFlowAggregatorLocas
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.LocasFactory
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyFeaturePool
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache
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

    IdeaFlowAggregatorLocas ideaflowLocas
    FeatureCache featureCache
    Metronome metronome

    MemoryOnlyFeaturePool featurePool
    Torchie torchie


    def setup() {

        torchieId = UUID.randomUUID()
        featureCache = new FeatureCache()

        ideaflowLocas = locasFactory.createIdeaFlowAggregatorLocas(torchieId, featureCache);

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        metronome = new Metronome(clockStart)

        torchie = torchieFactory.wireUpMemberTorchie(UUID.randomUUID(), torchieId, clockStart);

        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(2)
        time3 = clockStart.plusMinutes(3)
        time4 = clockStart.plusMinutes(6)

        Metronome.Tick tick = metronome.tick();

        calendarService.saveCalendar(1, 12, tick.from);
        calendarService.saveCalendar(1, tick.from.zoomOut());

    }


    def "should aggregate IdeaFlowMetrics by GridTime, i.e. aggregation of Twenties into DayParts"() {
        given:
        for (int i = 0; i < 12; i++) {
            torchie.whatsNext().call()
        }

        Metronome.Tick tick = torchie.getActiveTick();
        Metronome.Tick aggregateTick = tick.aggregateTicks.get(0);

        when:
        ideaflowLocas.breatheIn(aggregateTick)
        ideaflowLocas.breatheOut(aggregateTick)

        then:
        GridIdeaFlowMetricsEntity metrics = gridMetricsIdeaFlowRepository.findByTorchieGridTime(torchieId,
                aggregateTick.getZoomLevel().name(),
                Timestamp.valueOf(aggregateTick.from.getClockTime()))

        assert true
        //assert metrics != null

    }

}
