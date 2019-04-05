package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInFocus;

import java.time.LocalDateTime;

public class MoveToNewLocationInBox extends Movement {

    private final LocationInFocus location;

    public MoveToNewLocationInBox(LocalDateTime moment, LocationInFocus location) {
        super(moment);
        this.location = location;
    }

    public LocationInFocus getLocation() {
        return location;
    }
}
