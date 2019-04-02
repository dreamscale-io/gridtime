package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInPlace;

import java.time.LocalDateTime;

public class ModificationEvent {

    private final LocalDateTime position;
    private final LocationInPlace location;
    private final int modificationCount;

    public ModificationEvent(LocalDateTime position, LocationInPlace location, int modificationCount) {
        this.position = position;
        this.location = location;
        this.modificationCount = modificationCount;
    }
}
