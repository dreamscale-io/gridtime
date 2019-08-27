package com.dreamscale.gridtime.core.machine.memory.feature.reference;

import com.dreamscale.gridtime.core.machine.memory.type.IdeaFlowStateType;
import com.dreamscale.gridtime.core.machine.memory.feature.details.CircleDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class IdeaFlowStateReference extends FeatureReference {

    public IdeaFlowStateReference(IdeaFlowStateType stateType, String searchKey) {
        super(UUID.randomUUID(), stateType, searchKey, null, false);
    }

    public IdeaFlowStateReference(IdeaFlowStateType stateType, String searchKey, CircleDetails circleDetails) {
        super(UUID.randomUUID(), stateType, searchKey, circleDetails, false);
    }

    public IdeaFlowStateType getIdeaFlowStateType() {
        return (IdeaFlowStateType) getFeatureType();
    }

    @Override
    public String toDisplayString() {
        switch (getIdeaFlowStateType()) {
            case WTF_STATE:
                return "wtf";
            case LEARNING_STATE:
                return "lrn";
            case PROGRESS_STATE:
                return "prg";
        }
        return "?";
    }
}
