package com.dreamscale.gridtime.core.capability.system


import spock.lang.Specification

class GridClockSpec extends Specification {


    GridClock gridClock = new GridClock()

    def "should create a nanoTime precise clock" () {

        when:
        long nanoOne = gridClock.nanoTime()

        System.sleep(100)

        long nanoTwo = gridClock.nanoTime()

        then:
        assert nanoOne < nanoTwo

    }
}
