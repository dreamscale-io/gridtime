package com.dreamscale.gridtime.core.feeds.story.grid

import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.CandleStick
import spock.lang.Specification

class CandleStickSpec extends Specification {

     CandleStick candleStick



    def "should calculate floating metrics"() {
        given:
        candleStick = new CandleStick()

        when:
        candleStick.addSample(5)
        candleStick.addSample(10)
        candleStick.addSample(15)
        candleStick.addSample(20)

        then:
        assert candleStick.getSampleCount() == 4
        assert candleStick.getAvg() == 12.5d
        assert candleStick.getMin() == 5
        assert candleStick.getMax() == 20
        assert candleStick.getStddev() == Math.sqrt((Math.pow(7.5, 2) + Math.pow(2.5, 2) + Math.pow(2.5, 2) + Math.pow(7.5, 2))/4)
    }

    def "should combine metrics"() {
        given:
        CandleStick candleStick1 = new CandleStick()
        CandleStick candleStick2 = new CandleStick()


        when:
        candleStick1.addSample(5)
        candleStick1.addSample(10)
        candleStick2.addSample(15)
        candleStick2.addSample(20)

        candleStick1.combineAggregate(candleStick2)

        then:
        assert candleStick1.getSampleCount() == 4
        assert candleStick1.getAvg() == 12.5d
        assert candleStick1.getMin() == 5
        assert candleStick1.getMax() == 20
        assert candleStick1.getStddev() == Math.sqrt((Math.pow(7.5, 2) + Math.pow(2.5, 2) + Math.pow(2.5, 2) + Math.pow(7.5, 2))/4)
    }
}
