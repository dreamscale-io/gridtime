package com.dreamscale.htmflow.core.feeds.executor.parts.sink


import com.dreamscale.htmflow.core.domain.member.json.Member
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel
import com.dreamscale.htmflow.core.feeds.story.StoryTile
import com.dreamscale.htmflow.core.feeds.story.StoryTileModel
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel
import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails
import com.dreamscale.htmflow.core.feeds.story.feature.details.CircleDetails
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

class JSONTransformerSpec extends Specification {

    UUID torchieId
    StoryTile tile
    LocalDateTime clockStart
    GeometryClock geometryClock

    def setup() {
        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        geometryClock = new GeometryClock(clockStart)

        torchieId = UUID.randomUUID();
        tile = new StoryTile("/torchie/"+torchieId, geometryClock.coordinates, ZoomLevel.TWENTY_MINS)

    }

    def "should serialize and deserialize JSON"() {

        given:
        LocalDateTime time1 = clockStart.plusMinutes(4);
        LocalDateTime time2 = clockStart.plusMinutes(5);
        LocalDateTime time3 = clockStart.plusMinutes(6);

        tile.beginContext(new ContextBeginningEvent(time1, StructureLevel.INTENTION, UUID.randomUUID()))
        tile.startAuthorsBand(time1, new AuthorDetails(new Member("id", "My Name")))

        tile.gotoLocation(time1, "box", "/location/path/1", Duration.ofSeconds(22))
        tile.gotoLocation(time2, "box", "/location/path/2", Duration.ofSeconds(56))

        tile.modifyCurrentLocation(time2, 30)

        tile.startWTF(time2, new CircleDetails(UUID.randomUUID(), "circle"))

        tile.gotoLocation(time3, "box2", "/location/path/3", Duration.ofSeconds(14))

        tile.executeThing(time3, new ExecutionDetails(time3, Duration.ofSeconds(3)))

        tile.finishAfterLoad();

        when:
        StoryTileModel startingModel = tile.extractStoryTileModel();

        String json = JSONTransformer.toJson(startingModel);

        println json;
        StoryTileModel deserializedModel = JSONTransformer.fromJson(json, StoryTileModel.class);

        then:
        assert deserializedModel != null

    }
}
