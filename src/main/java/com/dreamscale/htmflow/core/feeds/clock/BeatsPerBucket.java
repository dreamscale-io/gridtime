package com.dreamscale.htmflow.core.feeds.clock;

public enum BeatsPerBucket {

    BEAT(20), QUARTER(5), HALF(10);

    private final int beatCount;

    BeatsPerBucket(int beatCount) {
        this.beatCount = beatCount;
    }

    public int getBeatCount() {
        return beatCount;
    }

}
