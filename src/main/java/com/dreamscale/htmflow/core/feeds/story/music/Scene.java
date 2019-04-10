package com.dreamscale.htmflow.core.feeds.story.music;

public class Scene {


    private int activeFeels;

    public void updateFeels(int feels) {
        this.activeFeels = feels;
    }

    public Snapshot snapshot(MusicGeometryClock.Coords coords) {
        return null;
    }
}
