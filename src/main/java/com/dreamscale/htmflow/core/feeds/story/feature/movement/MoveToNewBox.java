package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.FocalPoint;

import java.time.LocalDateTime;

public class MoveToNewBox extends Movement {

    private final FocalPoint place;

    public MoveToNewBox(LocalDateTime moment, FocalPoint place) {
        super(moment);
        this.place = place;
    }

    public FocalPoint getPlace() {
        return place;
    }
}
