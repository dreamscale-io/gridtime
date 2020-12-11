package com.dreamscale.gridtime.core.machine.executor.program.parts.sink

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.tile.metrics.GridBoxMetricsRepository

import com.dreamscale.gridtime.core.domain.tile.GridMarkerEntity
import com.dreamscale.gridtime.core.domain.tile.GridMarkerRepository
import com.dreamscale.gridtime.core.domain.tile.GridRowEntity
import com.dreamscale.gridtime.core.domain.tile.GridRowRepository
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository
import com.dreamscale.gridtime.core.machine.GridTimeEngine
import com.dreamscale.gridtime.core.machine.Torchie
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import com.dreamscale.gridtime.core.machine.executor.program.NoOpProgram
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.gridtime.core.machine.memory.TorchieState
import com.dreamscale.gridtime.core.machine.memory.MemoryOnlyTorchieState
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
import com.dreamscale.gridtime.core.machine.memory.feature.details.CircuitDetails
import com.dreamscale.gridtime.core.machine.memory.tag.types.StartTypeTag
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class SaveToPostgresSinkSpec extends Specification {

    @Autowired
    SaveToPostgresSink saveToPostgresSink

    @Autowired
    CalendarService calendarService

    @Autowired
    GridBoxMetricsRepository gridBoxMetricsRepository

    @Autowired
    GridMarkerRepository gridMarkerRepository

    @Autowired
    GridRowRepository gridRowRepository

    @Autowired
    GridIdeaFlowMetricsRepository gridTileSummaryRepository

    @Autowired
    GridTimeEngine gridTimeEngine;

    GeometryClock clock

    UUID torchieId
    FeatureCache featureCache
    TorchieState torchieState
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    Torchie torchie
    LocalDateTime clockStart
    UUID orgId

    def setup() {

        clock = new GeometryClock(LocalDateTime.now())
        torchieId = UUID.randomUUID()
        featureCache = new FeatureCache()

        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(2)
        time3 = clockStart.plusMinutes(3)
        time4 = clockStart.plusMinutes(6)

        orgId = UUID.randomUUID();
        torchieId = UUID.randomUUID();
        torchieState = new MemoryOnlyTorchieState(orgId, torchieId);

        torchie = new Torchie(torchieId, torchieState, new NoOpProgram());
        System.out.println(clockStart);

    }

    def "should save grid rows and markers to DB"() {
        given:

        GeometryClock.GridTime startPosition = GeometryClock.createGridTime(ZoomLevel.TWENTY, clockStart)

        calendarService.saveCalendar(1, 1, startPosition)

        torchieState.gotoPosition(startPosition)

        torchieState.getActiveTile().startWTF(time3, new CircuitDetails(UUID.randomUUID(), "hi"), StartTypeTag.Start)
        torchieState.getActiveTile().finishAfterLoad()

        when:
        saveToPostgresSink.save(torchieId, torchieState)

        List<GridRowEntity> rowEntities = gridRowRepository.findByTorchieIdAndRowName(torchieId, "@flow/wtf");

        List<GridMarkerEntity> markerEntities = gridMarkerRepository.findByTorchieId(torchieId);

        println markerEntities

        then:
        assert rowEntities != null
        assert rowEntities.size() == 1

        assert markerEntities != null
        assert  markerEntities.size() == 2;

    }
}
