package com.dreamscale.gridtime.core.machine.executor.program.parts.sink


import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValue
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellValueMap
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile
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

        tile = new GridTile(torchieId, geometryClock.activeGridTime, new FeatureCache(), new BoxResolver())

    }

    def "should serialize and deserialize"() {
        given:
        CellValueMap map = new CellValueMap();
        List<UUID> featureList = DefaultCollections.toList(UUID.randomUUID())

        map.put("20.1", new CellValue("wtf", featureList, null))
        map.put("20.2", new CellValue("wtf*", featureList, null))

        when:
        String json = JSONTransformer.toJson(map);

        println json

        CellValueMap rehydrated = JSONTransformer.fromJson(json, CellValueMap.class);

        then:
        assert rehydrated.size() == 2

    }

}
