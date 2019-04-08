package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;

import java.time.LocalDateTime;

public class ChangeContext extends Movement {

    private final ContextChangeEvent contextChangeEvent;

    public ChangeContext(LocalDateTime moment, ContextChangeEvent contextChangeEvent) {
        super(moment, MovementType.CHANGE_CONTEXT, null);
        this.contextChangeEvent = contextChangeEvent;
    }

    public ContextChangeEvent getEvent() {
        return contextChangeEvent;
    }

    public enum EventType {
        CONTEXT_BEGINNING,
        CONTEXT_ENDING,
    }

}
