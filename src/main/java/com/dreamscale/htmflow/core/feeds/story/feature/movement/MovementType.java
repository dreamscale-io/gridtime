package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.uri.FlowUriObjectType;

public enum MovementType {
    CHANGE_CONTEXT (FlowUriObjectType.MOVEMENT_CHANGE_CONTEXT),
    EXECUTE_THING (FlowUriObjectType.MOVEMENT_EXECUTE_THING),
    MODIFY_LOCATION (FlowUriObjectType.MOVEMENT_MODIFY_LOCATION),
    MOVE_ACROSS_BRIDGE (FlowUriObjectType.MOVEMENT_ACROSS_BRIDGE),
    MOVE_TO_BOX(FlowUriObjectType.MOVEMENT_TO_BOX),
    MOVE_TO_LOCATION(FlowUriObjectType.MOVEMENT_TO_LOCATION),
    POST_CIRCLE_MESSAGE(FlowUriObjectType.MOVEMENT_POST_MESSAGE);

    private final FlowUriObjectType flowUriObjectType;

    MovementType(FlowUriObjectType flowUriObjectType) {
        this.flowUriObjectType = flowUriObjectType;
    }

    public FlowUriObjectType getFlowUriObjectType() {
        return null;
    }
}
