package com.dreamscale.gridtime.core.machine.executor

import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import spock.lang.Specification

import java.time.LocalDateTime

class GeometryClockSpec extends Specification {

    GeometryClock geometryClock


    def "should generate coords matching start of year"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.GridTime coords = geometryClock.getActiveGridTime();

        then:
        assert coords != null
        assert coords.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+2:20"

    }

    def "should generate coords starting from 1 and count to 12 20s"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 23, 59)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.GridTime coords = geometryClock.getActiveGridTime();

        then:
        assert coords != null
        assert coords.toDisplayString() == "/gridtime/2019-B1-W1-D1_8pm+3:40"
    }

    def "should generate non-zero week coords"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2020, 6, 16, 17, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.GridTime coords = geometryClock.getActiveGridTime();

        then:
        assert coords != null
        assert coords.toDisplayString() == "/gridtime/2020-B4-W6-D2_4pm+1:20"
    }


    def "should count by weeks with monday starts"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 27, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.GridTime coords = geometryClock.getActiveGridTime();

        then:
        assert coords != null
        assert coords.toDisplayString() == "/gridtime/2019-B1-W3-D7_4am+0:20"

    }

    def "should consider days before first monday part of last year "() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 1, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.GridTime coords = geometryClock.getActiveGridTime();

        then:
        assert coords != null
        assert coords.toDisplayString() == "/gridtime/2018-B9-W5-D2_4am+0:20"

    }

    def "pan right block block should pan across year boundary in grid coordinates "() {
        given:
        GeometryClock blockClock = new GeometryClock(ZoomLevel.BLOCK, [2019, 9] as Integer[])
        GeometryClock yearClock = new GeometryClock(ZoomLevel.YEAR, [2020] as Integer[])


        when:
        GeometryClock.GridTime blockCoords = blockClock.getActiveGridTime();
        GeometryClock.GridTime nextBlockCoords = blockCoords.panRight();

        GeometryClock.GridTime yearCoords = yearClock.getActiveGridTime();


        LocalDateTime blockTime = blockCoords.getClockTime();

        println blockTime

        LocalDateTime nextBlockTime = nextBlockCoords.getClockTime();

        println nextBlockTime

        LocalDateTime yearTime = yearCoords.getClockTime();

        println yearTime

        then:
        assert blockCoords.toDisplayString() == "/gridtime/2019-B9"
        assert nextBlockCoords.toDisplayString() == "/gridtime/2020-B1"

        assert yearCoords.toDisplayString() == "/gridtime/2020"

        assert nextBlockTime == yearTime

    }

    def "should rollback from block 1 to 9 at end of the year "() {
        given:

        GeometryClock.GridTime time = GeometryClock.createGridTimeFromCoordinates(ZoomLevel.BLOCK, [2021, 1] as Integer[])

        println time.getClockTime()

        GeometryClock firstBlockClock = new GeometryClock(ZoomLevel.BLOCK, [2021, 1] as Integer[])
        GeometryClock twentyClock = new GeometryClock(firstBlockClock.getActiveGridTime().getClockTime())


        when:
        GeometryClock.GridTime nextCoords = twentyClock.getActiveGridTime();

        for (int i = 0; i < 100; i++) {
            nextCoords = nextCoords.panLeft();
            println nextCoords.getFormattedGridTime()
        }
        then:
        assert nextCoords.getBlock() == 9

    }


}
