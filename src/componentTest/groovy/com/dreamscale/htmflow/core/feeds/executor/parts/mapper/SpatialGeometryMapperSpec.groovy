package com.dreamscale.htmflow.core.feeds.executor.parts.mapper

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory
import com.dreamscale.htmflow.core.feeds.story.grid.TileGrid
import com.dreamscale.htmflow.core.feeds.story.music.BeatSize
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock
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
        MusicClock musicClock = new MusicClock(clockStart, clockStart.plusMinutes(20), 20);

        FeatureFactory featureFactory = new FeatureFactory("/tile/id")
        TileGrid storyGrid = new TileGrid(musicClock, BeatSize.QUARTER)
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

    def "should make multiple bubbles rings based on hops"() {
        given:
        LocalDateTime time1 = clockStart.plusMinutes(4);
        when:

        spatialGeometryMapper.gotoLocation(time1, "box", "/location/A", Duration.ofSeconds(1000))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/B", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/A", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/C", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/A", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/D", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/A", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/E", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/F", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/E", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/G", Duration.ofSeconds(20))


        def spatial = spatialGeometryMapper.getSpatialStructuredActivity()

        then:

        //starts in entrance, then must exit to get to box2

        //A is center, then everything is 1 hop away, except F & G are in ring 2

        spatial.getBoxActivities().get(0).thoughtBubbles.size() == 1;

        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getCenter().getLocation().locationPath == "/location/A"
        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getRings().size() == 2
        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getRings().get(0).getRingLocations().size() == 4
        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getRings().get(1).getRingLocations().size() == 2

    }

    def "should make multiple bubbles when network is broken"() {
        given:
        LocalDateTime time1 = clockStart.plusMinutes(4);

        when:

        spatialGeometryMapper.gotoLocation(time1, "box", "/location/A", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/B", Duration.ofSeconds(2000))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/A", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/B", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box2", "/location/ZZZ", Duration.ofSeconds(20))

        spatialGeometryMapper.gotoLocation(time1, "box", "/location/A", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/C", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box2", "/location/ZZZ", Duration.ofSeconds(20))

        spatialGeometryMapper.gotoLocation(time1, "box", "/location/E", Duration.ofSeconds(2000))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/F", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/E", Duration.ofSeconds(20))
        spatialGeometryMapper.gotoLocation(time1, "box", "/location/G", Duration.ofSeconds(20))


        def spatial = spatialGeometryMapper.getSpatialStructuredActivity()

        then:

        //so location A & B & C, are part of same network, but E/F are disconnected

        //A is center, then everything is 1 hop away, except F & G are in ring 2

        spatial.getBoxActivities().get(0).thoughtBubbles.size() == 2;

        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getCenter().getLocation().locationPath == "/location/B"
        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getRings().size() == 2
        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getRings().get(0).getRingLocations().size() == 1
        spatial.getBoxActivities().get(0).thoughtBubbles.get(0).getRings().get(1).getRingLocations().size() == 1

        spatial.getBoxActivities().get(0).thoughtBubbles.get(1).getCenter().getLocation().locationPath == "/location/E"
        spatial.getBoxActivities().get(0).thoughtBubbles.get(1).getRings().size() == 1
        spatial.getBoxActivities().get(0).thoughtBubbles.get(1).getRings().get(0).getRingLocations().size() == 2

    }

}
