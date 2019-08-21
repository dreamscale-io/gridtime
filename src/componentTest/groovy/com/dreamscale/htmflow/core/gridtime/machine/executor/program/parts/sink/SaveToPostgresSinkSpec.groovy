package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.tile.GridBoxMetricsRepository
import com.dreamscale.htmflow.core.domain.tile.GridBridgeMetricsRepository
import com.dreamscale.htmflow.core.domain.tile.GridMarkerEntity
import com.dreamscale.htmflow.core.domain.tile.GridMarkerRepository
import com.dreamscale.htmflow.core.domain.tile.GridRowEntity
import com.dreamscale.htmflow.core.domain.tile.GridRowRepository
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsRepository
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.TorchieCmd
import com.dreamscale.htmflow.core.gridtime.machine.Torchie
import com.dreamscale.htmflow.core.gridtime.machine.GridTimeExecutor
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.NoOpProgram
import com.dreamscale.htmflow.core.gridtime.machine.executor.workpile.TorchieWorkerPool
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyTorchieState
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.CircleDetails
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.types.StartTypeTag
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class SaveToPostgresSinkSpec extends Specification {

    @Autowired
    SaveToPostgresSink saveToPostgresSink

    @Autowired
    GridBoxMetricsRepository gridBoxMetricsRepository

    @Autowired
    GridBridgeMetricsRepository gridBridgeMetricsRepository

    @Autowired
    GridMarkerRepository gridMarkerRepository

    @Autowired
    GridRowRepository gridRowRepository

    @Autowired
    GridIdeaFlowMetricsRepository gridTileSummaryRepository

    GeometryClock clock

    UUID torchieId
    FeatureCache featureCache
    TorchieState torchieState
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    TorchieCmd cmd
    Torchie torchie
    LocalDateTime clockStart
    GridTimeExecutor gridTimeExecutor

    def setup() {

        clock = new GeometryClock(LocalDateTime.now())
        torchieId = UUID.randomUUID()
        featureCache = new FeatureCache()

        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(2)
        time3 = clockStart.plusMinutes(3)
        time4 = clockStart.plusMinutes(6)

        torchieId = UUID.randomUUID();
        torchieState = new MemoryOnlyTorchieState(torchieId);

        torchie = new Torchie(torchieId, torchieState, new NoOpProgram());
        System.out.println(clockStart);

        gridTimeExecutor = new GridTimeExecutor(new TorchieWorkerPool());

        cmd = new TorchieCmd(this.gridTimeExecutor, torchie);
        cmd.haltProgram()
    }

    def teardown() {
        this.gridTimeExecutor.shutdown();
    }

    def "should save grid rows and markers to DB"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        torchieState.getActiveTile().startWTF(time3, new CircleDetails(UUID.randomUUID(), "hi"), StartTypeTag.Start)

        torchieState.getActiveTile().finishAfterLoad()

        when:
        saveToPostgresSink.save(torchieId, torchieState)

        List<GridRowEntity> rowEntities = gridRowRepository.findByTorchieIdAndZoomLevelAndRowNameOrderByTileSeq(torchieId,
                ZoomLevel.TWENTY, "@flow/wtf");

        List<GridMarkerEntity> markerEntities = gridMarkerRepository.findByTorchieIdOrderByTileSeq(torchieId);

        println markerEntities

        then:
        assert rowEntities != null
        assert rowEntities.size() == 1

        assert markerEntities != null
        assert  markerEntities.size() == 2;

    }
}
