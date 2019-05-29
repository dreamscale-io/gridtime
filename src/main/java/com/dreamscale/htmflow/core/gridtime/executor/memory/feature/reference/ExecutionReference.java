package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.ExecutionEventType;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.IdeaFlowStateType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.ExecutionEvent;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@ToString
public class ExecutionReference extends FeatureReference {


    public ExecutionReference(ExecutionEventType eventType, String searchKey) {
        super(UUID.randomUUID(), eventType, searchKey, null, false);
    }

    public ExecutionReference(ExecutionEventType eventType, String searchKey, ExecutionEvent executionEvent) {
        super(UUID.randomUUID(), eventType, searchKey, executionEvent, false);
    }

    public ExecutionEventType getExecType() {
        return (ExecutionEventType) getFeatureType();
    }

    @Override
    public String toDisplayString() {
        if (getExecType() == ExecutionEventType.TEST) {
            if (isRed()) {
                return "r";
            } else {
                return "g";
            }
        }

        if (getExecType() == ExecutionEventType.APP) {
            return "a";
        }

        return "?";
    }

    public LocalDateTime getPosition() {
        return ((ExecutionEvent)getDetails()).getPosition();
    }

    public Duration getExecutionTime() {
        return ((ExecutionEvent)getDetails()).getDuration();
    }

    public boolean isRed() {
        return ((ExecutionEvent)getDetails()).isRed();
    }

    public boolean isGreen() {
        return ((ExecutionEvent)getDetails()).isGreen();
    }
}
