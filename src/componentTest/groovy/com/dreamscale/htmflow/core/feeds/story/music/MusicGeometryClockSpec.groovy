package com.dreamscale.htmflow.core.feeds.story.music


import spock.lang.Specification

import java.time.LocalDateTime

class MusicGeometryClockSpec extends Specification {

    MusicGeometryClock musicGeometryClock


    def "should divide time into beats and tick increments"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        LocalDateTime clockEnd = LocalDateTime.of(2019, 1, 7, 2, 40)
        musicGeometryClock = new MusicGeometryClock(clockStart, clockEnd)

        when:

        MusicGeometryClock.Coords coords1 = musicGeometryClock.getCoordinates();
        MusicGeometryClock.Coords coords2 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords3 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords4 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords5 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords6 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords7 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords8 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords9 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords10 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords11 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords12 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords13 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords14 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords15 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords16 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords17 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords18 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords19 = musicGeometryClock.tick();
        MusicGeometryClock.Coords coords20 = musicGeometryClock.tick();


        then:
        assert coords1.beat == 1;
        assert coords1.quarter == 1;
        assert coords1.half == 1;

        assert coords2.beat == 2;
        assert coords2.quarter == 1;
        assert coords2.half == 1;

        assert coords3.beat == 3;
        assert coords3.quarter == 1;
        assert coords3.half == 1;

        assert coords4.beat == 4;
        assert coords4.quarter == 1;
        assert coords4.half == 1;

        assert coords5.beat == 5;
        assert coords5.quarter == 1;
        assert coords5.half == 1;

        assert coords6.beat == 6;
        assert coords6.quarter == 2;
        assert coords6.half == 1;

        assert coords7.beat == 7;
        assert coords7.quarter == 2;
        assert coords7.half == 1;

        assert coords8.beat == 8;
        assert coords8.quarter == 2;
        assert coords8.half == 1;

        assert coords9.beat == 9;
        assert coords9.quarter == 2;
        assert coords9.half == 1;

        assert coords10.beat == 10;
        assert coords10.quarter == 2;
        assert coords10.half == 1;

        assert coords11.beat == 11;
        assert coords11.quarter == 3;
        assert coords11.half == 2;

        assert coords12.beat == 12;
        assert coords12.quarter == 3;
        assert coords12.half == 2;

        assert coords13.beat == 13;
        assert coords13.quarter == 3;
        assert coords13.half == 2;

        assert coords14.beat == 14;
        assert coords14.quarter == 3;
        assert coords14.half == 2;

        assert coords15.beat == 15;
        assert coords15.quarter == 3;
        assert coords15.half == 2;

        assert coords16.beat == 16;
        assert coords16.quarter == 4;
        assert coords16.half == 2;

        assert coords17.beat == 17;
        assert coords17.quarter == 4;
        assert coords17.half == 2;

        assert coords18.beat == 18;
        assert coords18.quarter == 4;
        assert coords18.half == 2;

        assert coords19.beat == 19;
        assert coords19.quarter == 4;
        assert coords19.half == 2;

        assert coords20.beat == 20;
        assert coords20.quarter == 4;
        assert coords20.half == 2;
    }


    def "should pan right into broader increments"() {
        given:
        LocalDateTime clockStart = LocalDateTime.of(2019, 1, 7, 2, 20)
        LocalDateTime clockEnd = LocalDateTime.of(2019, 1, 7, 2, 40)
        musicGeometryClock = new MusicGeometryClock(clockStart, clockEnd)

        when:

        MusicGeometryClock.Coords coords1 = musicGeometryClock.getCoordinates();
        MusicGeometryClock.Coords coords2 = musicGeometryClock.panRight(BeatsPerBucket.QUARTER)
        MusicGeometryClock.Coords coords3 = musicGeometryClock.panRight(BeatsPerBucket.QUARTER)
        MusicGeometryClock.Coords coords4 = musicGeometryClock.panRight(BeatsPerBucket.QUARTER)

        then:
        assert coords1.beat == 1;
        assert coords1.quarter == 1;
        assert coords1.half == 1;

        assert coords2.beat == 6;
        assert coords2.quarter == 2;
        assert coords2.half == 1;

        assert coords3.beat == 11;
        assert coords3.quarter == 3;
        assert coords3.half == 2;

        assert coords4.beat == 16;
        assert coords4.quarter == 4;
        assert coords4.half == 2;
    }


}
