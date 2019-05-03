package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ExecuteThing extends Movement {

    private EventType executeEventType;
    private ExecutionDetails executionDetails;

    public ExecuteThing(LocalDateTime moment, ExecutionDetails executionDetails, EventType executeEventType) {
        super(moment, FlowObjectType.MOVEMENT_EXECUTE_THING);
        this.executionDetails = executionDetails;
        this.executeEventType = executeEventType;
    }

    public ExecuteThing() {
        super(FlowObjectType.MOVEMENT_EXECUTE_THING);
    }

    public enum EventType {
        START_LONG_EXECUTION,
        END_LONG_EXECUTION,
        EXECUTE_EVENT
    }

}
