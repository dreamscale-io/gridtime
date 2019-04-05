package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;

import java.time.LocalDateTime;

public class ChangeContext extends Movement {

    private final ContextChangeEvent contextChangeEvent;

    public ChangeContext(LocalDateTime moment, ContextChangeEvent contextChangeEvent) {
        super(moment);
        this.contextChangeEvent = contextChangeEvent;
    }

    public ContextChangeEvent getEvent() {
        return contextChangeEvent;
    }

    public static enum EventType {
        CONTEXT_BEGINNING,
        CONTEXT_ENDING,
    }

}
