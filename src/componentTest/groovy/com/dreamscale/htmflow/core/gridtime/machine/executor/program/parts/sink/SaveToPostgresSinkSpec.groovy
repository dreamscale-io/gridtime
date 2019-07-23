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
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyFeaturePool
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
    FeaturePool featurePool
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    TorchieCmd cmd
    GridTimeExecutor torchieExecutor
    Torchie torchie
    LocalDateTime clockStart

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
        featurePool = new MemoryOnlyFeaturePool(torchieId);

        torchie = new Torchie(torchieId, featurePool, new NoOpProgram());
        System.out.println(clockStart);

        torchieExecutor = new GridTimeExecutor(1);

        cmd = new TorchieCmd(torchieExecutor, torchie);
        cmd.haltProgram()
    }

    def teardown() {
        torchieExecutor.shutdown();
    }

    def "should save grid rows and markers to DB"() {
        given:
        cmd.gotoTile(ZoomLevel.TWENTY, clockStart);

        featurePool.getActiveGridTile().startWTF(time3, new CircleDetails(UUID.randomUUID(), "hi"), StartTypeTag.Start)

        featurePool.getActiveGridTile().finishAfterLoad()

        when:
        saveToPostgresSink.save(torchieId, featurePool)

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
