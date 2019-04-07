package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;

import java.time.LocalDateTime;

public class ModifyLocation extends Movement {

    private final LocationInBox location;
    private final int modificationCount;

    public ModifyLocation(LocalDateTime moment, LocationInBox location, int modificationCount) {
        super(moment);
        this.location = location;
        this.modificationCount = modificationCount;
    }

}
