package com.dreamscale.htmflow.core.feeds.story.music.clock;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@ToString
public class Beat {

    public static Beat BEAT_1_OF_20 = new Beat(1, 20);
    public static Beat BEAT_2_OF_20 = new Beat(2, 20);
    public static Beat BEAT_3_OF_20 = new Beat(3, 20);
    public static Beat BEAT_4_OF_20 = new Beat(4, 20);
    public static Beat BEAT_5_OF_20 = new Beat(5, 20);
    public static Beat BEAT_6_OF_20 = new Beat(6, 20);
    public static Beat BEAT_7_OF_20 = new Beat(7, 20);
    public static Beat BEAT_8_OF_20 = new Beat(8, 20);
    public static Beat BEAT_9_OF_20 = new Beat(9, 20);
    public static Beat BEAT_10_OF_20 = new Beat(10, 20);
    public static Beat BEAT_11_OF_20 = new Beat(11, 20);
    public static Beat BEAT_12_OF_20 = new Beat(12, 20);
    public static Beat BEAT_13_OF_20 = new Beat(13, 20);
    public static Beat BEAT_14_OF_20 = new Beat(14, 20);
    public static Beat BEAT_15_OF_20 = new Beat(15, 20);
    public static Beat BEAT_16_OF_20 = new Beat(16, 20);
    public static Beat BEAT_17_OF_20 = new Beat(17, 20);
    public static Beat BEAT_18_OF_20 = new Beat(18, 20);
    public static Beat BEAT_19_OF_20 = new Beat(19, 20);
    public static Beat BEAT_20_OF_20 = new Beat(20, 20);

    public static Beat BEAT_1_OF_4 = new Beat(1, 4);
    public static Beat BEAT_2_OF_4 = new Beat(2, 4);
    public static Beat BEAT_3_OF_4 = new Beat(3, 4);
    public static Beat BEAT_4_OF_4 = new Beat(4, 4);

    public static Beat BEAT_1_OF_12 = new Beat(1, 12);
    public static Beat BEAT_2_OF_12 = new Beat(2, 12);
    public static Beat BEAT_3_OF_12 = new Beat(3, 12);
    public static Beat BEAT_4_OF_12 = new Beat(4, 12);
    public static Beat BEAT_5_OF_12 = new Beat(5, 12);
    public static Beat BEAT_6_OF_12 = new Beat(6, 12);
    public static Beat BEAT_7_OF_12 = new Beat(7, 12);
    public static Beat BEAT_8_OF_12 = new Beat(8, 12);
    public static Beat BEAT_9_OF_12 = new Beat(9, 12);
    public static Beat BEAT_10_OF_12 = new Beat(10, 12);
    public static Beat BEAT_11_OF_12 = new Beat(11, 12);
    public static Beat BEAT_12_OF_12 = new Beat(12, 12);

    private int beat;
    private int beatsPerMeasure;
    private int angle;
    private int nextBeatAngle;

    public static final Beat[] TWENTY_BEATS_PER_MEASURE = {
            BEAT_1_OF_20,
            BEAT_2_OF_20,
            BEAT_3_OF_20,
            BEAT_4_OF_20,
            BEAT_5_OF_20,
            BEAT_6_OF_20,
            BEAT_7_OF_20,
            BEAT_8_OF_20,
            BEAT_9_OF_20,
            BEAT_10_OF_20,
            BEAT_11_OF_20,
            BEAT_12_OF_20,
            BEAT_13_OF_20,
            BEAT_14_OF_20,
            BEAT_15_OF_20,
            BEAT_16_OF_20,
            BEAT_17_OF_20,
            BEAT_18_OF_20,
            BEAT_19_OF_20,
            BEAT_20_OF_20
    };

    public static final Beat[] FOUR_BEATS_PER_MEASURE = {
            BEAT_1_OF_4,
            BEAT_2_OF_4,
            BEAT_3_OF_4,
            BEAT_4_OF_4
    };

    public static final Beat[] TWELVE_BEATS_PER_MEASURE = {
            BEAT_1_OF_12,
            BEAT_2_OF_12,
            BEAT_3_OF_12,
            BEAT_4_OF_12,
            BEAT_5_OF_12,
            BEAT_6_OF_12,
            BEAT_7_OF_12,
            BEAT_8_OF_12,
            BEAT_9_OF_12,
            BEAT_10_OF_12,
            BEAT_11_OF_12,
            BEAT_12_OF_12
    };


    public Beat(int beat, int beatsPerMeasure) {
        this.beat = beat;
        this.beatsPerMeasure = beatsPerMeasure;
        this.angle = Math.floorDiv((beat - 1) * 360 , beatsPerMeasure);
        this.nextBeatAngle = Math.floorDiv(beat * 360 , beatsPerMeasure);
    }

    boolean isWithin(Beat aBiggerBeat) {
        return (angle >= aBiggerBeat.angle && nextBeatAngle <= aBiggerBeat.nextBeatAngle);
    }


    public static Beat[] getFlyweightBeats(int beatsPerMeasure) {
        if (beatsPerMeasure == 4) {
            return FOUR_BEATS_PER_MEASURE;
        } else if (beatsPerMeasure == 12) {
            return TWELVE_BEATS_PER_MEASURE;
        } else if (beatsPerMeasure == 20) {
            return TWENTY_BEATS_PER_MEASURE;
        }
        return new Beat[0];
    }
}
