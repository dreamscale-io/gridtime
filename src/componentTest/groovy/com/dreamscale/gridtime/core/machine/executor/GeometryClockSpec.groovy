package com.dreamscale.gridtime.core.machine.executor

import com.dreamscale.gridtime.core.machine.clock.GeometryClock
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
        assert coords.toDisplayString() == "2019-B1-W1-D1_12am+2:20"

    }

    def "should generate coords starting from 1 and count to 12 20s"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 23, 59)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.GridTime coords = geometryClock.getActiveGridTime();

        then:
        assert coords != null
        assert coords.toDisplayString() == "2019-B1-W1-D1_8pm+3:40"
    }


    def "should count by weeks with monday starts"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 27, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.GridTime coords = geometryClock.getActiveGridTime();

        then:
        assert coords != null
        assert coords.toDisplayString() == "2019-B1-W3-D7_4am+0:20"

    }

    def "should consider days before first monday part of last year "() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 1, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.GridTime coords = geometryClock.getActiveGridTime();

        then:
        assert coords != null
        assert coords.toDisplayString() == "2018-B9-W5-D2_4am+0:20"

    }



}
