package com.dreamscale.gridtime.core.machine.executor.circuit.now

import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration
import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureReferenceFactory
import com.dreamscale.gridtime.core.machine.memory.feature.details.CircleDetails
import com.dreamscale.gridtime.core.machine.memory.feature.details.WorkContextEvent
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid
import com.dreamscale.gridtime.core.machine.memory.tag.types.StartTypeTag
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType
import org.junit.Ignore
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

class EyeSpec extends Specification {

    Eye eye
    FeatureReferenceFactory featureFactory

    UUID torchieId
    UUID projectId
    UUID taskId
    UUID intentionId

    LocalDateTime clockStart
    GeometryClock clock
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    FeatureCache featureCache
    UUID circleId
    IMusicGrid experienceGrid
    PlaceReference box1
    PlaceReference box2
    PlaceReference box3

    PlaceReference file1A
    PlaceReference file1B
    PlaceReference file2A
    PlaceReference file2B



    def setup() {
        eye = new Eye()
        projectId = UUID.randomUUID()
        taskId = UUID.randomUUID()
        intentionId = UUID.randomUUID()

        torchieId = UUID.randomUUID()
        circleId = UUID.randomUUID();

        TeamBoxConfiguration teamBoxConfiguration = new TeamBoxConfiguration()
        teamBoxConfiguration.configureMatcher(projectId, new BoxMatcherConfig("aBoxOfCode1", "/box1/*"))
        teamBoxConfiguration.configureMatcher(projectId, new BoxMatcherConfig("aBoxOfCode2", "/box2/*"))
        teamBoxConfiguration.configureMatcher(projectId, new BoxMatcherConfig("aBoxOfCode3", "/box3/*"))

        featureCache = new FeatureCache(teamBoxConfiguration);

        featureFactory = new FeatureReferenceFactory()

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(3)
        time3 = clockStart.plusMinutes(12)
        time4 = clockStart.plusMinutes(14)

        clock = new GeometryClock(clockStart)

        box1 = featureFactory.createBoxReference(projectId, "aBoxOfCode1");
        box2 = featureFactory.createBoxReference(projectId, "aBoxOfCode2");
        box3 = featureFactory.createBoxReference(projectId, "aBoxOfCode3");

        file1A = featureFactory.createLocationReference(projectId, "aBoxOfCode1", "/box1/a/file");
        file1B = featureFactory.createLocationReference(projectId, "aBoxOfCode1", "/box1/b/file");

        file2A = featureFactory.createLocationReference(projectId, "aBoxOfCode2", "/box2/a/file");
        file2B = featureFactory.createLocationReference(projectId, "aBoxOfCode2", "/box2/b/file");

    }

    def "should watch for painful feature's in the Grid, and add to TwilightMap"() {
        given:

        eye.focusInScope(new Scope(PlaceType.BOX, ZoomLevel.WEEK))

        eye.watchFor(box1)
        eye.watchFor(box2)

        experienceGrid = generateSplitWTFExperienceGrid(file1A, file1B, file2A, file2B)
        println experienceGrid.playAllTracks()

        when:
        eye.pushIntoFocus(experienceGrid)
        TwilightMap twilightMap = eye.see()

        then:
        assert twilightMap != null
//        assert 1 == twilightMap.count(PlaceType.BOX);
//        assert 2 == twilightMap.count(PlaceType.LOCATION);
//
//        assert twilightMap.contains(box2)
//        assert twilightMap.contains(file1A)
//        assert twilightMap.contains(file1B)
//
//        TwilightMap childPainMap = twilightMap.selectFeaturesAbovePainThreshold( 4 );
//
//        assert childPainMap.size() == 2
//        assert childPainMap.count(PlaceType.BOX) == 1
//        assert childPainMap.count(PlaceType.LOCATION) == 1
    }

    IMusicGrid generateSplitWTFExperienceGrid(PlaceReference ... gotoLocations) {
        GridTile gridTile = new GridTile(torchieId, clock.getActiveGridTime(), featureCache)

        gridTile.startWorkContext(time1, new WorkContextEvent(
                intentionId, "start work",
                taskId, "taskA",
                projectId, "projA"))

        int halfway = gotoLocations.size() / 2;
        boolean wtfStarted = false;
        for (int i = 0 ; i < gotoLocations.size(); i++) {

            PlaceReference location = gotoLocations[i];

            if (i >= halfway ) {
                if (!wtfStarted) {
                    gridTile.startWTF(time3, new CircleDetails(circleId, "yo"), StartTypeTag.Start)
                    gridTile.startFeelsBand(time3, -5)
                }
                gridTile.gotoLocation(time4, location.getLocationPath(), Duration.ofSeconds(5))

            } else {
                gridTile.startFeelsBand(time1, +2)
                gridTile.gotoLocation(time2, location.getLocationPath(), Duration.ofSeconds(5))
            }
        }

        gridTile.finishAfterLoad();
        return gridTile.getMusicGrid();
    }
}
