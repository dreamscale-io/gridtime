package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.domain.member.json.Member
import com.dreamscale.htmflow.core.domain.tile.StoryGridMetricsEntity
import com.dreamscale.htmflow.core.domain.tile.StoryGridMetricsRepository
import com.dreamscale.htmflow.core.domain.tile.StoryTileEntity
import com.dreamscale.htmflow.core.domain.tile.StoryTileRepository
import com.dreamscale.htmflow.core.domain.tile.StoryGridSummaryEntity
import com.dreamscale.htmflow.core.domain.tile.StoryGridSummaryRepository
import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

@ComponentTest
class SaveToPostgresSinkSpec extends Specification {

    @Autowired
    SaveToPostgresSink saveToPostgresSink;

    @Autowired
    StoryTileRepository storyTileRepository;

    @Autowired
    StoryGridMetricsRepository storyGridMetricsRepository;

    @Autowired
    StoryGridSummaryRepository storyTileSummaryRepository;

    UUID torchieId
    GridTile tile
    LocalDateTime clockStart
    GeometryClock geometryClock

    def setup() {
        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        geometryClock = new GeometryClock(clockStart)

        torchieId = UUID.randomUUID();
        tile = new GridTile("/torchie/"+torchieId, geometryClock.activeCoords, ZoomLevel.TWENTIES)

    }

    def "should save tiles to the DB"() {

        given:
        LocalDateTime time1 = clockStart.plusMinutes(4);
        LocalDateTime time2 = clockStart.plusMinutes(5);
        LocalDateTime time3 = clockStart.plusMinutes(6);

//        tile.beginContext(new MusicalSequenceBeginning(time1, StructureLevel.INTENTION, UUID.randomUUID()))
//        tile.startAuthorsBand(time1, new PairingAuthorsAnnotation(new Member("id", "My Name")))
//
//        tile.gotoLocation(time1, "box", "/location/path/1", Duration.ofSeconds(22))
//        tile.gotoLocation(time2, "box", "/location/path/2", Duration.ofSeconds(56))
//
//        tile.modifyCurrentLocation(time2, 30)
//
//        tile.startWTF(time2, new CircleAnnotation(UUID.randomUUID(), "circle"))
//
//        tile.gotoLocation(time3, "box2", "/location/path/3", Duration.ofSeconds(14))
//
//        tile.executeThing(time3, new ExecutionEvent(time3, Duration.ofSeconds(3)))
//
//        tile.finishAfterLoad();

        when:
        saveToPostgresSink.save(torchieId, tile)

        List<StoryTileEntity> tiles = storyTileRepository.findByTorchieIdOrderByClockPosition(torchieId);

        List<StoryGridSummaryEntity> summaryTiles = storyTileSummaryRepository.findByTorchieId(torchieId);

        List<StoryGridMetricsEntity> metrics = storyGridMetricsRepository.findByTorchieId(torchieId);

        then:
        assert tiles.size() == 1
        assert summaryTiles.size() == 1
        assert metrics.size() == 19

    }
}
