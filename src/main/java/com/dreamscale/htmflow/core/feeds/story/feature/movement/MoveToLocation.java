package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.Traversal;

import java.time.LocalDateTime;

public class MoveToLocation extends Movement {

    private final LocationInBox location;
    private final Traversal traversal;

    public MoveToLocation(LocalDateTime moment, LocationInBox location, Traversal traversal) {
        super(moment, MovementType.MOVE_TO_LOCATION, location);
        this.location = location;
        this.traversal = traversal;
    }

    public LocationInBox getLocation() {
        return location;
    }

    public Traversal getTraversal() {
        return traversal;
    }
}
