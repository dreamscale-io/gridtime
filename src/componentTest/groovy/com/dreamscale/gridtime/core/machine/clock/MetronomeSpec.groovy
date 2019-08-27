package com.dreamscale.gridtime.core.machine.clock


import spock.lang.Specification

import java.time.LocalDateTime

class MetronomeSpec extends Specification {

    LocalDateTime clockStart

    def setup() {
        clockStart = GeometryClock.getFirstMomentOfYear(2019)
    }

    def "should tick from beginning without repeating first node"() {
        given:
        Metronome metronome = new Metronome(clockStart)

        when:
        Metronome.Tick tick1 = metronome.tick()
        Metronome.Tick tick2 = metronome.tick()
        Metronome.Tick tick3 = metronome.tick()
        Metronome.Tick tick4 = metronome.tick()
        Metronome.Tick tick5 = metronome.tick()
        Metronome.Tick tick6 = metronome.tick()
        Metronome.Tick tick7 = metronome.tick()
        Metronome.Tick tick8 = metronome.tick()
        Metronome.Tick tick9 = metronome.tick()
        Metronome.Tick tick10 = metronome.tick()
        Metronome.Tick tick11 = metronome.tick()
        Metronome.Tick tick12 = metronome.tick()
        Metronome.Tick tick13 = metronome.tick()

        then:
        assert tick1.from.toDisplayString() == "2019-B1-W1-D1_12am+0:00"
        assert tick2.from.toDisplayString() == "2019-B1-W1-D1_12am+0:20"
        assert tick3.from.toDisplayString() == "2019-B1-W1-D1_12am+0:40"
        assert tick4.from.toDisplayString() == "2019-B1-W1-D1_12am+1:00"
        assert tick5.from.toDisplayString() == "2019-B1-W1-D1_12am+1:20"
        assert tick6.from.toDisplayString() == "2019-B1-W1-D1_12am+1:40"
        assert tick7.from.toDisplayString() == "2019-B1-W1-D1_12am+2:00"
        assert tick8.from.toDisplayString() == "2019-B1-W1-D1_12am+2:20"
        assert tick9.from.toDisplayString() == "2019-B1-W1-D1_12am+2:40"
        assert tick10.from.toDisplayString() == "2019-B1-W1-D1_12am+3:00"
        assert tick11.from.toDisplayString() == "2019-B1-W1-D1_12am+3:20"
        assert tick12.from.toDisplayString() == "2019-B1-W1-D1_12am+3:40"

        assert tick13.from.toDisplayString() == "2019-B1-W1-D1_4am+0:00"

        assert tick12.aggregateTicks[0].from.toDisplayString() == "2019-B1-W1-D1_12am"
        assert tick12.aggregateTicks[0].to.toDisplayString() == "2019-B1-W1-D1_4am"
    }

}
