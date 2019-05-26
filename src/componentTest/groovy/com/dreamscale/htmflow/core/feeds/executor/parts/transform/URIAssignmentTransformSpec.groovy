package com.dreamscale.htmflow.core.feeds.executor.parts.transform

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel
import com.dreamscale.htmflow.core.feeds.story.mapper.URIMapper
import com.dreamscale.htmflow.core.feeds.story.TileBuilder
import com.dreamscale.htmflow.core.feeds.story.feature.context.MusicalSequenceBeginning
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

@ComponentTest
class URIAssignmentTransformSpec extends Specification {

    @Autowired
    URIAssignmentTransform uriAssignmentTransform

    @Autowired
    URIMapper uriMapper

    TileBuilder storyTile
    LocalDateTime clockStart

    def setup() {
        clockStart = LocalDateTime.now();
        storyTile = new TileBuilder("@torchie/id", new GeometryClock(clockStart).getCoordinates(), ZoomLevel.TWENTIES)
    }

    def "transform should reuse existing location URI if already in DB"() {
        given:
        UUID projectId = UUID.randomUUID();
        LocationInBox locationPreSaved = new LocationInBox("box", "/a/path");
        uriMapper.populateLocationUri(projectId, locationPreSaved);


        MusicalSequenceBeginning projectStart = new MusicalSequenceBeginning(clockStart, StructureLevel.PROJECT, projectId);

        storyTile.beginContext(projectStart)
        storyTile.gotoLocation(clockStart, "box", "/a/path", Duration.ofSeconds(20))

        when:
        uriAssignmentTransform.transform(storyTile);

        then:
        LocationInBox locationFromTile = (LocationInBox) storyTile.getLastMovement(RhythmLayerType.LOCATION_CHANGES).location
        assert locationFromTile != null
        assert locationFromTile.getUri() == locationPreSaved.getUri()
    }
}
