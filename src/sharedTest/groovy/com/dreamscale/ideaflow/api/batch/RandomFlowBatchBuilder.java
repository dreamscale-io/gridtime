package com.dreamscale.ideaflow.api.batch;

import static com.dreamscale.ideaflow.core.CoreARandom.aRandom;

public class RandomFlowBatchBuilder extends NewFlowBatch.NewFlowBatchBuilder {

    public RandomFlowBatchBuilder() {
        super();
        timeSent(aRandom.localDateTime())
                .editorActivity(aRandom.editorActivity().build())
                .executionActivity(aRandom.executionActivity().build())
                .modificationActivity(aRandom.modificationActivity().build())
                .idleActivity(aRandom.idleActivity().build())
                .externalActivity(aRandom.externalActivity().build())
                .event(aRandom.event().build());
    }
}
