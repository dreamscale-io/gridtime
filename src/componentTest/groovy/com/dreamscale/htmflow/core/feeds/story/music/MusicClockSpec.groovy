package com.dreamscale.htmflow.core.feeds.story.music

import com.dreamscale.htmflow.core.feeds.story.music.clock.ClockBeat
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock
import spock.lang.Specification

import java.time.LocalDateTime

class MusicClockSpec extends Specification {

    MusicClock musicClock


    def "should divide time into beats and tick increments"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        LocalDateTime clockEnd = LocalDateTime.of(2019, 1, 7, 2, 40)
        musicClock = new MusicClock(clockStart, clockEnd, 20)

        when:

        ClockBeat coords1 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords2 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords3 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords4 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords5 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords6 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords7 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords8 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords9 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords10 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords11 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords12 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords13 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords14 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords15 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords16 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords17 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords18 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords19 = musicClock.next(BeatSize.BEAT);
        ClockBeat coords20 = musicClock.next(BeatSize.BEAT);


        then:
        assert coords1.beat == 1;
        assert coords1.toQuarter().beat == 1;

        assert coords2.beat == 2;
        assert coords2.toQuarter().beat == 1;

        assert coords3.beat == 3;
        assert coords3.toQuarter().beat == 1;

        assert coords4.beat == 4;
        assert coords4.toQuarter().beat == 1;

        assert coords5.beat == 5;
        assert coords5.toQuarter().beat == 1;

        assert coords6.beat == 6;
        assert coords6.toQuarter().beat == 2;

        assert coords7.beat == 7;
        assert coords7.toQuarter().beat == 2;

        assert coords8.beat == 8;
        assert coords8.toQuarter().beat == 2;

        assert coords9.beat == 9;
        assert coords9.toQuarter().beat == 2;

        assert coords10.beat == 10;
        assert coords10.toQuarter().beat == 2;

        assert coords11.beat == 11;
        assert coords11.toQuarter().beat == 3;

        assert coords12.beat == 12;
        assert coords12.toQuarter().beat == 3;

        assert coords13.beat == 13;
        assert coords13.toQuarter().beat == 3;

        assert coords14.beat == 14;
        assert coords14.toQuarter().beat == 3;

        assert coords15.beat == 15;
        assert coords15.toQuarter().beat == 3;

        assert coords16.beat == 16;
        assert coords16.toQuarter().beat == 4;

        assert coords17.beat == 17;
        assert coords17.toQuarter().beat == 4;

        assert coords18.beat == 18;
        assert coords18.toQuarter().beat == 4;

        assert coords19.beat == 19;
        assert coords19.toQuarter().beat == 4;

        assert coords20.beat == 20;
        assert coords20.toQuarter().beat == 4;
    }


    def "should pan right into broader increments"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        LocalDateTime clockEnd = LocalDateTime.of(2019, 1, 7, 2, 40)
        musicClock = new MusicClock(clockStart, clockEnd, 20)

        when:

        ClockBeat coords1 = musicClock.getCurrentBeat();
        ClockBeat coords2 = musicClock.next(BeatSize.QUARTER)
        ClockBeat coords3 = musicClock.next(BeatSize.QUARTER)
        ClockBeat coords4 = musicClock.next(BeatSize.QUARTER)

        then:
        assert coords1.beat == 1;
        assert coords1.toQuarter().beat == 1;

        assert coords2.beat == 6;
        assert coords2.toQuarter().beat == 2;

        assert coords3.beat == 11;
        assert coords3.toQuarter().beat == 3;

        assert coords4.beat == 16;
        assert coords4.toQuarter().beat == 4;
    }


}
