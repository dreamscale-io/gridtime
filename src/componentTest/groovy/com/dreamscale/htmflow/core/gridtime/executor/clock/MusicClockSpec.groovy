package com.dreamscale.htmflow.core.gridtime.executor.clock

import com.dreamscale.htmflow.core.gridtime.executor.alarm.TimeBombTrigger
import spock.lang.Specification

import java.time.Duration

class MusicClockSpec extends Specification {

    MusicClock musicClock

    def "should divide time into 20 beats with quarter note summary"() {
        given:
        musicClock = new MusicClock(ZoomLevel.TWENTY)

        when:

        RelativeBeat beat1 = musicClock.next();
        RelativeBeat beat2 = musicClock.next();
        RelativeBeat beat3 = musicClock.next();
        RelativeBeat beat4 = musicClock.next();
        RelativeBeat beat5 = musicClock.next();
        RelativeBeat beat6 = musicClock.next();
        RelativeBeat beat7 = musicClock.next();
        RelativeBeat beat8 = musicClock.next();
        RelativeBeat beat9 = musicClock.next();
        RelativeBeat beat10 = musicClock.next();
        RelativeBeat beat11 = musicClock.next();
        RelativeBeat beat12 = musicClock.next();
        RelativeBeat beat13 = musicClock.next();
        RelativeBeat beat14 = musicClock.next();
        RelativeBeat beat15 = musicClock.next();
        RelativeBeat beat16 = musicClock.next();
        RelativeBeat beat17 = musicClock.next();
        RelativeBeat beat18 = musicClock.next();
        RelativeBeat beat19 = musicClock.next();
        RelativeBeat beat20 = musicClock.next();


        then:
        assert beat1.beat == 1;
        assert beat1.toSummaryBeat().beat == 1;
        assert beat1.getRelativeDuration() == Duration.ofMinutes(0)
        assert beat1.toSummaryBeat().getRelativeDuration() == Duration.ofMinutes(0)

        assert beat2.beat == 2;
        assert beat2.toSummaryBeat().beat == 1;
        assert beat2.getRelativeDuration() == Duration.ofMinutes(1)
        assert beat2.toSummaryBeat().getRelativeDuration() == Duration.ofMinutes(0)

        assert beat3.beat == 3;
        assert beat3.toSummaryBeat().beat == 1;
        assert beat3.getRelativeDuration() == Duration.ofMinutes(2)
        assert beat3.toSummaryBeat().getRelativeDuration() == Duration.ofMinutes(0)

        assert beat4.beat == 4;
        assert beat4.toSummaryBeat().beat == 1;

        assert beat5.beat == 5;
        assert beat5.toSummaryBeat().beat == 1;

        assert beat6.beat == 6;
        assert beat6.toSummaryBeat().beat == 2;

        assert beat7.beat == 7;
        assert beat7.toSummaryBeat().beat == 2;

        assert beat8.beat == 8;
        assert beat8.toSummaryBeat().beat == 2;

        assert beat9.beat == 9;
        assert beat9.toSummaryBeat().beat == 2;

        assert beat10.beat == 10;
        assert beat10.toSummaryBeat().beat == 2;

        assert beat11.beat == 11;
        assert beat11.toSummaryBeat().beat == 3;
        assert beat11.getRelativeDuration() == Duration.ofMinutes(10)
        assert beat11.toSummaryBeat().getRelativeDuration() == Duration.ofMinutes(10)

        assert beat12.beat == 12;
        assert beat12.toSummaryBeat().beat == 3;

        assert beat13.beat == 13;
        assert beat13.toSummaryBeat().beat == 3;

        assert beat14.beat == 14;
        assert beat14.toSummaryBeat().beat == 3;

        assert beat15.beat == 15;
        assert beat15.toSummaryBeat().beat == 3;

        assert beat16.beat == 16;
        assert beat16.toSummaryBeat().beat == 4;

        assert beat17.beat == 17;
        assert beat17.toSummaryBeat().beat == 4;

        assert beat18.beat == 18;
        assert beat18.toSummaryBeat().beat == 4;

        assert beat19.beat == 19;
        assert beat19.toSummaryBeat().beat == 4;

        assert beat20.beat == 20;
        assert beat20.toSummaryBeat().beat == 4;
    }

    def "should return closest beat for some random duration within measure, rounding down to the containing beat"() {
        given:
        musicClock = new MusicClock(ZoomLevel.TWENTY)

        when:
        RelativeBeat beat5 = musicClock.getClosestBeat(Duration.ofMinutes(4).plusSeconds(5))
        RelativeBeat beat5ExactStart = musicClock.getClosestBeat(Duration.ofMinutes(4))
        RelativeBeat beat5LastSecond = musicClock.getClosestBeat(Duration.ofMinutes(4).plusSeconds(59))
        RelativeBeat beat6 = musicClock.getClosestBeat(Duration.ofMinutes(5))


        then:
        assert beat5.beat == 5
        assert beat5ExactStart.beat == 5
        assert beat5LastSecond.beat == 5
        assert beat6.beat == 6

    }

    def "future time bomb triggers should breakdown measure count and time within measure"() {
        given:
        musicClock = new MusicClock(ZoomLevel.TWENTY)

        when:
        TimeBombTrigger timebomb = musicClock.getFutureTimeBomb(Duration.ofHours(3).plusMinutes(4))

        then:
        assert timebomb.getFullMeasuresRemainingToCountDown() == 9
        assert timebomb.getBeatWithinMeasureToSplode().beat == 5

    }

    def "get forwards iterator that starts from beginning, and clicks to the last beat"() {
        given:
        musicClock = new MusicClock(ZoomLevel.TWENTY)

        when:
        def iterator = musicClock.getForwardsIterator();
        def beat1 = iterator.next();
        def beat2 = iterator.next();

        def lastBeat
        while (iterator.hasNext()) {
            lastBeat = iterator.next()
        }

        then:
        assert beat1.beat == 1
        assert beat2.beat == 2
        assert lastBeat.beat == 20

    }


    def "get backwards iterator that starts from middle, and clicks to first beat"() {
        given:
        musicClock = new MusicClock(ZoomLevel.TWENTY)

        when:
        def iterator = musicClock.getBackwardsIterator(musicClock.getBeat(10));
        def beat10 = iterator.next();
        def beat9 = iterator.next();
        def beat8 = iterator.next();

        def lastBeat
        while (iterator.hasNext()) {
            lastBeat = iterator.next()
        }

        then:
        assert beat10.beat == 10
        assert beat9.beat == 9
        assert beat8.beat == 8
        assert lastBeat.beat == 1

    }

    def "navigation of anchoring beats in music clock should effect active beat"() {
        given:
        musicClock = new MusicClock(ZoomLevel.TWENTY)

        when:
        musicClock.gotoBeat(5);

        then:
        assert musicClock.getBeat(5) == musicClock.getActiveBeat()
        assert musicClock.getActiveBeat().beat == 5

    }


}
