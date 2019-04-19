package com.dreamscale.htmflow.core.feeds.story.music

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
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
        assert coords1.beats == 1;
        assert coords1.quarters == 1;
        assert coords1.halves == 1;

        assert coords2.beats == 2;
        assert coords2.quarters == 1;
        assert coords2.halves == 1;

        assert coords3.beats == 3;
        assert coords3.quarters == 1;
        assert coords3.halves == 1;

        assert coords4.beats == 4;
        assert coords4.quarters == 1;
        assert coords4.halves == 1;

        assert coords5.beats == 5;
        assert coords5.quarters == 1;
        assert coords5.halves == 1;

        assert coords6.beats == 6;
        assert coords6.quarters == 2;
        assert coords6.halves == 1;

        assert coords7.beats == 7;
        assert coords7.quarters == 2;
        assert coords7.halves == 1;

        assert coords8.beats == 8;
        assert coords8.quarters == 2;
        assert coords8.halves == 1;

        assert coords9.beats == 9;
        assert coords9.quarters == 2;
        assert coords9.halves == 1;

        assert coords10.beats == 10;
        assert coords10.quarters == 2;
        assert coords10.halves == 1;

        assert coords11.beats == 11;
        assert coords11.quarters == 3;
        assert coords11.halves == 2;

        assert coords12.beats == 12;
        assert coords12.quarters == 3;
        assert coords12.halves == 2;

        assert coords13.beats == 13;
        assert coords13.quarters == 3;
        assert coords13.halves == 2;

        assert coords14.beats == 14;
        assert coords14.quarters == 3;
        assert coords14.halves == 2;

        assert coords15.beats == 15;
        assert coords15.quarters == 3;
        assert coords15.halves == 2;

        assert coords16.beats == 16;
        assert coords16.quarters == 4;
        assert coords16.halves == 2;

        assert coords17.beats == 17;
        assert coords17.quarters == 4;
        assert coords17.halves == 2;

        assert coords18.beats == 18;
        assert coords18.quarters == 4;
        assert coords18.halves == 2;

        assert coords19.beats == 19;
        assert coords19.quarters == 4;
        assert coords19.halves == 2;

        assert coords20.beats == 20;
        assert coords20.quarters == 4;
        assert coords20.halves == 2;
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
        assert coords1.beats == 1;
        assert coords1.quarters == 1;
        assert coords1.halves == 1;

        assert coords2.beats == 6;
        assert coords2.quarters == 2;
        assert coords2.halves == 1;

        assert coords3.beats == 11;
        assert coords3.quarters == 3;
        assert coords3.halves == 2;

        assert coords4.beats == 16;
        assert coords4.quarters == 4;
        assert coords4.halves == 2;
    }


}
