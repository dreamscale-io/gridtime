package com.dreamscale.gridtime.core.machine.clock;

import com.dreamscale.gridtime.api.grid.Observable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@ToString
public class Beat implements Observable {

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

    public static Beat BEAT_1_OF_6 = new Beat(1, 6);
    public static Beat BEAT_2_OF_6 = new Beat(2, 6);
    public static Beat BEAT_3_OF_6 = new Beat(3, 6);
    public static Beat BEAT_4_OF_6 = new Beat(4, 6);
    public static Beat BEAT_5_OF_6 = new Beat(5, 6);
    public static Beat BEAT_6_OF_6 = new Beat(6, 6);

    public static Beat BEAT_1_OF_7 = new Beat(1, 7);
    public static Beat BEAT_2_OF_7 = new Beat(2, 7);
    public static Beat BEAT_3_OF_7 = new Beat(3, 7);
    public static Beat BEAT_4_OF_7 = new Beat(4, 7);
    public static Beat BEAT_5_OF_7 = new Beat(5, 7);
    public static Beat BEAT_6_OF_7 = new Beat(6, 7);
    public static Beat BEAT_7_OF_7 = new Beat(7, 7);

    public static Beat BEAT_1_OF_9 = new Beat(1, 9);
    public static Beat BEAT_2_OF_9 = new Beat(2, 9);
    public static Beat BEAT_3_OF_9 = new Beat(3, 9);
    public static Beat BEAT_4_OF_9 = new Beat(4, 9);
    public static Beat BEAT_5_OF_9 = new Beat(5, 9);
    public static Beat BEAT_6_OF_9 = new Beat(6, 9);
    public static Beat BEAT_7_OF_9 = new Beat(7, 9);
    public static Beat BEAT_8_OF_9 = new Beat(8, 9);
    public static Beat BEAT_9_OF_9 = new Beat(9, 9);


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

    public static final Beat[] SIX_BEATS_PER_MEASURE = {
            BEAT_1_OF_6,
            BEAT_2_OF_6,
            BEAT_3_OF_6,
            BEAT_4_OF_6,
            BEAT_5_OF_6,
            BEAT_6_OF_6
    };

    public static final Beat[] SEVEN_BEATS_PER_MEASURE = {
            BEAT_1_OF_7,
            BEAT_2_OF_7,
            BEAT_3_OF_7,
            BEAT_4_OF_7,
            BEAT_5_OF_7,
            BEAT_6_OF_7,
            BEAT_7_OF_7,
    };

    public static final Beat[] NINE_BEATS_PER_MEASURE = {
            BEAT_1_OF_9,
            BEAT_2_OF_9,
            BEAT_3_OF_9,
            BEAT_4_OF_9,
            BEAT_5_OF_9,
            BEAT_6_OF_9,
            BEAT_7_OF_9,
            BEAT_8_OF_9,
            BEAT_9_OF_9,
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
        } else if (beatsPerMeasure == 6) {
            return SIX_BEATS_PER_MEASURE;
        } else if (beatsPerMeasure == 7) {
            return SEVEN_BEATS_PER_MEASURE;
        } else if (beatsPerMeasure == 9) {
            return NINE_BEATS_PER_MEASURE;
        }
        return new Beat[0];
    }

    public String toDisplayString() {
        return beatsPerMeasure + "." + beat;
    }
}
