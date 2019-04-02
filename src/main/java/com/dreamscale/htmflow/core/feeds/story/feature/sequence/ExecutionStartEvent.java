package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import java.time.LocalDateTime;

public class ExecutionStartEvent {

    private LocalDateTime position;
    private final ExecutionContext executionContext;

    public ExecutionStartEvent(LocalDateTime position, ExecutionContext executionContext) {
        this.position = position;
        this.executionContext = executionContext;
    }
}
