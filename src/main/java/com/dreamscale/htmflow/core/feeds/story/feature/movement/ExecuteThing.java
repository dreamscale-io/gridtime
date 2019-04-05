package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;

import java.time.LocalDateTime;

public class ExecuteThing extends Movement {

    private final ExecutionDetails executionDetails;
    private final EventType executeEventType;

    public ExecuteThing(LocalDateTime moment, ExecutionDetails executionDetails, EventType executeEventType) {
        super(moment);
        this.executionDetails = executionDetails;
        this.executeEventType = executeEventType;
    }

    public ExecutionDetails getDetails() {
        return executionDetails;
    }

    public static enum EventType {
        START_LONG_EXECUTION,
        END_LONG_EXECUTION,
        EXECUTE_EVENT
    }

}
