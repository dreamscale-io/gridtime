package com.dreamscale.htmflow.core.gridtime.executor

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock
import spock.lang.Specification

import java.time.LocalDateTime

class GeometryClockSpec extends Specification {

    GeometryClock geometryClock


    def "should generate coords matching start of year"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getActiveCoords();

        then:
        assert coords != null
        assert coords.getFormattedGridTime() == "2019B1-W1-D1_12am+2:00"

    }

    def "should generate coords starting from 1 and count to 12 20s"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 23, 59)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getActiveCoords();

        then:
        assert coords != null
        assert coords.getFormattedGridTime() == "2019B1-W1-D1_8pm+3:00"
    }


    def "should count by weeks with monday starts"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 27, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getActiveCoords();

        then:
        assert coords != null
        assert coords.getFormattedGridTime() == "2019B1-W3-D7_4am+0:40"

    }

    def "should consider days before first monday part of last year "() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 1, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getActiveCoords();

        then:
        assert coords != null
        assert coords.getFormattedGridTime() == "2018B9-W5-D2_4am+0:40"

    }



}
