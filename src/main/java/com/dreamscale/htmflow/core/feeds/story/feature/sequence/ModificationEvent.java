package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import java.time.LocalDateTime;

public class ModificationEvent extends Movement {

    private final ModificationContext modificationContext;

    public ModificationEvent(LocalDateTime moment, Object reference, ModificationContext modificationContext) {
        super(moment, reference);

        this.modificationContext = modificationContext;
    }
}
