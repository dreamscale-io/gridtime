package com.dreamscale.htmflow.core.feeds.story.music;

public enum BeatSize {

    BEAT(20), QUARTER(5), HALF(10);

    private final int beatCount;

    BeatSize(int beatCount) {
        this.beatCount = beatCount;
    }

    public int getBeatCount() {
        return beatCount;
    }

}
