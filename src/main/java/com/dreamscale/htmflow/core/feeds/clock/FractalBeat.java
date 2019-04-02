package com.dreamscale.htmflow.core.feeds.clock;

public enum FractalBeat {


    BEAT(12), EIGHTH(2), QUARTER(3), TRIPLET(4), HALF(6),

    INNER_BEAT(36), INNER_EIGHT(6), INNER_QUARTER(9), INNER_TRIPLET(12), INNER_HALF(18);


    private final int beatCount;

    FractalBeat(int beatCount) {
        this.beatCount = beatCount;
    }

    public int getBeatCount() {
        return beatCount;
    }

}
