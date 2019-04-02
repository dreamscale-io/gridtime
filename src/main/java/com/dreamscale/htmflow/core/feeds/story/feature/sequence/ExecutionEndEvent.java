package com.dreamscale.htmflow.core.feeds.story.feature.sequence;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.LocalDateTime;

public class ExecutionEndEvent implements FlowFeature {

    private final ExecutionContext executionContext;
    private final LocalDateTime position;

    public ExecutionEndEvent(LocalDateTime position, ExecutionContext executionContext) {
        this.position = position;
        this.executionContext = executionContext;
    }
}
