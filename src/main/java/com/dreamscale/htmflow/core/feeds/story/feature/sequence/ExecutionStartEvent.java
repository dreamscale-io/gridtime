package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.LocalDateTime;

public class ExecutionStartEvent implements FlowFeature {

    private LocalDateTime position;
    private final ExecutionContext executionContext;

    public ExecutionStartEvent(LocalDateTime position, ExecutionContext executionContext) {
        this.position = position;
        this.executionContext = executionContext;
    }
}
