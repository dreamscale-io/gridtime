package com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.sink

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

@ComponentTest
class SaveToPostgresSinkSpec extends Specification {

    @Autowired
    SaveToPostgresSink saveToPostgresSink

    GridTile gridTile
    GeometryClock clock
    UUID torchieId
    FeatureCache featureCache

    def setup() {

        clock = new GeometryClock(LocalDateTime.now())
        torchieId = UUID.randomUUID()
        featureCache = new FeatureCache()
        gridTile = new GridTile(torchieId, clock.getActiveGridTime(), featureCache)
    }

    def "should save tile grid to DB"() {

    }
}
