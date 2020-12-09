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
        Metronome.TickScope tick1 = metronome.tick()
        Metronome.TickScope tick2 = metronome.tick()
        Metronome.TickScope tick3 = metronome.tick()
        Metronome.TickScope tick4 = metronome.tick()
        Metronome.TickScope tick5 = metronome.tick()
        Metronome.TickScope tick6 = metronome.tick()
        Metronome.TickScope tick7 = metronome.tick()
        Metronome.TickScope tick8 = metronome.tick()
        Metronome.TickScope tick9 = metronome.tick()
        Metronome.TickScope tick10 = metronome.tick()
        Metronome.TickScope tick11 = metronome.tick()
        Metronome.TickScope tick12 = metronome.tick()
        Metronome.TickScope tick13 = metronome.tick()

        then:
        assert tick1.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+0:00"
        assert tick2.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+0:20"
        assert tick3.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+0:40"
        assert tick4.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+1:00"
        assert tick5.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+1:20"
        assert tick6.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+1:40"
        assert tick7.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+2:00"
        assert tick8.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+2:20"
        assert tick9.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+2:40"
        assert tick10.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+3:00"
        assert tick11.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+3:20"
        assert tick12.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am+3:40"

        assert tick13.from.toDisplayString() == "/gridtime/2019-B1-W1-D1_4am+0:00"

        assert tick12.aggregateTickScopes[0].from.toDisplayString() == "/gridtime/2019-B1-W1-D1_12am"
        assert tick12.aggregateTickScopes[0].to.toDisplayString() == "/gridtime/2019-B1-W1-D1_4am"
    }


    def "getAggregateTick should truncate for block correctly"() {

        when:
        Metronome.TickScope tick = Metronome.getAggregateTick(ZoomLevel.BLOCK,
                GeometryClock.createGridTimeFromCoordinates(ZoomLevel.TWENTY, [2021, 1, 1, 1, 1, 1] as Integer []))

        then:

        assert tick.from.toDisplayString() == "/gridtime/2020-B9"
    }

    def "getAggregateTick should truncate for year correctly"() {

        when:
        Metronome.TickScope tick = Metronome.getAggregateTick(ZoomLevel.YEAR,
                GeometryClock.createGridTimeFromCoordinates(ZoomLevel.TWENTY, [2021, 1, 1, 1, 1, 1] as Integer []))

        then:

        assert tick.from.toDisplayString() == "/gridtime/2020"
    }

    def "should tick over year end and create block 9 block"() {
        given:

        LocalDateTime christmas = LocalDateTime.of(2021, 1, 3, 0, 0);

        Metronome metronome = new Metronome(christmas)

        when:

        Metronome.TickScope lastTickScope = null;

        for (int i = 0; i < 100; i++) {
            Metronome.TickScope tickScope = metronome.tick()

            if (tickScope.getFrom().getBlock() == 1) {
                break;
            }
            lastTickScope = tickScope
        }

        for (int i = 0; i < lastTickScope.aggregateTickScopes.size(); i++ ) {
            println lastTickScope.aggregateTickScopes[i].toDisplayString()
        }

        then:
        assert lastTickScope.from.toDisplayString() == "/gridtime/2020-B9-W4-D7_8pm+3:40"

        assert lastTickScope.aggregateTickScopes[0].from.toDisplayString() == "/gridtime/2020-B9-W4-D7_8pm"
        assert lastTickScope.aggregateTickScopes[1].from.toDisplayString() == "/gridtime/2020-B9-W4-D7"
        assert lastTickScope.aggregateTickScopes[2].from.toDisplayString() == "/gridtime/2020-B9-W4"
        assert lastTickScope.aggregateTickScopes[3].from.toDisplayString() == "/gridtime/2020-B9"
        assert lastTickScope.aggregateTickScopes[4].from.toDisplayString() == "/gridtime/2020"
    }
}
