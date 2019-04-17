package com.dreamscale.htmflow.core.feeds.executor

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import spock.lang.Specification

import java.time.LocalDateTime

class GeometryClockSpec extends Specification {

    GeometryClock geometryClock


    def "should generate coords matching start of year"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getCoordinates();

        then:
        assert coords != null
        assert coords.year == 2019
        assert coords.daysIntoWeek == 1
        assert coords.weeksIntoYear == 1
        assert coords.fourHourSteps == 1
        assert coords.twentyMinuteSteps == 8
        assert coords.weeksIntoBlock == 1
        assert coords.block == 1

    }

    def "should generate coords starting from 1 and count to 12 20s"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 23, 59)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getCoordinates();

        then:
        assert coords != null
        assert coords.year == 2019
        assert coords.daysIntoWeek == 1
        assert coords.weeksIntoYear == 1
        assert coords.fourHourSteps == 6
        assert coords.twentyMinuteSteps == 12
        assert coords.weeksIntoBlock == 1
        assert coords.block == 1


    }


    def "should count by weeks with monday starts"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 27, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getCoordinates();

        then:
        assert coords != null
        assert coords.year == 2019
        assert coords.daysIntoWeek == 7
        assert coords.weeksIntoYear == 3

    }

    def "should consider days before first monday part of last year "() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 1, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getCoordinates();

        then:
        assert coords != null
        assert coords.year == 2018
        assert coords.daysIntoWeek == 2
        assert coords.weeksIntoYear == 53

    }



}
