package com.dreamscale.gridtime.core.feeds.story;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile
import spock.lang.Specification

import java.time.LocalDateTime;

public class StoryTileSpec extends Specification {

    GridTile tile
    LocalDateTime clockStart
    GeometryClock geometryClock

//    def setup() {
//        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
//        geometryClock = new GeometryClock(clockStart)
//
//        tile = new GridTile("@torchie/id", geometryClock.activeCoords, ZoomLevel.TWENTIES)
//    }
//
//    def "should start and end context"() {
//        given:
//        StartTypeTag startTask = new MusicalSequenceBeginning()
//        startTask.setPosition(clockStart.plusMinutes(4))
//        startTask.setStructureLevel(StructureLevel.TASK)
//        startTask.setContextId(UUID.randomUUID())
//        startTask.setDescription("name");
//
//        MusicalSequenceEnding endTask = new MusicalSequenceEnding()
//        endTask.setPosition(clockStart.plusMinutes(6))
//        endTask.setStructureLevel(StructureLevel.TASK)
//        endTask.setDescription("name");
//        endTask.setReferenceId(startTask.getContextId())
//
//        when:
//        tile.beginContext(startTask)
//        def afterStartContext = tile.getCurrentContext()
//
//        tile.endContext(endTask)
//        def afterEndContext = tile.getCurrentContext()
//
//        then:
//        assert afterStartContext.taskContext.description == "name"
//        assert afterStartContext.projectContext == null;
//        assert afterStartContext.intentionContext == null
//        assert afterStartContext.position == clockStart.plusMinutes(4)
//
//        assert afterEndContext.taskContext == null
//        assert afterEndContext.projectContext == null;
//        assert afterEndContext.intentionContext == null
//
//
//    }
//
//
//    def "should go to location and map movements"() {
//        given:
//        LocalDateTime time1 = clockStart.plusMinutes(4);
//        LocalDateTime time2 = clockStart.plusMinutes(5);
//        LocalDateTime time3 = clockStart.plusMinutes(6);
//
//        when:
//        tile.gotoLocation(time1, "box", "/location/path/1", Duration.ofSeconds(60))
//        tile.gotoLocation(time2, "box", "/location/path/2", Duration.ofSeconds(60))
//        tile.gotoLocation(time3, "box", "/location/path/1", Duration.ofSeconds(60))
//
//
//        def layer = tile.getRhythmLayer(RhythmLayerType.LOCATION_CHANGES)
//        def spatial = tile.getSpatialStructure()
//        def grid = tile.getTileGrid()
//
//        then:
//        assert layer.movements.size() == 4
//        assert layer.movements.get(0) instanceof MoveToBox
//        assert layer.movements.get(1) instanceof MoveToLocation
//        assert layer.movements.get(2) instanceof MoveToLocation
//        assert layer.movements.get(3) instanceof MoveToLocation
//
//        assert layer.movements.get(1).location == layer.movements.get(3).location
//
//        assert spatial.getBoxActivities().size() == 1
//        assert spatial.getBoxActivities().get(0).thoughtBubbles.size() == 1
//        assert spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getAllLocations().size() == 3
//        assert spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getAllTraversals().size() == 2
//
//        assert grid.featureMetricTotals.getBoxesVisited().size() == 1
//        assert grid.featureMetricTotals.getLocationsVisited().size() == 2
//    }
//
//    def "should modify existing location"() {
//        given:
//        LocalDateTime time1 = clockStart.plusMinutes(4);
//        LocalDateTime time2 = clockStart.plusMinutes(5);
//        LocalDateTime time3 = clockStart.plusMinutes(6);
//
//        MusicalSequenceBeginning startTask = new MusicalSequenceBeginning()
//        startTask.setPosition(time1)
//        startTask.setStructureLevel(StructureLevel.TASK)
//        startTask.setContextId(UUID.randomUUID())
//        startTask.setDescription("name");
//
//        when:
//        tile.beginContext(startTask)
//        tile.gotoLocation(time1, "box", "/location/path/1", Duration.ofSeconds(60))
//        tile.modifyCurrentLocation(time2, 55)
//        tile.modifyCurrentLocation(time3, 11)
//
//        def grid = tile.getTileGrid();
//        def locations = grid.featureMetricTotals.getLocationsVisited();
//
//        then:
//        assert locations.size() == 1;
//        assert grid.getMetricTotals(locations.get(0)).getMetrics().getModificationCandle().sampleCount == 2;
//
//    }
//
//    def "should start and clear bands"() {
//        given:
//        LocalDateTime time1 = clockStart.plusMinutes(4);
//        LocalDateTime time2 = clockStart.plusMinutes(5);
//
//        CircleAnnotation circleDetails = new CircleAnnotation(UUID.randomUUID(), "circle");
//
//        when:
//        tile.startWTF(time1, circleDetails)
//        tile.clearWTF(time2)
//        tile.finishAfterLoad();
//
//        def layer = tile.getBandLayer(BandType.FRICTION_WTF)
//
//        then:
//        assert layer.timebands.size() == 1
//        assert layer.timebands.get(0).start == time1
//        assert layer.timebands.get(0).end == time2
//    }
//
//    def "should create carry over bands out of intervals"() {
//        given:
//        LocalDateTime time1 = clockStart.plusMinutes(4);
//        LocalDateTime time2 = clockStart.plusMinutes(5);
//
//        CircleAnnotation circleDetails = new CircleAnnotation(UUID.randomUUID(), "circle");
//
//        when:
//        tile.startWTF(time1, circleDetails)
//        tile.finishAfterLoad()
//
//        TileBuilder nextTile = new TileBuilder("next", geometryClock.activeCoords.panRight(ZoomLevel.TWENTIES), ZoomLevel.TWENTIES);
//
//        nextTile.carryOverTileContext(tile.getCarryOverContext());
//        nextTile.finishAfterLoad();
//
//        def layer = tile.getBandLayer(BandType.FRICTION_WTF)
//        def nextTileLayer = nextTile.getBandLayer(BandType.FRICTION_WTF)
//
//        then:
//        assert layer.timebands.size() == 1
//        assert layer.timebands.get(0).start == time1
//        assert layer.timebands.get(0).end == clockStart.plusMinutes(20)
//
//        assert nextTileLayer.timebands.size() == 1
//        assert nextTileLayer.timebands.get(0).start == clockStart.plusMinutes(20)
//        assert nextTileLayer.timebands.get(0).end == clockStart.plusMinutes(40)
//
//    }
//
//    def "should create rolling bands"() {
//        given:
//        LocalDateTime time1 = clockStart.plusMinutes(4);
//        LocalDateTime time2 = clockStart.plusMinutes(9);
//
//        when:
//        tile.addTypingSampleToAssessLearningFriction(time1, 55)
//        tile.addTypingSampleToAssessLearningFriction(time1, 52)
//        tile.addTypingSampleToAssessLearningFriction(time1, 30)
//
//        tile.addTypingSampleToAssessLearningFriction(time2, 55)
//        tile.addTypingSampleToAssessLearningFriction(time2, 52)
//
//        def layer = tile.getBandLayer(BandType.FRICTION_LEARNING)
//
//        then:
//        assert layer.timebands.size() == 4
//
//        assert layer.timebands.get(0).start == clockStart
//        assert layer.timebands.get(0).end == clockStart.plusMinutes(5)
//        assert ((RollingAggregateBand)layer.timebands.get(0)).aggregateCandleStick.sampleCount == 3;
//
//        assert layer.timebands.get(1).start == clockStart.plusMinutes(5)
//        assert layer.timebands.get(1).end == clockStart.plusMinutes(10)
//        assert ((RollingAggregateBand)layer.timebands.get(1)).aggregateCandleStick.sampleCount == 2;
//
//    }
//
//    def "should generate snapshots when playing frames"() {
//        given:
//        LocalDateTime time1 = clockStart.plusMinutes(4);
//        LocalDateTime time2 = clockStart.plusMinutes(6);
//        LocalDateTime time3 = clockStart.plusMinutes(11);
//        LocalDateTime time4 = clockStart.plusMinutes(16);
//
//        CircleAnnotation circleDetails = new CircleAnnotation(UUID.randomUUID(), "circle");
//
//        when:
//        tile.gotoLocation(time1, "box", "/a/path", Duration.ofSeconds(15))
//        tile.gotoLocation(time1, "box", "/a/path2", Duration.ofSeconds(15))
//        tile.gotoLocation(time1, "box2", "/a/path3", Duration.ofSeconds(15))
//
//
//        tile.startWTF(time1, circleDetails)
//        tile.clearWTF(time3)
//
//        tile.addTypingSampleToAssessLearningFriction(time2, 340)
//        tile.addTypingSampleToAssessLearningFriction(time3, 55)
//
//        tile.startFeelsBand(time1, new FeelsAnnotation(3))
//        tile.startFeelsBand(time1, new FeelsAnnotation(1))
//        tile.clearFeelsBand(time2)
//
//        tile.startFeelsBand(time3, new FeelsAnnotation(-4))
//
//        tile.play();
//        def storyGrid = tile.getTileGrid();
//        List<Column> columns = storyGrid.columns;
//
//        then:
//        assert columns.size() == 4
//        assert columns.get(0).getBoxesVisited().size() == 2
//        assert columns.get(0).getLocationsVisited().size() == 5
//
//
//    }
}
