package com.dreamscale.htmflow.core.feeds.executor.parts.mapper

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel
import com.dreamscale.htmflow.core.feeds.story.StoryTile
import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGrid
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

class SpatialGeometryMapperSpec extends Specification {

    SpatialGeometryMapper spatialGeometryMapper
    LocalDateTime clockStart
    GeometryClock geometryClock

    def setup() {
        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        geometryClock = new GeometryClock(clockStart)

        FeatureFactory featureFactory = new FeatureFactory("/tile/id")
        StoryGrid storyGrid = new StoryGrid()
        spatialGeometryMapper = new SpatialGeometryMapper(featureFactory, storyGrid)
    }

    def "should go to locations in space and make traversals"() {
        given:
        LocalDateTime time1 = clockStart.plusMinutes(4);
        LocalDateTime time2 = clockStart.plusMinutes(5);
        LocalDateTime time3 = clockStart.plusMinutes(6);

        when:

        spatialGeometryMapper.gotoLocation(time1, "box", "/location/A", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time2, "box", "/location/B", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time3, "box2", "/location/C", Duration.ofSeconds(20))

        def spatial = spatialGeometryMapper.getSpatialStructuredActivity()

        then:

        //starts in entrance, then must exit to get to box2

        //Enter => A => B => Exit, Enter => C

        spatial.getBoxActivities().size() == 2;
        spatial.getBoxActivities().get(0).box.boxName == "box"
        spatial.getBoxActivities().get(0).thoughtBubbles.size() == 1;

        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getAllLocations().size() == 4;
        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getAllTraversals().size() == 3;

    }

}
