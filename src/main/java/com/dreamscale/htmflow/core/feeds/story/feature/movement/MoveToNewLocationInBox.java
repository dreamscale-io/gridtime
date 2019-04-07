package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;

import java.time.LocalDateTime;

public class MoveToNewLocationInBox extends Movement {

    private final LocationInBox location;

    public MoveToNewLocationInBox(LocalDateTime moment, LocationInBox location) {
        super(moment);
        this.location = location;
    }

    public LocationInBox getLocation() {
        return location;
    }
}
