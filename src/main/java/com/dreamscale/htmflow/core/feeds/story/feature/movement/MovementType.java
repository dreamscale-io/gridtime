package com.dreamscale.htmflow.core.feeds.story.feature.movement;

import com.dreamscale.htmflow.core.domain.uri.FlowObjectType;

public enum MovementType {
    CHANGE_CONTEXT (FlowObjectType.MOVEMENT_CHANGE_CONTEXT),
    EXECUTE_THING (FlowObjectType.MOVEMENT_EXECUTE_THING),
    MODIFY_LOCATION (FlowObjectType.MOVEMENT_MODIFY_LOCATION),
    MOVE_ACROSS_BRIDGE (FlowObjectType.MOVEMENT_ACROSS_BRIDGE),
    MOVE_TO_BOX(FlowObjectType.MOVEMENT_TO_BOX),
    MOVE_TO_LOCATION(FlowObjectType.MOVEMENT_TO_LOCATION),
    POST_CIRCLE_MESSAGE(FlowObjectType.MOVEMENT_POST_MESSAGE);

    private final FlowObjectType flowObjectType;

    MovementType(FlowObjectType flowObjectType) {
        this.flowObjectType = flowObjectType;
    }

    public FlowObjectType getFlowObjectType() {
        return null;
    }
}
