package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink


import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile
import spock.lang.Specification

import java.time.LocalDateTime

class JSONTransformerSpec extends Specification {

    UUID torchieId
    GridTile tile
    LocalDateTime clockStart
    GeometryClock geometryClock

    def setup() {
        clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        geometryClock = new GeometryClock(clockStart)

        torchieId = UUID.randomUUID();
        tile = new GridTile(torchieId, geometryClock.activeGridTime, new FeatureCache())

    }

    def "should serialize and deserialize JSON"() {

//        given:
//        LocalDateTime time1 = clockStart.plusMinutes(4);
//        LocalDateTime time2 = clockStart.plusMinutes(5);
//        LocalDateTime time3 = clockStart.plusMinutes(6);
//
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
//
//        when:
//        StoryTileModel startingModel = tile.extractStoryTileModel();
//
//        String json = JSONTransformer.toJson(startingModel);
//
//        println json;
//        StoryTileModel deserializedModel = JSONTransformer.fromJson(json, StoryTileModel.class);
//
//        then:
//        assert deserializedModel != null

    }
}
