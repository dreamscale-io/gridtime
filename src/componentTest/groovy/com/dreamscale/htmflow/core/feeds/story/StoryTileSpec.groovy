package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEndingEvent
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveToBox
import com.dreamscale.htmflow.core.feeds.story.feature.movement.MoveToLocation
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Box
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;
import spock.lang.Specification

import java.time.Duration;
import java.time.LocalDateTime;

public class StoryTileSpec extends Specification {

    StoryTile tile
    LocalDateTime clockStart

    def setup() {
        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        def geometryClock = new GeometryClock(clockStart)

        tile = new StoryTile("@torchie/id", geometryClock.coordinates, ZoomLevel.MIN_20)
    }

    def "should start and end context"() {
        given:
        ContextBeginningEvent startTask = new ContextBeginningEvent()
        startTask.setPosition(clockStart.plusMinutes(4))
        startTask.setStructureLevel(StructureLevel.TASK)
        startTask.setReferenceId(UUID.randomUUID())
        startTask.setName("name");

        ContextEndingEvent endTask = new ContextEndingEvent()
        endTask.setPosition(clockStart.plusMinutes(6))
        endTask.setStructureLevel(StructureLevel.TASK)
        endTask.setName("name");
        endTask.setReferenceId(startTask.getReferenceId())

        when:
        tile.beginContext(startTask)
        def afterStartContext = tile.getCurrentContext()

        tile.endContext(endTask)
        def afterEndContext = tile.getCurrentContext()

        then:
        assert afterStartContext.taskContext.name == "name"
        assert afterStartContext.projectContext == null;
        assert afterStartContext.intentionContext == null
        assert afterStartContext.position == clockStart.plusMinutes(4)

        assert afterEndContext.taskContext == null
        assert afterEndContext.projectContext == null;
        assert afterEndContext.intentionContext == null


    }


    def "should go to location and map movements"() {
        given:
        LocalDateTime time1 = clockStart.plusMinutes(4);
        LocalDateTime time2 = clockStart.plusMinutes(5);
        LocalDateTime time3 = clockStart.plusMinutes(6);

        when:
        tile.gotoLocation(time1, "box", "/location/path/1", Duration.ofSeconds(60))
        tile.gotoLocation(time2, "box", "/location/path/2", Duration.ofSeconds(60))
        tile.gotoLocation(time3, "box", "/location/path/1", Duration.ofSeconds(60))

        def layer = tile.getRhythmLayer(RhythmLayerType.LOCATION_CHANGES)
        def spatial = tile.getSpatialStructure()
        def grid = tile.getStoryGrid();

        then:
        assert layer.movements.size() == 4
        assert layer.movements.get(0) instanceof MoveToBox
        assert layer.movements.get(1) instanceof MoveToLocation
        assert layer.movements.get(2) instanceof MoveToLocation
        assert layer.movements.get(3) instanceof MoveToLocation

        assert layer.movements.get(1).referenceObject == layer.movements.get(3).referenceObject

        assert spatial.getBoxActivities().size() == 1
        assert spatial.getBoxActivities().get(0).thoughtBubbles.size() == 1
        assert spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getAllLocations().size() == 2
        assert spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getAllTraversals().size() == 1

        assert grid.featureRows.size() == 4
        assert grid.aggregateRows.size() == 1
    }

    def "should modify existing location"() {
        given:
        LocalDateTime time1 = clockStart.plusMinutes(4);
        LocalDateTime time2 = clockStart.plusMinutes(5);
        LocalDateTime time3 = clockStart.plusMinutes(6);

        ContextBeginningEvent startTask = new ContextBeginningEvent()
        startTask.setPosition(time1)
        startTask.setStructureLevel(StructureLevel.TASK)
        startTask.setReferenceId(UUID.randomUUID())
        startTask.setName("name");

        when:
        tile.beginContext(startTask)
        tile.gotoLocation(time1, "box", "/location/path/1", Duration.ofSeconds(60))
        tile.modifyCurrentLocation(time2, 55)
        tile.modifyCurrentLocation(time3, 11)

        def grid = tile.getStoryGrid();

        then:
        assert grid.featureRows.size() == 3;
        assert grid.featureRows.get(0).allTimeBucket.getModificationCandle().sampleCount == 2;
        assert grid.featureRows.get(1).allTimeBucket.getModificationCandle().sampleCount == 2;
        assert grid.featureRows.get(2).allTimeBucket.getModificationCandle().sampleCount == 2;

    }
}
