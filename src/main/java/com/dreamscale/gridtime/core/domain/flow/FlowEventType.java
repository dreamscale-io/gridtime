package com.dreamscale.gridtime.core.domain.flow;

import com.dreamscale.gridtime.api.flow.event.EventType;

public enum FlowEventType {

    SNIPPET, FILE_REFERENCE, ACTIVATE, DEACTIVATE, NOTE;

    public static FlowEventType toFlowEventType(EventType inputEventType) {

        if (inputEventType.equals(EventType.ACTIVATE) ) {
            return FlowEventType.ACTIVATE;
        }

        if (inputEventType.equals(EventType.DEACTIVATE) ) {
            return FlowEventType.DEACTIVATE;
        }

        if (inputEventType.equals(EventType.NOTE) ) {
            return FlowEventType.NOTE;
        }
        return null;
    }
}
