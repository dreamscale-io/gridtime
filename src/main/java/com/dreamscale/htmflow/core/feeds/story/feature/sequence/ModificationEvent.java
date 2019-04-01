package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import java.time.LocalDateTime;

public class ModificationEvent extends Movement {

    private final int modificationCount;

    public ModificationEvent(LocalDateTime moment, Object reference, int modificationCount) {
        super(moment, reference);

        this.modificationCount = modificationCount;
    }
}
