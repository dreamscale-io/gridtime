package com.dreamscale.gridtime.core.capability.query

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel
import spock.lang.Specification

class GridtimeExpressionSpec extends Specification {


    def "should parse simple gridtime expression" () {
        given:

        String timeScope = "gt[2020,1,3]"

        when:
        GridtimeExpression gtExp = GridtimeExpression.parse(timeScope)

        then:
        assert gtExp != null
        assert gtExp.getYear() == 2020
        assert gtExp.getBlock() == 1
        assert gtExp.getWeek() == 3
        assert gtExp.getZoomLevel() == ZoomLevel.WEEK

    }


    def "should parse range gridtime expression" () {
        given:

        String timeScope = "gt[2020,2,3-5]"

        when:
        GridtimeExpression gtExp = GridtimeExpression.parse(timeScope)

        then:
        assert gtExp != null
        assert gtExp.getYear() == 2020
        assert gtExp.getBlock() == 2
        assert gtExp.getWeek() == 3
        assert gtExp.getRangeExpression() == 5
        assert gtExp.getZoomLevel() == ZoomLevel.WEEK
        assert gtExp.getRangeZoomLevel() == ZoomLevel.WEEK

        assert gtExp.getCoords() == [2020, 2, 3] as List
        assert gtExp.getRangeCoords() == [2020, 2, 5] as List

    }
}
