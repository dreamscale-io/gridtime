package com.dreamscale.htmflow.core.feeds.story.music


import spock.lang.Specification

import java.time.LocalDateTime

class MusicClockSpec extends Specification {

    MusicClock musicClock


    def "should divide time into beats and tick increments"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        LocalDateTime clockEnd = LocalDateTime.of(2019, 1, 7, 2, 40)
        musicClock = new MusicClock(clockStart, clockEnd)

        when:

        MusicClock.Beat coords1 = musicClock.getCurrentBeat();
        MusicClock.Beat coords2 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords3 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords4 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords5 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords6 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords7 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords8 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords9 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords10 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords11 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords12 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords13 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords14 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords15 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords16 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords17 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords18 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords19 = musicClock.next(BeatSize.BEAT);
        MusicClock.Beat coords20 = musicClock.next(BeatSize.BEAT);


        then:
        assert coords1.beat == 1;
        assert coords1.toQuarter().beat == 1;
        assert coords1.toHalf().beat == 1;

        assert coords2.beat == 2;
        assert coords2.toQuarter().beat == 1;
        assert coords2.toHalf().beat == 1;

        assert coords3.beat == 3;
        assert coords3.toQuarter().beat == 1;
        assert coords3.toHalf().beat == 1;

        assert coords4.beat == 4;
        assert coords4.toQuarter().beat == 1;
        assert coords4.toHalf().beat == 1;

        assert coords5.beat == 5;
        assert coords5.toQuarter().beat == 1;
        assert coords5.toHalf().beat == 1;

        assert coords6.beat == 6;
        assert coords6.toQuarter().beat == 2;
        assert coords6.toHalf().beat == 1;

        assert coords7.beat == 7;
        assert coords7.toQuarter().beat == 2;
        assert coords7.toHalf().beat == 1;

        assert coords8.beat == 8;
        assert coords8.toQuarter().beat == 2;
        assert coords8.toHalf().beat == 1;

        assert coords9.beat == 9;
        assert coords9.toQuarter().beat == 2;
        assert coords9.toHalf().beat == 1;

        assert coords10.beat == 10;
        assert coords10.toQuarter().beat == 2;
        assert coords10.toHalf().beat == 1;

        assert coords11.beat == 11;
        assert coords11.toQuarter().beat == 3;
        assert coords11.toHalf().beat == 2;

        assert coords12.beat == 12;
        assert coords12.toQuarter().beat == 3;
        assert coords12.toHalf().beat == 2;

        assert coords13.beat == 13;
        assert coords13.toQuarter().beat == 3;
        assert coords13.toHalf().beat == 2;

        assert coords14.beat == 14;
        assert coords14.toQuarter().beat == 3;
        assert coords14.toHalf().beat == 2;

        assert coords15.beat == 15;
        assert coords15.toQuarter().beat == 3;
        assert coords15.toHalf().beat == 2;

        assert coords16.beat == 16;
        assert coords16.toQuarter().beat == 4;
        assert coords16.toHalf().beat == 2;

        assert coords17.beat == 17;
        assert coords17.toQuarter().beat == 4;
        assert coords17.toHalf().beat == 2;

        assert coords18.beat == 18;
        assert coords18.toQuarter().beat == 4;
        assert coords18.toHalf().beat == 2;

        assert coords19.beat == 19;
        assert coords19.toQuarter().beat == 4;
        assert coords19.toHalf().beat == 2;

        assert coords20.beat == 20;
        assert coords20.toQuarter().beat == 4;
        assert coords20.toHalf().beat == 2;
    }


    def "should pan right into broader increments"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        LocalDateTime clockEnd = LocalDateTime.of(2019, 1, 7, 2, 40)
        musicClock = new MusicClock(clockStart, clockEnd)

        when:

        MusicClock.Beat coords1 = musicClock.getCurrentBeat();
        MusicClock.Beat coords2 = musicClock.next(BeatSize.QUARTER)
        MusicClock.Beat coords3 = musicClock.next(BeatSize.QUARTER)
        MusicClock.Beat coords4 = musicClock.next(BeatSize.QUARTER)

        then:
        assert coords1.beat == 1;
        assert coords1.toQuarter().beat == 1;
        assert coords1.toHalf().beat == 1;

        assert coords2.beat == 6;
        assert coords2.toQuarter().beat == 2;
        assert coords2.toHalf().beat == 1;

        assert coords3.beat == 11;
        assert coords3.toQuarter().beat == 3;
        assert coords3.toHalf().beat == 2;

        assert coords4.beat == 16;
        assert coords4.toQuarter().beat == 4;
        assert coords4.toHalf().beat == 2;
    }


}
