package com.dreamscale.htmflow.core.feeds.executor

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import spock.lang.Specification

import java.time.LocalDateTime

class GeometryClockSpec extends Specification {

    GeometryClock geometryClock


    def "should generate coords matching start of year"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getCoordinates();

        then:
        assert coords != null
        assert coords.currentYear == 2019
        assert coords.daysIntoWeek == 1
        assert coords.weeksIntoYear == 1
        assert coords.hoursIntoDay == 4
        assert coords.minuteBucketsIntoHour == 2
        assert coords.weeksIntoBlock == 1
        assert coords.blocksIntoYear == 1


    }

    def "should count by weeks with monday starts"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 27, 4, 20)
        geometryClock = new GeometryClock(clockStart)

        when:
        GeometryClock.Coords coords = geometryClock.getCoordinates();

        then:
        assert coords != null
        assert coords.currentYear == 2019
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
        assert coords.currentYear == 2018
        assert coords.daysIntoWeek == 2
        assert coords.weeksIntoYear == 53

    }



}
