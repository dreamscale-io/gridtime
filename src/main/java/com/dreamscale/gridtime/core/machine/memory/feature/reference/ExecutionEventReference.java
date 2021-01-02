package com.dreamscale.gridtime.core.machine.memory.feature.reference;

import com.dreamscale.gridtime.core.machine.memory.type.ExecutionEventType;
import com.dreamscale.gridtime.core.machine.memory.feature.details.ExecutionEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@ToString
public class ExecutionEventReference extends FeatureReference {


    public ExecutionEventReference(ExecutionEventType eventType, String searchKey) {
        super(UUID.randomUUID(), eventType, searchKey, null, false);
    }

    public ExecutionEventReference(ExecutionEventType eventType, String searchKey, ExecutionEvent executionEvent) {
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

    @Override
    public String getDescription() {
        String description = getProcessName() + " ("+toDisplayString()+", "+getExecutionTime().getSeconds() + "s";

        if (isDebug()) {
            description += ", debug";
        }
        description += ")";

        return description;
    }

    public String getProcessName() {
        return ((ExecutionEvent)getDetails()).getProcessName();
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

    public boolean isDebug() {
        return ((ExecutionEvent)getDetails()).isDebug();
    }
}
