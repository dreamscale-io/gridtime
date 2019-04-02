package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import java.time.LocalDateTime;

public class ExecutionEvent {

    private final ExecutionContext executionContext;
    private final LocalDateTime position;

    public ExecutionEvent(LocalDateTime position, ExecutionContext executionContext) {
        this.position = position;
        this.executionContext = executionContext;
    }

}
