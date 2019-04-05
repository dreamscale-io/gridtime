package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInFocus;

import java.time.LocalDateTime;

public class ModifyLocation extends Movement {

    private final LocationInFocus location;
    private final int modificationCount;

    public ModifyLocation(LocalDateTime moment, LocationInFocus location, int modificationCount) {
        super(moment);
        this.location = location;
        this.modificationCount = modificationCount;
    }

}
