package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;

import java.time.LocalDateTime;

public class MoveToLocation extends Movement {

    private final LocationInBox location;

    public MoveToLocation(LocalDateTime moment, LocationInBox location) {
        super(moment, MovementType.MOVE_TO_LOCATION, location);
        this.location = location;
    }

    public LocationInBox getLocation() {
        return location;
    }
}
