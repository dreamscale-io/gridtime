package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import java.time.LocalDateTime;

public class ExecutionEvent extends Movement {

    private final ExecutionContext executionContext;

    public ExecutionEvent(LocalDateTime moment, Object reference, ExecutionContext executionContext) {
        super(moment, reference);

        this.executionContext = executionContext;
    }
}
